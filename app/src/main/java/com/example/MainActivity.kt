package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.BlockerScreen
import com.example.ui.BlockerViewModel
import com.example.ui.theme.ShortsBlockerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BlockerViewModel by viewModels {
        BlockerViewModel.factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShortsBlockerTheme {
                BlockerScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAccessibilityStatus(this)
    }
}
