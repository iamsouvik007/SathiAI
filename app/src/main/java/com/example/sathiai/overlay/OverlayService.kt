//package com.example.sathiai.overlay
//
//import android.animation.ValueAnimator
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Intent
//import android.graphics.Color
//import android.graphics.PixelFormat
//import android.graphics.Typeface
//import android.graphics.drawable.GradientDrawable
//import android.os.Build
//import android.os.Handler
//import android.os.IBinder
//import android.os.Looper
//import android.view.Gravity
//import android.view.MotionEvent
//import android.view.View
//import android.view.WindowManager
//import android.view.animation.DecelerateInterpolator
//import android.view.animation.OvershootInterpolator
//import android.widget.*
//import androidx.core.app.NotificationCompat
//import com.example.sathiai.network.ChatRequest
//import com.example.sathiai.network.Message
//import com.example.sathiai.network.RetrofitInstance
//import com.example.sathiai.services.ScreenAnalyzer          // FIX 6
//import com.example.sathiai.services.ScreenReaderService
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob                     // FIX 2
//import kotlinx.coroutines.cancel                            // FIX 3
//import kotlinx.coroutines.launch
//
//class OverlayService : Service() {
//
//    // ── Colors ────────────────────────────────────────────────────────────────
//    private val colorSurface     = Color.parseColor("#161A24")
//    private val colorSurfaceAlt  = Color.parseColor("#1E2330")
//    private val colorPrimary     = Color.parseColor("#7C6AF7")
//    private val colorPrimaryDim  = Color.parseColor("#4A3FA0")
//    private val colorSecondary   = Color.parseColor("#2EC4B6")
//    private val colorTextPrimary    = Color.parseColor("#F0F2FF")
//    private val colorTextSecondary  = Color.parseColor("#8B91A8")
//    private val colorDivider     = Color.parseColor("#252A3A")
//    private val colorError       = Color.parseColor("#FF6B6B")
//
//    // ── State ─────────────────────────────────────────────────────────────────
//    private lateinit var windowManager: WindowManager
//    private lateinit var bubbleView: FrameLayout
//    private var overlayLayout: LinearLayout? = null
//    private var responseText: TextView? = null
//    private var thinkingDots: TextView? = null
//    private var thinkingHandler: Handler? = null
//    private var thinkingRunnable: Runnable? = null
//
//    // FIX 2 — single scoped coroutine scope for this service lifetime
//    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//
//    companion object {
//        const val CHANNEL_ID      = "sathiai_overlay_channel"
//        const val NOTIFICATION_ID = 1
//    }
//
//    // ── Service lifecycle ─────────────────────────────────────────────────────
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onCreate() {
//        super.onCreate()
//        createNotificationChannel()
//        startForeground(NOTIFICATION_ID, buildNotification())
//        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
//        createBubble()
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return START_STICKY
//    }
//
//    // FIX 1 + FIX 3 — removed restart loop, added serviceScope.cancel()
//    override fun onDestroy() {
//        super.onDestroy()
//        serviceScope.cancel()                               // FIX 3
//        stopThinkingAnimation()
//        if (::bubbleView.isInitialized) {
//            try { windowManager.removeView(bubbleView) } catch (_: Exception) {}
//        }
//        hideOverlay()
//        // FIX 1: restart loop removed — START_STICKY handles this
//    }
//
//    // ── Foreground notification ───────────────────────────────────────────────
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "SathiAI Overlay",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Keeps SathiAI overlay running"
//                setShowBadge(false)
//            }
//            getSystemService(NotificationManager::class.java)
//                .createNotificationChannel(channel)
//        }
//    }
//
//    private fun buildNotification(): Notification {
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle("SathiAI is active")
//            .setContentText("Tap to open")
//            .setSmallIcon(android.R.drawable.ic_dialog_info)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setSilent(true)
//            .setOngoing(true)
//            .build()
//    }
//
//    // ── Floating Bubble ───────────────────────────────────────────────────────
//
//    private fun createBubble() {
//        bubbleView = FrameLayout(this)
//
//        val glowRing = View(this).apply {
//            background = GradientDrawable().apply {
//                shape = GradientDrawable.OVAL
//                setColor(Color.TRANSPARENT)
//                setStroke(3, Color.parseColor("#557C6AF7"))
//            }
//            layoutParams = FrameLayout.LayoutParams(160, 160).apply {
//                gravity = Gravity.CENTER
//            }
//        }
//
//        val bubble = TextView(this).apply {
//            text = "✦"
//            textSize = 22f
//            gravity = Gravity.CENTER
//            setTextColor(colorTextPrimary)
//            typeface = Typeface.DEFAULT_BOLD
//            background = GradientDrawable().apply {
//                shape = GradientDrawable.OVAL
//                colors = intArrayOf(colorPrimary, colorPrimaryDim)
//                gradientType = GradientDrawable.RADIAL_GRADIENT
//                gradientRadius = 70f
//            }
//            elevation = 24f
//            layoutParams = FrameLayout.LayoutParams(140, 140).apply {
//                gravity = Gravity.CENTER
//            }
//        }
//
//        bubbleView.addView(glowRing)
//        bubbleView.addView(bubble)
//
//        ValueAnimator.ofFloat(0.6f, 1f).apply {
//            duration = 2000
//            repeatMode = ValueAnimator.REVERSE
//            repeatCount = ValueAnimator.INFINITE
//            addUpdateListener { glowRing.alpha = it.animatedValue as Float }
//            start()
//        }
//
//        val params = WindowManager.LayoutParams(
//            170, 170,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT
//        ).apply {
//            gravity = Gravity.TOP or Gravity.START
//            x = 80; y = 320
//        }
//
//        windowManager.addView(bubbleView, params)
//        attachBubbleTouchListener(bubble, params)
//    }
//
//    private fun attachBubbleTouchListener(bubble: TextView, params: WindowManager.LayoutParams) {
//        var initialX = 0; var initialY = 0
//        var touchX = 0f; var touchY = 0f
//        var isDragging = false
//
//        bubbleView.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    initialX = params.x; initialY = params.y
//                    touchX = event.rawX; touchY = event.rawY
//                    isDragging = false
//                    bubble.animate().scaleX(0.88f).scaleY(0.88f).setDuration(100).start()
//                    true
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val dx = (event.rawX - touchX).toInt()
//                    val dy = (event.rawY - touchY).toInt()
//                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
//                        isDragging = true
//                        params.x = initialX + dx; params.y = initialY + dy
//                        windowManager.updateViewLayout(bubbleView, params)
//                    }
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    bubble.animate().scaleX(1f).scaleY(1f)
//                        .setInterpolator(OvershootInterpolator()).setDuration(250).start()
//                    if (!isDragging) toggleOverlay()
//                    true
//                }
//                else -> false
//            }
//        }
//    }
//
//    // ── Overlay panel ─────────────────────────────────────────────────────────
//
//    private fun toggleOverlay() {
//        if (overlayLayout == null) showOverlay() else hideOverlay()
//    }
//
//    private fun showOverlay() {
//        val root = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//            background = GradientDrawable().apply {
//                cornerRadius = 32f
//                colors = intArrayOf(Color.parseColor("#F0161A24"), colorSurface)
//                gradientType = GradientDrawable.LINEAR_GRADIENT
//                orientation = GradientDrawable.Orientation.TOP_BOTTOM
//            }
//            elevation = 48f
//            alpha = 0f
//        }
//
//        // Header
//        val header = LinearLayout(this).apply {
//            orientation = LinearLayout.HORIZONTAL
//            gravity = Gravity.CENTER_VERTICAL
//            setPadding(40, 36, 32, 28)
//        }
//        val logoMark = TextView(this).apply {
//            text = "✦"; textSize = 18f
//            setTextColor(colorPrimary)
//            typeface = Typeface.DEFAULT_BOLD
//        }
//        val titleGroup = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//            setPadding(20, 0, 0, 0)
//            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
//        }
//        val titleText = TextView(this).apply {
//            text = "SathiAI"; textSize = 18f
//            typeface = Typeface.DEFAULT_BOLD
//            setTextColor(colorTextPrimary)
//        }
//        val subtitleText = TextView(this).apply {
//            text = "Your on-screen AI assistant"; textSize = 11f
//            setTextColor(colorTextSecondary)
//        }
//        titleGroup.addView(titleText); titleGroup.addView(subtitleText)
//        val closeBtn = TextView(this).apply {
//            text = "✕"; textSize = 16f
//            setTextColor(colorTextSecondary)
//            gravity = Gravity.CENTER
//            setPadding(16, 16, 16, 16)
//            setOnClickListener { hideOverlay() }
//        }
//        header.addView(logoMark); header.addView(titleGroup); header.addView(closeBtn)
//
//        // Divider
//        val divider = View(this).apply {
//            setBackgroundColor(colorDivider)
//            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
//        }
//
//        // Response card
//        val responseCard = LinearLayout(this).apply {
//            orientation = LinearLayout.VERTICAL
//            setPadding(40, 28, 40, 28)
//            background = GradientDrawable().apply {
//                cornerRadius = 20f
//                setColor(Color.parseColor("#0A0D0F14"))
//            }
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            ).apply { setMargins(28, 24, 28, 8) }
//        }
//        val responseLabel = TextView(this).apply {
//            text = "RESPONSE"; textSize = 9f; letterSpacing = 0.15f
//            setTextColor(colorPrimary); setPadding(0, 0, 0, 10)
//        }
//        val localResponseText = TextView(this).apply {
//            text = "Ask something or tap Analyze Screen to begin."
//            textSize = 14f
//            setTextColor(colorTextSecondary)
//            setLineSpacing(0f, 1.5f)
//            minHeight = 80
//        }
//        responseText = localResponseText
//        val localThinkingDots = TextView(this).apply {
//            text = "●  ●  ●"; textSize = 12f
//            setTextColor(colorPrimary)
//            visibility = View.GONE
//            setPadding(0, 8, 0, 0)
//        }
//        thinkingDots = localThinkingDots
//        responseCard.addView(responseLabel)
//        responseCard.addView(localResponseText)
//        responseCard.addView(localThinkingDots)
//
//        // Input row
//        val inputRow = LinearLayout(this).apply {
//            orientation = LinearLayout.HORIZONTAL
//            gravity = Gravity.CENTER_VERTICAL
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            ).apply { setMargins(28, 16, 28, 8) }
//        }
//        val input = EditText(this).apply {
//            hint = "Ask anything…"
//            setHintTextColor(colorTextSecondary)
//            textSize = 14f
//            setTextColor(colorTextPrimary)
//            setPadding(28, 22, 20, 22)
//            background = GradientDrawable().apply {
//                cornerRadius = 24f
//                setColor(colorSurfaceAlt)
//                setStroke(1, colorDivider)
//            }
//            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
//            maxLines = 3
//        }
//        val sendBtn = TextView(this).apply {
//            text = "▶"; textSize = 16f
//            gravity = Gravity.CENTER
//            setTextColor(Color.WHITE)
//            background = GradientDrawable().apply {
//                shape = GradientDrawable.OVAL
//                colors = intArrayOf(colorPrimary, colorPrimaryDim)
//                gradientType = GradientDrawable.LINEAR_GRADIENT
//                orientation = GradientDrawable.Orientation.TOP_BOTTOM
//            }
//            layoutParams = LinearLayout.LayoutParams(108, 108).apply { setMargins(16, 0, 0, 0) }
//            setOnClickListener {
//                val msg = input.text.toString()
//                if (msg.isNotBlank()) {
//                    showThinking("Thinking")
//                    askGroq(msg, localResponseText)
//                    input.setText("")
//                }
//                animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).withEndAction {
//                    animate().scaleX(1f).scaleY(1f)
//                        .setInterpolator(OvershootInterpolator()).setDuration(200).start()
//                }.start()
//            }
//        }
//        inputRow.addView(input); inputRow.addView(sendBtn)
//
//        // Action row
//        val actionRow = LinearLayout(this).apply {
//            orientation = LinearLayout.HORIZONTAL
//            gravity = Gravity.CENTER
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            ).apply { setMargins(28, 8, 28, 32) }
//        }
//
//        val analyzeBtn = buildPillButton("⊙  Analyze Screen", colorSecondary) {
//            // FIX 6 — use cleaned screen text
//            val screenText = ScreenAnalyzer.cleanText(ScreenReaderService.latestScreenText)
//            val appName    = ScreenReaderService.latestAppName
//            if (screenText.isNotBlank()) {
//                showThinking("Analyzing")
//                val prompt = buildString {
//                    append("The user is currently using the app: \"$appName\".\n\n")
//                    append("Here is the text visible on that app's screen:\n$screenText\n\n")
//                    append("Please explain what this app does, what the user is looking at right now, ")
//                    append("and any helpful tips or insights about what they see.")
//                }
//                askGroq(prompt, localResponseText)
//            } else {
//                updateResponse("No screen text detected yet. Enable Accessibility Service first.", isError = true)
//            }
//        }
//
//        val clearBtn = buildPillButton("✕  Clear", colorSurfaceAlt) {
//            localResponseText.text = "Ask something or tap Analyze Screen to begin."
//            localResponseText.setTextColor(colorTextSecondary)
//            stopThinkingAnimation()
//        }
//        actionRow.addView(analyzeBtn); actionRow.addView(clearBtn)
//
//        root.addView(header); root.addView(divider); root.addView(responseCard)
//        root.addView(inputRow); root.addView(actionRow)
//        overlayLayout = root
//
//        val params = WindowManager.LayoutParams(
//            980, WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//            PixelFormat.TRANSLUCENT
//        ).apply { gravity = Gravity.CENTER }
//
//        windowManager.addView(root, params)
//        root.translationY = 30f
//        root.animate().alpha(1f).translationYBy(-30f)
//            .setDuration(320).setInterpolator(DecelerateInterpolator()).start()
//    }
//
//    private fun buildPillButton(label: String, bgColor: Int, onClick: () -> Unit): TextView {
//        return TextView(this).apply {
//            text = label; textSize = 12f
//            setTextColor(if (bgColor == colorSurfaceAlt) colorTextSecondary else Color.WHITE)
//            gravity = Gravity.CENTER
//            setPadding(28, 18, 28, 18)
//            background = GradientDrawable().apply { cornerRadius = 50f; setColor(bgColor) }
//            layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            ).apply { setMargins(8, 0, 8, 0) }
//            setOnClickListener {
//                animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).withEndAction {
//                    animate().scaleX(1f).scaleY(1f)
//                        .setInterpolator(OvershootInterpolator()).setDuration(200).start()
//                }.start()
//                onClick()
//            }
//        }
//    }
//
//    // ── Thinking animation ────────────────────────────────────────────────────
//
//    private fun showThinking(prefix: String) {
//        stopThinkingAnimation()
//        responseText?.text = ""
//        thinkingDots?.visibility = View.VISIBLE
//        var dotCount = 0
//        val dotChars = arrayOf("●", "● ●", "● ● ●")
//        thinkingHandler = Handler(Looper.getMainLooper())
//        thinkingRunnable = object : Runnable {
//            override fun run() {
//                thinkingDots?.text = "$prefix  ${dotChars[dotCount % 3]}"
//                dotCount++
//                thinkingHandler?.postDelayed(this, 450)
//            }
//        }
//        thinkingHandler?.post(thinkingRunnable!!)
//    }
//
//    private fun stopThinkingAnimation() {
//        thinkingRunnable?.let { thinkingHandler?.removeCallbacks(it) }
//        thinkingDots?.visibility = View.GONE
//        thinkingHandler = null; thinkingRunnable = null
//    }
//
//    // ── AI call ───────────────────────────────────────────────────────────────
//    // FIX 4 — use serviceScope instead of random CoroutineScope
//    // FIX 4 — use Handler(main) instead of nested CoroutineScope(Main)
//
//    private fun askGroq(userMessage: String, responseView: TextView) {
//        serviceScope.launch {
//            try {
//                val request = ChatRequest(
//                    model = "llama-3.3-70b-versatile",
//                    messages = listOf(Message(role = "user", content = userMessage))
//                )
//                val response = RetrofitInstance.api.sendMessage(
//                    token = "Bearer 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",
//                    request = request
//                )
//                val reply = response.choices.firstOrNull()?.message?.content ?: "No response"
//
//                Handler(Looper.getMainLooper()).post {
//                    stopThinkingAnimation()
//                    updateResponse(reply, isError = false)
//                }
//
//            } catch (e: Exception) {
//                Handler(Looper.getMainLooper()).post {
//                    stopThinkingAnimation()
//                    updateResponse("Error: ${e.message}", isError = true)
//                }
//            }
//        }
//    }
//
//    private fun updateResponse(text: String, isError: Boolean) {
//        responseText?.apply {
//            setTextColor(if (isError) colorError else colorTextPrimary)
//            this.text = ""
//            val handler = Handler(Looper.getMainLooper())
//            text.toCharArray().forEachIndexed { i, _ ->
//                handler.postDelayed({ this.text = text.substring(0, i + 1) }, (i * 12).toLong())
//            }
//        }
//    }
//
//    // ── Hide overlay ──────────────────────────────────────────────────────────
//
//    private fun hideOverlay() {
//        overlayLayout?.let { layout ->
//            layout.animate().alpha(0f).translationYBy(20f)
//                .setDuration(200).setInterpolator(DecelerateInterpolator())
//                .withEndAction {
//                    try { windowManager.removeView(layout) } catch (_: Exception) {}
//                    overlayLayout = null
//                    responseText = null
//                    thinkingDots = null
//                }.start()
//            stopThinkingAnimation()
//        }

//    }
//}

package com.example.sathiai.overlay

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import android.widget.ScrollView
import androidx.core.app.NotificationCompat
import com.example.sathiai.network.ChatRequest
import com.example.sathiai.network.Message
import com.example.sathiai.network.RetrofitInstance
import com.example.sathiai.services.ScreenAnalyzer          // FIX 6
import com.example.sathiai.services.ScreenReaderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob                     // FIX 2
import kotlinx.coroutines.cancel                            // FIX 3
import kotlinx.coroutines.launch

class OverlayService : Service() {

    // ── Colors ────────────────────────────────────────────────────────────────
    private val colorSurface     = Color.parseColor("#161A24")
    private val colorSurfaceAlt  = Color.parseColor("#1E2330")
    private val colorPrimary     = Color.parseColor("#7C6AF7")
    private val colorPrimaryDim  = Color.parseColor("#4A3FA0")
    private val colorSecondary   = Color.parseColor("#2EC4B6")
    private val colorTextPrimary    = Color.parseColor("#F0F2FF")
    private val colorTextSecondary  = Color.parseColor("#8B91A8")
    private val colorDivider     = Color.parseColor("#252A3A")
    private val colorError       = Color.parseColor("#FF6B6B")

    // ── State ─────────────────────────────────────────────────────────────────
    private lateinit var windowManager: WindowManager
    private lateinit var bubbleView: FrameLayout
    private var overlayLayout: LinearLayout? = null
    private var responseText: TextView? = null
    private var thinkingDots: TextView? = null
    private var responseScroll: ScrollView? = null
    private var thinkingHandler: Handler? = null
    private var thinkingRunnable: Runnable? = null

    // FIX 2 — single scoped coroutine scope for this service lifetime
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID      = "sathiai_overlay_channel"
        const val NOTIFICATION_ID = 1
    }

    // ── Service lifecycle ─────────────────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // FIX 1 + FIX 3 — removed restart loop, added serviceScope.cancel()
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()                               // FIX 3
        stopThinkingAnimation()
        if (::bubbleView.isInitialized) {
            try { windowManager.removeView(bubbleView) } catch (_: Exception) {}
        }
        hideOverlay()
        // FIX 1: restart loop removed — START_STICKY handles this
    }

    // ── Foreground notification ───────────────────────────────────────────────

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
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SathiAI is active")
            .setContentText("Tap to open")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    // ── Floating Bubble ───────────────────────────────────────────────────────

    private fun createBubble() {
        bubbleView = FrameLayout(this)

        val glowRing = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.TRANSPARENT)
                setStroke(3, Color.parseColor("#557C6AF7"))
            }
            layoutParams = FrameLayout.LayoutParams(160, 160).apply {
                gravity = Gravity.CENTER
            }
        }

        val bubble = TextView(this).apply {
            text = "✦"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(colorTextPrimary)
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(colorPrimary, colorPrimaryDim)
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = 70f
            }
            elevation = 24f
            layoutParams = FrameLayout.LayoutParams(140, 140).apply {
                gravity = Gravity.CENTER
            }
        }

        bubbleView.addView(glowRing)
        bubbleView.addView(bubble)

        ValueAnimator.ofFloat(0.6f, 1f).apply {
            duration = 2000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { glowRing.alpha = it.animatedValue as Float }
            start()
        }

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
        attachBubbleTouchListener(bubble, params)
    }

    private fun attachBubbleTouchListener(bubble: TextView, params: WindowManager.LayoutParams) {
        var initialX = 0; var initialY = 0
        var touchX = 0f; var touchY = 0f
        var isDragging = false

        bubbleView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    touchX = event.rawX; touchY = event.rawY
                    isDragging = false
                    bubble.animate().scaleX(0.88f).scaleY(0.88f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
                        isDragging = true
                        params.x = initialX + dx; params.y = initialY + dy
                        windowManager.updateViewLayout(bubbleView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    bubble.animate().scaleX(1f).scaleY(1f)
                        .setInterpolator(OvershootInterpolator()).setDuration(250).start()
                    if (!isDragging) toggleOverlay()
                    true
                }
                else -> false
            }
        }
    }

    // ── Overlay panel ─────────────────────────────────────────────────────────

    private fun toggleOverlay() {
        if (overlayLayout == null) showOverlay() else hideOverlay()
    }

    private fun showOverlay() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = 32f
                colors = intArrayOf(Color.parseColor("#F0161A24"), colorSurface)
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
            }
            elevation = 48f
            alpha = 0f
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(40, 36, 32, 28)
        }
        val logoMark = TextView(this).apply {
            text = "✦"; textSize = 18f
            setTextColor(colorPrimary)
            typeface = Typeface.DEFAULT_BOLD
        }
        val titleGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val titleText = TextView(this).apply {
            text = "SathiAI"; textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorTextPrimary)
        }
        val subtitleText = TextView(this).apply {
            text = "Your on-screen AI assistant"; textSize = 11f
            setTextColor(colorTextSecondary)
        }
        titleGroup.addView(titleText); titleGroup.addView(subtitleText)
        val closeBtn = TextView(this).apply {
            text = "✕"; textSize = 16f
            setTextColor(colorTextSecondary)
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setOnClickListener { hideOverlay() }
        }
        header.addView(logoMark); header.addView(titleGroup); header.addView(closeBtn)

        // Divider
        val divider = View(this).apply {
            setBackgroundColor(colorDivider)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
        }

        // Response card
        val responseCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 28, 40, 28)
            background = GradientDrawable().apply {
                cornerRadius = 20f
                setColor(Color.parseColor("#0A0D0F14"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(28, 24, 28, 8) }
        }
        val responseLabel = TextView(this).apply {
            text = "RESPONSE"; textSize = 9f; letterSpacing = 0.15f
            setTextColor(colorPrimary); setPadding(0, 0, 0, 10)
        }
        val localResponseText = TextView(this).apply {
            text = "Ask something or tap Analyze Screen to begin."
            textSize = 14f
            setTextColor(colorTextSecondary)
            setLineSpacing(0f, 1.5f)
            minHeight = 80
            setTextIsSelectable(true)   // FIX: long-press to copy
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        responseText = localResponseText

        // FIX: fixed-height ScrollView — panel never grows with content
        val responseScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                340   // fixed px height (~180dp) — adjust if needed
            )
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        responseScroll.addView(localResponseText)
        this.responseScroll = responseScroll

        val localThinkingDots = TextView(this).apply {
            text = "●  ●  ●"; textSize = 12f
            setTextColor(colorPrimary)
            visibility = View.GONE
            setPadding(0, 8, 0, 0)
        }
        thinkingDots = localThinkingDots
        responseCard.addView(responseLabel)
        responseCard.addView(responseScroll)      // scroll wraps the text
        responseCard.addView(localThinkingDots)

        // Input row
        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(28, 16, 28, 8) }
        }
        val input = EditText(this).apply {
            hint = "Ask anything…"
            setHintTextColor(colorTextSecondary)
            textSize = 14f
            setTextColor(colorTextPrimary)
            setPadding(28, 22, 20, 22)
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(colorSurfaceAlt)
                setStroke(1, colorDivider)
            }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            maxLines = 3
        }
        val sendBtn = TextView(this).apply {
            text = "▶"; textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(colorPrimary, colorPrimaryDim)
                gradientType = GradientDrawable.LINEAR_GRADIENT
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
            }
            layoutParams = LinearLayout.LayoutParams(108, 108).apply { setMargins(16, 0, 0, 0) }
            setOnClickListener {
                val msg = input.text.toString()
                if (msg.isNotBlank()) {
                    showThinking("Thinking")
                    askGroq(msg, localResponseText)
                    input.setText("")
                }
                animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).withEndAction {
                    animate().scaleX(1f).scaleY(1f)
                        .setInterpolator(OvershootInterpolator()).setDuration(200).start()
                }.start()
            }
        }
        inputRow.addView(input); inputRow.addView(sendBtn)

        // Action row
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(28, 8, 28, 32) }
        }

        val analyzeBtn = buildPillButton("⊙  Analyze Screen", colorSecondary) {
            // FIX 6 — use cleaned screen text
            val screenText = ScreenAnalyzer.cleanText(ScreenReaderService.latestScreenText)
            val appName    = ScreenReaderService.latestAppName
            if (screenText.isNotBlank()) {
                showThinking("Analyzing")
                val prompt = buildString {
                    append("The user is currently using the app: \"$appName\".\n\n")
                    append("Here is the text visible on that app's screen:\n$screenText\n\n")
                    append("Please explain what this app does, what the user is looking at right now, ")
                    append("and any helpful tips or insights about what they see.")
                }
                askGroq(prompt, localResponseText)
            } else {
                updateResponse("No screen text detected yet. Enable Accessibility Service first.", isError = true)
            }
        }

        val clearBtn = buildPillButton("✕  Clear", colorSurfaceAlt) {
            localResponseText.text = "Ask something or tap Analyze Screen to begin."
            localResponseText.setTextColor(colorTextSecondary)
            stopThinkingAnimation()
        }
        actionRow.addView(analyzeBtn); actionRow.addView(clearBtn)

        root.addView(header); root.addView(divider); root.addView(responseCard)
        root.addView(inputRow); root.addView(actionRow)
        overlayLayout = root

        // FIX: fixed panel height — never grows with response content
        val params = WindowManager.LayoutParams(
            980, 1400,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager.addView(root, params)
        root.translationY = 30f
        root.animate().alpha(1f).translationYBy(-30f)
            .setDuration(320).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun buildPillButton(label: String, bgColor: Int, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label; textSize = 12f
            setTextColor(if (bgColor == colorSurfaceAlt) colorTextSecondary else Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(28, 18, 28, 18)
            background = GradientDrawable().apply { cornerRadius = 50f; setColor(bgColor) }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 0, 8, 0) }
            setOnClickListener {
                animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).withEndAction {
                    animate().scaleX(1f).scaleY(1f)
                        .setInterpolator(OvershootInterpolator()).setDuration(200).start()
                }.start()
                onClick()
            }
        }
    }

    // ── Thinking animation ────────────────────────────────────────────────────

    private fun showThinking(prefix: String) {
        stopThinkingAnimation()
        responseText?.text = ""
        thinkingDots?.visibility = View.VISIBLE
        var dotCount = 0
        val dotChars = arrayOf("●", "● ●", "● ● ●")
        thinkingHandler = Handler(Looper.getMainLooper())
        thinkingRunnable = object : Runnable {
            override fun run() {
                thinkingDots?.text = "$prefix  ${dotChars[dotCount % 3]}"
                dotCount++
                thinkingHandler?.postDelayed(this, 450)
            }
        }
        thinkingHandler?.post(thinkingRunnable!!)
    }

    private fun stopThinkingAnimation() {
        thinkingRunnable?.let { thinkingHandler?.removeCallbacks(it) }
        thinkingDots?.visibility = View.GONE
        thinkingHandler = null; thinkingRunnable = null
    }

    // ── AI call ───────────────────────────────────────────────────────────────
    // FIX 4 — use serviceScope instead of random CoroutineScope
    // FIX 4 — use Handler(main) instead of nested CoroutineScope(Main)

    private fun askGroq(userMessage: String, responseView: TextView) {
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

                Handler(Looper.getMainLooper()).post {
                    stopThinkingAnimation()
                    updateResponse(reply, isError = false)
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    stopThinkingAnimation()
                    updateResponse("Error: ${e.message}", isError = true)
                }
            }
        }
    }

    private fun updateResponse(text: String, isError: Boolean) {
        responseText?.apply {
            setTextColor(if (isError) colorError else colorTextPrimary)
            this.text = ""
            val handler = Handler(Looper.getMainLooper())
            val totalMs = (text.length * 12).toLong()
            text.toCharArray().forEachIndexed { i, _ ->
                handler.postDelayed({ this.text = text.substring(0, i + 1) }, (i * 12).toLong())
            }
            // auto-scroll to bottom after typewriter finishes
            handler.postDelayed({
                responseScroll?.fullScroll(ScrollView.FOCUS_DOWN)
            }, totalMs + 50)
        }
    }

    // ── Hide overlay ──────────────────────────────────────────────────────────

    private fun hideOverlay() {
        overlayLayout?.let { layout ->
            layout.animate().alpha(0f).translationYBy(20f)
                .setDuration(200).setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    try { windowManager.removeView(layout) } catch (_: Exception) {}
                    overlayLayout = null
                    responseText = null
                    thinkingDots = null
                    responseScroll = null
                }.start()
            stopThinkingAnimation()
        }
    }
}


