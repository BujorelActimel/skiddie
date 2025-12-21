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

    var onRunStopCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onClearTerminalCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onFocusEditorCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onFocusTerminalCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onNewFileCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onSaveFileCallback by remember { mutableStateOf<() -> Unit>({}) }
    var onOpenFileCallback by remember { mutableStateOf<() -> Unit>({}) }

    var triggerFocusEditor by remember { mutableStateOf(0) }
    var triggerFocusTerminal by remember { mutableStateOf(0) }
    var triggerNewFile by remember { mutableStateOf(0) }
    var triggerSaveFile by remember { mutableStateOf(0) }
    var triggerOpenFile by remember { mutableStateOf(0) }


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

    LaunchedEffect(triggerNewFile) {
        if (triggerNewFile > 0) {
            onNewFileCallback()
        }
    }

    LaunchedEffect(triggerSaveFile) {
        if (triggerSaveFile > 0) {
            onSaveFileCallback()
        }
    }

    LaunchedEffect(triggerOpenFile) {
        if (triggerOpenFile > 0) {
            onOpenFileCallback()
        }
    }


    Window(
        onCloseRequest = ::exitApplication,
        title = windowTitle,
        state = windowState,
        onKeyEvent = { event ->
            val isShortcut = when {
                event.key == Key.L && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.Enter && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.DirectionLeft && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.DirectionRight && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.N && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.S && (event.isCtrlPressed || event.isMetaPressed) -> true
                event.key == Key.O && (event.isCtrlPressed || event.isMetaPressed) -> true
                else -> false
            }

            if (isShortcut) {
                if (event.type == KeyEventType.KeyDown) {
                    when {
                        event.key == Key.L -> onClearTerminalCallback()
                        event.key == Key.Enter -> onRunStopCallback()
                        event.key == Key.DirectionLeft -> triggerFocusEditor++
                        event.key == Key.DirectionRight -> triggerFocusTerminal++
                        event.key == Key.N -> triggerNewFile++
                        event.key == Key.S -> triggerSaveFile++
                        event.key == Key.O -> triggerOpenFile++
                    }
                }
                true
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
                onFocusTerminalShortcut = { callback -> onFocusTerminalCallback = callback },
                onNewFileShortcut = { callback -> onNewFileCallback = callback },
                onSaveFileShortcut = { callback -> onSaveFileCallback = callback },
                onOpenFileShortcut = { callback -> onOpenFileCallback = callback }
            )
        }
    }
}
