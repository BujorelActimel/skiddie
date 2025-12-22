package com.skiddie

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.skiddie.language.LanguageRegistry
import com.skiddie.ui.AppAction
import com.skiddie.ui.SkiddieApp
import com.skiddie.ui.theme.SkiddieTheme
import kotlinx.coroutines.flow.MutableStateFlow

fun main() = application {
    LanguageRegistry.load()

    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    var windowTitle by remember { mutableStateOf("Skiddie - Untitled") }

    val actionFlow = remember { MutableStateFlow<AppAction?>(null) }

    Window(
        onCloseRequest = ::exitApplication,
        title = windowTitle,
        state = windowState,
        onKeyEvent = { event ->
            if (event.type == KeyEventType.KeyDown && (event.isCtrlPressed || event.isMetaPressed)) {
                val action = when (event.key) {
                    Key.L -> AppAction.ClearTerminal
                    Key.Enter -> AppAction.RunStop
                    Key.DirectionLeft -> AppAction.FocusEditor
                    Key.DirectionRight -> AppAction.FocusTerminal
                    Key.N -> AppAction.NewFile
                    Key.S -> AppAction.SaveFile
                    Key.O -> AppAction.OpenFile
                    else -> null
                }

                action?.let {
                    actionFlow.value = it
                    true
                } ?: false
            } else {
                false
            }
        }
    ) {
        SkiddieTheme {
            SkiddieApp(
                onTitleChange = { filePath ->
                    windowTitle = "Skiddie - $filePath"
                },
                actionFlow = actionFlow
            )
        }
    }
}
