package com.kaixuan.weightloss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kaixuan.weightloss.ui.LoginScreen
import com.kaixuan.weightloss.ui.SettingsScreen
import com.kaixuan.weightloss.ui.WeightScreen
import com.kaixuan.weightloss.ui.theme.WeightLossTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeightLossTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                var showSettings by remember { mutableStateOf(false) }

                when (isLoggedIn) {
                    null -> {
                        // 正在检查登录状态，显示加载指示器
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    false -> {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { }
                        )
                    }
                    true -> {
                        if (showSettings) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { showSettings = false }
                            )
                        } else {
                            WeightScreen(
                                viewModel = viewModel,
                                onSettingsClick = { showSettings = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
