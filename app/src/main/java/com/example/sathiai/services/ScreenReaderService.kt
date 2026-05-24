package com.example.sathiai.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ScreenReaderService : AccessibilityService() {

    companion object {
        /** The visible text from the foreground app's screen (never SathiAI itself). */
        var latestScreenText: String = ""
            private set

        /** Human-readable name of the foreground app, e.g. "YouTube", "Chrome". */
        var latestAppName: String = "Unknown App"
            private set

        /** Package name of the foreground app. */
        var latestPackageName: String = ""
            private set

        // SathiAI's own package — we never read ourselves
        private const val OWN_PACKAGE = "com.example.sathiai"
    }

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 300
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val pkg = event.packageName?.toString() ?: return

        // ── Ignore our own overlay completely ────────────────────────────────
        if (pkg == OWN_PACKAGE) return

        // ── Update foreground app info on window switch ──────────────────────
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            latestPackageName = pkg
            latestAppName     = resolveAppName(pkg)
        }

        // ── Only scrape text from the currently tracked foreground app ───────
        if (pkg != latestPackageName) return

        val root = rootInActiveWindow ?: return
        val sb   = StringBuilder()
        scrapeText(root, sb)
        val scraped = sb.toString().trim()

        if (scraped.isNotBlank()) {
            latestScreenText = scraped
        }
    }

    /** Recursively walk the node tree and collect all visible text. */
    private fun scrapeText(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        node ?: return

        val text        = node.text?.toString()?.trim()
        val contentDesc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank()) {
            sb.append(text).append("\n")
        } else if (!contentDesc.isNullOrBlank()) {
            // Fall back to content description (useful for icon buttons, images)
            sb.append(contentDesc).append("\n")
        }

        for (i in 0 until node.childCount) {
            scrapeText(node.getChild(i), sb)
        }
    }

    /** Resolve a package name to a human-readable app label. */
    private fun resolveAppName(packageName: String): String {
        return try {
            val pm: PackageManager = applicationContext.packageManager
            val info: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // Fall back to the last segment of the package name
            packageName.substringAfterLast(".")
                .replaceFirstChar { it.uppercaseChar() }
        }
    }

    override fun onInterrupt() {
        latestScreenText  = ""
        latestAppName     = "Unknown App"
        latestPackageName = ""
    }
}