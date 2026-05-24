package com.example.sathiai

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sathiai.overlay.OverlayService
import com.example.sathiai.ui.AppEntry

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        checkOverlayPermission()

        setContent {

            MaterialTheme {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {

                    AppEntry()
                }
            }
        }
    }

    private fun checkOverlayPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                startActivity(intent)

            } else {

                startOverlayService()
            }

        } else {

            startOverlayService()
        }
    }

    private fun startOverlayService() {

        val serviceIntent =
            Intent(this, OverlayService::class.java)

        startService(serviceIntent)
    }

    override fun onResume() {

        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (Settings.canDrawOverlays(this)) {

                startOverlayService()
            }
        }
    }
}