package com.apprenova.renovacheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apprenova.renovacheck.navigation.RenovaNavHost
import com.apprenova.renovacheck.ui.theme.RenovaCheckTheme
import com.apprenova.renovacheck.viewmodel.RenovaViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: RenovaViewModel = viewModel()
            val settings = vm.appSettings
            val darkTheme = settings.darkMode || isSystemInDarkTheme()

            RenovaCheckTheme(darkTheme = darkTheme) {
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !darkTheme
                    )
                }

                RenovaNavHost(viewModel = vm)
            }
        }
    }
}
