package com.skiddie

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.skiddie.language.LanguageRegistry
import com.skiddie.ui.SkiddieApp
import com.skiddie.ui.theme.SkiddieTheme

fun main() = application {
    LanguageRegistry.load()

    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Skiddie",
        state = windowState
    ) {
        SkiddieTheme {
            SkiddieApp()
        }
    }
}
