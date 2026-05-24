package com.example.sathiai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sathiai.overlay.OverlayService

/**
 * Starts OverlayService automatically on device boot
 * and when the app is updated / reinstalled.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val trigger = intent.action
        if (
            trigger == Intent.ACTION_BOOT_COMPLETED ||
            trigger == Intent.ACTION_MY_PACKAGE_REPLACED ||
            trigger == "android.intent.action.QUICKBOOT_POWERON"   // HTC / some OEMs
        ) {
            val serviceIntent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}