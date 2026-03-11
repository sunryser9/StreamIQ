package com.streamiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.streamiq.ui.navigation.StreamIQNavHost
import com.streamiq.ui.theme.StreamIQTheme
import com.streamiq.ui.viewmodel.StreamIQViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StreamIQViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            StreamIQTheme(isDark = uiState.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    StreamIQNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        onToggleTheme = { viewModel.toggleTheme() }
                    )
                }
            }
        }
    }
}
