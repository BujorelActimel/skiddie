package com.skiddie.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType
import com.skiddie.execution.ScriptExecutor
import com.skiddie.file.FileDialogs
import com.skiddie.file.FileManager
import com.skiddie.language.LanguageRegistry
import com.skiddie.terminal.TerminalBuffer
import com.skiddie.terminal.TerminalMode
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.TerminalPane
import com.skiddie.ui.components.ToolBar
import kotlinx.coroutines.launch

@Composable
fun SkiddieApp(
    onTitleChange: (String) -> Unit = {},
    onRunStopShortcut: (() -> Unit) -> Unit = {},
    onClearTerminalShortcut: (() -> Unit) -> Unit = {},
    onFocusEditorShortcut: (() -> Unit) -> Unit = {},
    onFocusTerminalShortcut: (() -> Unit) -> Unit = {}
) {
    val fileManager = remember { FileManager() }

    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    val terminalBuffer = remember { TerminalBuffer(maxLines = 10000) }
    var terminalMode by remember { mutableStateOf(TerminalMode.READ_ONLY) }
    var stdinInput by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var fileStateVersion by remember { mutableStateOf(0) } // Trigger recomposition when file state changes

    val availableLanguages = remember { LanguageRegistry.all() }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

    var executor by remember { mutableStateOf<ScriptExecutor?>(null) }
    val executorScope = rememberCoroutineScope()

    // Focus requesters for editor and terminal (for programmatic focus switching)
    val editorFocusRequester = remember { FocusRequester() }
    val terminalFocusRequester = remember { FocusRequester() }

    LaunchedEffect(code, fileStateVersion) {
        fileManager.markDirty(code)
        val path = fileManager.getFullDisplayPath()
        val indicator = fileManager.getDirtyIndicator()
        onTitleChange("$path$indicator")
    }

    val fileName = remember(fileStateVersion) {
        fileManager.getDisplayName()
    }

    val dirtyIndicator = remember(fileStateVersion) {
        fileManager.getDirtyIndicator()
    }

    val onStdinSubmit: () -> Unit = {
        if (stdinInput.isNotEmpty() && isRunning) {
            // Capture the input value before clearing it
            val inputToSend = stdinInput
            stdinInput = ""
            executorScope.launch {
                executor?.sendInput(inputToSend)
            }
        }
    }

    val onClearOutput: () -> Unit = {
        terminalBuffer.clear()
    }

    val runScript: () -> Unit = {
        selectedLanguage?.let { language ->
            try {
                terminalBuffer.clear()
                terminalMode = TerminalMode.INTERACTIVE
                isRunning = true

                if (fileManager.hasFile() && fileManager.isDirty()) {
                    try {
                        fileManager.save(code)
                        fileStateVersion++
                    } catch (e: Exception) {
                        terminalBuffer.addLine(
                            OutputLine("Warning: Failed to autosave before run: ${e.message}", OutputType.SYSTEM)
                        )
                    }
                }

                val tempFile = fileManager.getTempFileForExecution(
                    content = code,
                    extension = language.fileExtension
                )

                if (executor == null) {
                    executor = ScriptExecutor(executorScope)
                }

                val command = language.runCommand.replace("{file}", tempFile.absolutePath)

                executorScope.launch {
                    executor?.execute(
                        command = command,
                        workingDirectory = tempFile.parentFile,
                        onOutputLine = { line ->
                            terminalBuffer.addLine(line)
                        },
                        onComplete = { result ->
                            isRunning = false
                            terminalMode = TerminalMode.READ_ONLY
                            tempFile.delete()
                        }
                    )
                }
            } catch (e: Exception) {
                terminalBuffer.clear()
                terminalBuffer.addLine(
                    OutputLine("Failed to start: ${e.message}", OutputType.STDERR)
                )
                terminalMode = TerminalMode.READ_ONLY
                isRunning = false
            }
        }
    }

    val stopScript: () -> Unit = {
        executor?.stop()
        isRunning = false
        terminalMode = TerminalMode.READ_ONLY
    }

    // Register callbacks for global shortcuts
    LaunchedEffect(Unit) {
        onRunStopShortcut {
            if (isRunning) stopScript() else runScript()
        }
        onClearTerminalShortcut {
            terminalBuffer.clear()
        }
        onFocusEditorShortcut {
            editorFocusRequester.requestFocus()
        }
        onFocusTerminalShortcut {
            terminalFocusRequester.requestFocus()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ToolBar(
                selectedLanguage = selectedLanguage,
                availableLanguages = availableLanguages,
                onLanguageSelected = { selectedLanguage = it },
                onRun = runScript,
                onStop = stopScript,
                onHelp = {
                    // TODO: Show help dialog
                    println("Help clicked")
                },
                isRunning = isRunning
            )

            Row(modifier = Modifier.fillMaxSize()) {
                CodeEditor(
                    code = code,
                    onCodeChange = { code = it },
                    fileName = fileName,
                    dirtyIndicator = dirtyIndicator,
                    focusRequester = editorFocusRequester,
                    onNew = {
                        code = fileManager.new()
                        terminalBuffer.clear()
                        fileStateVersion++
                    },
                    onOpen = {
                        FileDialogs.openFile(title = "Open Script")?.let { file ->
                            try {
                                code = fileManager.open(file.absolutePath)
                                terminalBuffer.clear()
                                fileStateVersion++
                            } catch (e: Exception) {
                                terminalBuffer.clear()
                                terminalBuffer.addLine(
                                    OutputLine("Error opening file: ${e.message}", OutputType.STDERR)
                                )
                            }
                        }
                    },
                    onSave = {
                        try {
                            if (fileManager.hasFile()) {
                                fileManager.save(code)
                                fileStateVersion++
                            } else {
                                FileDialogs.saveFile(
                                    title = "Save Script",
                                    defaultName = "untitled.${selectedLanguage?.fileExtension ?: "kts"}"
                                )?.let { file ->
                                    fileManager.save(code, file.absolutePath)
                                    fileStateVersion++
                                }
                            }
                        } catch (e: Exception) {
                            terminalBuffer.clear()
                            terminalBuffer.addLine(
                                OutputLine("Error saving file: ${e.message}", OutputType.STDERR)
                            )
                        }
                    },
                    modifier = Modifier.weight(0.65f).fillMaxHeight()
                )

                VerticalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))

                TerminalPane(
                    outputLines = terminalBuffer.getLines(),
                    stdinInput = stdinInput,
                    onStdinInputChange = { stdinInput = it },
                    onStdinSubmit = onStdinSubmit,
                    onClear = onClearOutput,
                    terminalMode = terminalMode,
                    focusRequester = terminalFocusRequester,
                    modifier = Modifier.weight(0.35f).fillMaxHeight()
                )
            }
        }
    }
}
