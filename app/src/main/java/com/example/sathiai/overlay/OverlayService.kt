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
import com.example.sathiai.BuildConfig
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
    private var closeZoneView: FrameLayout? = null
    private var overlayComposeView: ComposeView? = null
    
    // Position state for anchoring and draggability
    private var lastBubbleX = 20
    private var lastBubbleY = 400
    private var lastPopupX = -1
    private var lastPopupY = -1

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // ── Compose States ────────────────────────────────────────────────────────
    private var isThinkingState = mutableStateOf(false)
    private var inputTextState = mutableStateOf("")
    private val chatHistory = mutableStateListOf<Message>()

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
        createCloseZone()
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
        closeZoneView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
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
        bubbleView = FrameLayout(this)
        val bubble = android.widget.TextView(this).apply {
            text = "✦"; textSize = 20f; gravity = android.view.Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
            val gd = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                colors = intArrayOf(android.graphics.Color.parseColor("#7B61FF"), android.graphics.Color.parseColor("#4A3FA0"))
            }
            background = gd; elevation = 16f
            layoutParams = FrameLayout.LayoutParams(120, 120).apply { gravity = android.view.Gravity.CENTER }
        }
        bubbleView.addView(bubble)

        val params = WindowManager.LayoutParams(
            140, 140,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20; y = 400
        }

        windowManager.addView(bubbleView, params)
        
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        var initialX = 0; var initialY = 0; var touchX = 0f; var touchY = 0f; var isDragging = false
        
        bubbleView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y; touchX = event.rawX; touchY = event.rawY; isDragging = false
                    showCloseZone()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt(); val dy = (event.rawY - touchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true
                        params.x = initialX + dx; params.y = initialY + dy
                        windowManager.updateViewLayout(bubbleView, params)
                        
                        // Check if over close zone
                        val overClose = params.y > screenHeight * 0.8 && Math.abs(params.x - screenWidth/2 + 70) < 150
                        updateCloseZoneState(overClose)
                        true
                    } else true
                }
                MotionEvent.ACTION_UP -> {
                    hideCloseZone()
                    if (isDragging) {
                        // Magnetic Snap to Edges
                        val targetX = if (params.x + 70 < screenWidth / 2) 20 else screenWidth - 160
                        
                        // Check for dismissal
                        if (params.y > screenHeight * 0.8 && Math.abs(params.x - screenWidth/2 + 70) < 150) {
                            stopSelf()
                        } else {
                            lastBubbleX = targetX
                            lastBubbleY = params.y
                            animateSnap(params, targetX)
                        }
                    } else {
                        lastBubbleX = params.x
                        lastBubbleY = params.y
                        toggleOverlay()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun animateSnap(params: WindowManager.LayoutParams, targetX: Int) {
        val startX = params.x
        val duration = 300L
        val startTime = System.currentTimeMillis()
        
        serviceScope.launch {
            while (System.currentTimeMillis() - startTime < duration) {
                val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
                params.x = (startX + (targetX - startX) * progress).toInt()
                try { windowManager.updateViewLayout(bubbleView, params) } catch (_: Exception) {}
                kotlinx.coroutines.delay(16)
            }
            params.x = targetX
            try { windowManager.updateViewLayout(bubbleView, params) } catch (_: Exception) {}
        }
    }

    private fun createCloseZone() {
        closeZoneView = FrameLayout(this).apply {
            visibility = View.GONE
            val circle = android.view.View(this@OverlayService).apply {
                val gd = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(android.graphics.Color.parseColor("#44FF0000"))
                    setStroke(2, android.graphics.Color.RED)
                }
                background = gd
                layoutParams = FrameLayout.LayoutParams(160, 160).apply { gravity = Gravity.CENTER }
            }
            val icon = android.widget.ImageView(this@OverlayService).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(android.graphics.Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(60, 60).apply { gravity = Gravity.CENTER }
            }
            addView(circle)
            addView(icon)
        }

        val params = WindowManager.LayoutParams(
            200, 200,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 100
        }
        windowManager.addView(closeZoneView, params)
    }

    private fun showCloseZone() { closeZoneView?.visibility = View.VISIBLE }
    private fun hideCloseZone() { closeZoneView?.visibility = View.GONE }
    private fun updateCloseZoneState(active: Boolean) {
        closeZoneView?.getChildAt(0)?.scaleX = if (active) 1.5f else 1.0f
        closeZoneView?.getChildAt(0)?.scaleY = if (active) 1.5f else 1.0f
    }

    private fun toggleOverlay() {
        if (overlayComposeView == null) showOverlay() else hideOverlay()
    }

    private fun showOverlay() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Compact Popup Dimensions
        val popupWidth = (screenWidth * 0.75f).toInt()
        val popupHeight = (screenHeight * 0.45f).toInt()

        overlayComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            
            setContent {
                PremiumOverlayUI(
                    messages = chatHistory,
                    isThinking = isThinkingState.value,
                    inputText = inputTextState.value,
                    onInputChanged = { inputTextState.value = it },
                    onAnalyzeScreen = { analyzeScreen() },
                    onClear = { chatHistory.clear() },
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

        // Adaptive Positioning Logic
        val params = WindowManager.LayoutParams(
            popupWidth,
            popupHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            
            if (lastPopupX != -1) {
                x = lastPopupX
                y = lastPopupY
            } else {
                // Initial Anchoring: Open away from edges
                x = if (lastBubbleX < screenWidth / 2) {
                    lastBubbleX + 150 // Open to the right of bubble
                } else {
                    lastBubbleX - popupWidth - 10 // Open to the left of bubble
                }
                
                y = if (lastBubbleY < screenHeight / 2) {
                    lastBubbleY // Align top with bubble
                } else {
                    lastBubbleY - popupHeight + 140 // Align bottom with bubble
                }
                
                // Edge constraints
                x = x.coerceIn(20, screenWidth - popupWidth - 20)
                y = y.coerceIn(50, screenHeight - popupHeight - 100)
            }
            
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            windowAnimations = android.R.style.Animation_Dialog // Basic animation for now
        }

        windowManager.addView(overlayComposeView, params)
        
        // Draggable Popup Logic
        var initialX = 0; var initialY = 0; var touchX = 0f; var touchY = 0f
        overlayComposeView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y; touchX = event.rawX; touchY = event.rawY; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt(); val dy = (event.rawY - touchY).toInt()
                    params.x = (initialX + dx).coerceIn(0, screenWidth - popupWidth)
                    params.y = (initialY + dy).coerceIn(0, screenHeight - popupHeight)
                    lastPopupX = params.x
                    lastPopupY = params.y
                    try { windowManager.updateViewLayout(overlayComposeView, params) } catch (_: Exception) {}
                    true
                }
                else -> false
            }
        }
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
        val userMsg = Message(role = "user", content = userMessage)
        chatHistory.add(userMsg)
        isThinkingState.value = true

        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank()) {
            chatHistory.add(
                Message(
                    role = "assistant",
                    content = "Error: Missing GROQ_API_KEY (set it in local.properties or env)"
                )
            )
            isThinkingState.value = false
            return
        }
        
        serviceScope.launch {
            try {
                val request = ChatRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = chatHistory.toList()
                )
                val response = RetrofitInstance.api.sendMessage(
                    token = "Bearer $apiKey",
                    request = request
                )
                val reply = response.choices.firstOrNull()?.message?.content?.toString() ?: "No response"
                chatHistory.add(Message(role = "assistant", content = reply))
            } catch (e: Exception) {
                chatHistory.add(Message(role = "assistant", content = "Error: ${e.message}"))
            } finally {
                isThinkingState.value = false
            }
        }
    }
}