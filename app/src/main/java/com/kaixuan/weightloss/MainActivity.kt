package com.kaixuan.weightloss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.kaixuan.weightloss.ui.WeightScreen
import com.kaixuan.weightloss.ui.theme.WeightLossTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeightLossTheme {
                WeightScreen(viewModel = viewModel)
            }
        }
    }
}