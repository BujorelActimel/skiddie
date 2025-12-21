package com.skiddie

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
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
    var windowTitle by remember { mutableStateOf("Skiddie - Untitled") }

    // Callbacks for global shortcuts
    var onRunStopCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onClearTerminalCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onFocusEditorCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onFocusTerminalCallback by remember { mutableStateOf<() -> Unit>({}) }

    // Trigger flags for deferred actions
    var triggerFocusEditor by remember { mutableStateOf(0) }
    var triggerFocusTerminal by remember { mutableStateOf(0) }

    // Defer focus changes to avoid invalidating focus system during key events
    LaunchedEffect(triggerFocusEditor) {
        if (triggerFocusEditor > 0) {
            onFocusEditorCallback()
        }
    }

    LaunchedEffect(triggerFocusTerminal) {
        if (triggerFocusTerminal > 0) {
            onFocusTerminalCallback()
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = windowTitle,
        state = windowState,
        onKeyEvent = { event ->
            // Handle global shortcuts
            val isShortcut = when {
                event.key == Key.L && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.Enter && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.DirectionLeft && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.DirectionRight && (event.isCtrlPressed || event.isMetaPressed) -> true
                else -> false
            }

            if (isShortcut) {
                if (event.type == KeyEventType.KeyDown) {
                    when {
                        event.key == Key.L -> onClearTerminalCallback()
                        event.key == Key.Enter -> onRunStopCallback()
                        event.key == Key.DirectionLeft -> triggerFocusEditor++
                        event.key == Key.DirectionRight -> triggerFocusTerminal++
                    }
                }
                true // Consume both KeyDown and KeyUp
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
                onRunStopShortcut = { callback -> onRunStopCallback = callback },
                onClearTerminalShortcut = { callback -> onClearTerminalCallback = callback },
                onFocusEditorShortcut = { callback -> onFocusEditorCallback = callback },
                onFocusTerminalShortcut = { callback -> onFocusTerminalCallback = callback }
            )
        }
    }
}
