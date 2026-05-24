package com.example.sathiai.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.sathiai.network.ChatRequest
import com.example.sathiai.network.Message
import com.example.sathiai.network.RetrofitInstance
import com.example.sathiai.services.ScreenAnalyzer
import com.example.sathiai.services.ScreenReaderService
import com.example.sathiai.ui.components.PremiumOverlayUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: FrameLayout
    private var overlayComposeView: ComposeView? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // ── Compose States ────────────────────────────────────────────────────────
    private var responseTextState = mutableStateOf("")
    private var isThinkingState = mutableStateOf(false)
    private var inputTextState = mutableStateOf("")

    // ── Lifecycle Support ─────────────────────────────────────────────────────
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val CHANNEL_ID      = "sathiai_overlay_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        if (::bubbleView.isInitialized) {
            try { windowManager.removeView(bubbleView) } catch (_: Exception) {}
        }
        hideOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SathiAI Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps SathiAI overlay running"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SathiAI Copilot Active")
            .setContentText("Expand to analyze screen")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private fun createBubble() {
        // Reuse existing bubble logic from user's code for consistency
        // (Assuming existing bubble design is preserved as per instructions)
        bubbleView = FrameLayout(this)
        val bubble = android.widget.TextView(this).apply {
            text = "✦"; textSize = 22f; gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
            val gd = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                colors = intArrayOf(android.graphics.Color.parseColor("#7B61FF"), android.graphics.Color.parseColor("#4A3FA0"))
            }
            background = gd; elevation = 24f
            layoutParams = FrameLayout.LayoutParams(140, 140).apply { gravity = android.view.Gravity.CENTER }
        }
        bubbleView.addView(bubble)

        val params = WindowManager.LayoutParams(
            170, 170,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80; y = 320
        }

        windowManager.addView(bubbleView, params)
        
        var initialX = 0; var initialY = 0; var touchX = 0f; var touchY = 0f; var isDragging = false
        bubbleView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y; touchX = event.rawX; touchY = event.rawY; isDragging = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt(); val dy = (event.rawY - touchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true; params.x = initialX + dx; params.y = initialY + dy
                        windowManager.updateViewLayout(bubbleView, params); true
                    } else true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) toggleOverlay()
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleOverlay() {
        if (overlayComposeView == null) showOverlay() else hideOverlay()
    }

    private fun showOverlay() {
        overlayComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            
            setContent {
                PremiumOverlayUI(
                    responseText = responseTextState.value,
                    isThinking = isThinkingState.value,
                    inputText = inputTextState.value,
                    onInputChanged = { inputTextState.value = it },
                    onAnalyzeScreen = { analyzeScreen() },
                    onClear = { responseTextState.value = "" },
                    onSend = { 
                        val msg = inputTextState.value
                        if (msg.isNotBlank()) {
                            askGroq(msg)
                            inputTextState.value = ""
                        }
                    },
                    onClose = { hideOverlay() }
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            1500, // Fixed high quality height
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager.addView(overlayComposeView, params)
    }

    private fun hideOverlay() {
        overlayComposeView?.let {
            windowManager.removeView(it)
            overlayComposeView = null
        }
    }

    private fun analyzeScreen() {
        val screenText = ScreenAnalyzer.cleanText(ScreenReaderService.latestScreenText)
        if (screenText.isNotBlank()) {
            askGroq("The user is looking at this screen content:\n$screenText\n\nPlease provide insights.")
        }
    }

    private fun askGroq(userMessage: String) {
        isThinkingState.value = true
        serviceScope.launch {
            try {
                val request = ChatRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = listOf(Message(role = "user", content = userMessage))
                )
                val response = RetrofitInstance.api.sendMessage(
                    token = "Bearer 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",
                    request = request
                )
                val reply = response.choices.firstOrNull()?.message?.content?.toString() ?: "No response"
                responseTextState.value = reply
            } catch (e: Exception) {
                responseTextState.value = "Error: ${e.message}"
            } finally {
                isThinkingState.value = false
            }
        }
    }
}