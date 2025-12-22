package com.skiddie.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.*
import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType
import com.skiddie.execution.ScriptExecutor
import com.skiddie.file.FileDialogs
import com.skiddie.file.FileManager
import com.skiddie.file.FileOperationsHandler
import com.skiddie.file.FileOperationResult
import com.skiddie.language.LanguageRegistry
import com.skiddie.terminal.TerminalBuffer
import com.skiddie.terminal.TerminalMode
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.TerminalPane
import com.skiddie.ui.components.ToolBar
import com.skiddie.ui.components.HelpDialog
import kotlinx.coroutines.launch

@Composable
fun SkiddieApp(
    onTitleChange: (String) -> Unit = {},
    onRunStopShortcut: (() -> Unit) -> Unit = {},
    onClearTerminalShortcut: (() -> Unit) -> Unit = {},
    onFocusEditorShortcut: (() -> Unit) -> Unit = {},
    onFocusTerminalShortcut: (() -> Unit) -> Unit = {},
    onNewFileShortcut: (() -> Unit) -> Unit = {},
    onSaveFileShortcut: (() -> Unit) -> Unit = {},
    onOpenFileShortcut: (() -> Unit) -> Unit = {}
) {
    val fileManager = remember { FileManager() }
    val terminalBuffer = remember { TerminalBuffer(maxLines = 10000) }
    val fileOpsHandler = remember {
        FileOperationsHandler(
            fileManager = fileManager,
            onError = { errorMessage ->
                terminalBuffer.clear()
                terminalBuffer.addLine(OutputLine(errorMessage, OutputType.STDERR))
            }
        )
    }

    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var terminalMode by remember { mutableStateOf(TerminalMode.READ_ONLY) }
    var stdinInput by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var fileStateVersion by remember { mutableStateOf(0) }

    val availableLanguages = remember { LanguageRegistry.all() }
    var showHelpDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

    var executor by remember { mutableStateOf<ScriptExecutor?>(null) }
    val executorScope = rememberCoroutineScope()

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
        onNewFileShortcut {
            fileOpsHandler.newFile().let { result ->
                if (result is FileOperationResult.Success) {
                    code = result.content
                    terminalBuffer.clear()
                    fileStateVersion++
                }
            }
        }

        onSaveFileShortcut {
            fileOpsHandler.saveFile(code, selectedLanguage?.fileExtension ?: "kts").let { result ->
                if (result is FileOperationResult.Success) {
                    fileStateVersion++
                }
            }
        }

        onOpenFileShortcut {
            fileOpsHandler.openFile().let { result ->
                if (result is FileOperationResult.Success) {
                    code = result.content
                    terminalBuffer.clear()
                    fileStateVersion++
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {
                            event.key == Key.Enter && (event.isCtrlPressed || event.isMetaPressed) -> {
                                if (isRunning) stopScript() else runScript()
                                true
                            }
                            event.key == Key.L && (event.isCtrlPressed || event.isMetaPressed) -> {
                                terminalBuffer.clear()
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
        ) {
            ToolBar(
                selectedLanguage = selectedLanguage,
                availableLanguages = availableLanguages,
                onLanguageSelected = { selectedLanguage = it },
                onRun = runScript,
                onStop = stopScript,
                onHelp = { showHelpDialog = true },
                isRunning = isRunning
            )

            Row(modifier = Modifier.fillMaxSize()) {
                CodeEditor(
                    code = code,
                    onCodeChange = { code = it },
                    selectedLanguage = selectedLanguage,
                    fileName = fileName,
                    dirtyIndicator = dirtyIndicator,
                    focusRequester = editorFocusRequester,
                    onNew = {
                        fileOpsHandler.newFile().let { result ->
                            if (result is FileOperationResult.Success) {
                                code = result.content
                                terminalBuffer.clear()
                                fileStateVersion++
                            }
                        }
                    },
                    onOpen = {
                        fileOpsHandler.openFile().let { result ->
                            if (result is FileOperationResult.Success) {
                                code = result.content
                                terminalBuffer.clear()
                                fileStateVersion++
                            }
                        }
                    },
                    onSave = {
                        fileOpsHandler.saveFile(code, selectedLanguage?.fileExtension ?: "kts").let { result ->
                            if (result is FileOperationResult.Success) {
                                fileStateVersion++
                            }
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
        if (showHelpDialog) {
            HelpDialog(onDismiss = { showHelpDialog = false })
        }
    }
}
