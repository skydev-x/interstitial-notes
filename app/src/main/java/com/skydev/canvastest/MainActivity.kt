package com.skydev.canvastest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.skydev.canvastest.ui.feature.notetaking.NoteTakingScreen
import com.skydev.canvastest.ui.feature.notetaking.NoteTakingViewModel
import com.skydev.canvastest.ui.theme.CanvasTestTheme

class MainActivity : ComponentActivity() {
    val viewModel: NoteTakingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NoteTakingScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
