package com.example.sathiai.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sathiai.chat.ChatScreen
import com.example.sathiai.ui.splash.SplashScreen

@Composable
fun AppEntry() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToChat = {
                navController.navigate("chat") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("chat") {
            ChatScreen()
        }
    }
}