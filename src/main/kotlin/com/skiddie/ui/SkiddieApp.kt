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
import com.skiddie.file.FileStateManager
import com.skiddie.language.LanguageRegistry
import com.skiddie.terminal.TerminalBuffer
import com.skiddie.terminal.TerminalMode
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.TerminalPane
import com.skiddie.ui.components.ToolBar
import com.skiddie.ui.components.HelpDialog
import com.skiddie.ui.viewmodel.ExecutionViewModel
import kotlinx.coroutines.launch

@Composable
fun SkiddieApp(
    onTitleChange: (String) -> Unit = {}
) {
    val fileManager = remember { FileManager() }
    val fileStateManager = remember { FileStateManager(fileManager) }
    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var stdinInput by remember { mutableStateOf("") }

    val availableLanguages = remember { LanguageRegistry.all() }
    var showHelpDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

    val executorScope = rememberCoroutineScope()
    val executionViewModel = remember {
        ExecutionViewModel(
            scope = executorScope,
            fileManager = fileManager,
            onFileStateChange = { fileStateManager.refresh() }
        )
    }

    val fileOpsHandler = remember(executionViewModel) {
        FileOperationsHandler(
            fileManager = fileManager,
            onError = { errorMessage ->
                executionViewModel.terminalBuffer.clear()
                executionViewModel.terminalBuffer.addLine(OutputLine(errorMessage, OutputType.STDERR))
            }
        )
    }

    val isRunning by executionViewModel.isRunning.collectAsState()
    val terminalMode by executionViewModel.terminalMode.collectAsState()
    val terminalBuffer = executionViewModel.terminalBuffer

    val editorFocusRequester = remember { FocusRequester() }
    val terminalFocusRequester = remember { FocusRequester() }

    val fileState by fileStateManager.state.collectAsState()

    LaunchedEffect(Unit) {
        editorFocusRequester.requestFocus()
    }

    LaunchedEffect(code) {
        fileStateManager.updateContent(code)
        onTitleChange("${fileState.fullPath}${fileState.dirtyIndicator}")
    }

    val onStdinSubmit: () -> Unit = {
        if (stdinInput.isNotEmpty()) {
            val inputToSend = stdinInput
            stdinInput = ""
            executionViewModel.sendInput(inputToSend)
        }
    }

    val onClearOutput: () -> Unit = {
        executionViewModel.clearTerminal()
    }

    val runScript: () -> Unit = {
        selectedLanguage?.let { language ->
            executionViewModel.runScript(code, language)
        }
    }

    val stopScript: () -> Unit = {
        executionViewModel.stopScript()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && (event.isCtrlPressed || event.isMetaPressed)) {
                        when (event.key) {
                            Key.Enter -> {
                                if (isRunning) stopScript() else runScript()
                                true
                            }
                            Key.L -> {
                                executionViewModel.clearTerminal()
                                true
                            }
                            Key.DirectionLeft -> {
                                editorFocusRequester.requestFocus()
                                true
                            }
                            Key.DirectionRight -> {
                                terminalFocusRequester.requestFocus()
                                true
                            }
                            Key.N -> {
                                fileOpsHandler.newFile().let { result ->
                                    if (result is FileOperationResult.Success) {
                                        code = result.content
                                        executionViewModel.clearTerminal()
                                        fileStateManager.refresh()
                                    }
                                }
                                true
                            }
                            Key.S -> {
                                fileOpsHandler.saveFile(code, selectedLanguage?.fileExtension ?: "kts").let { result ->
                                    if (result is FileOperationResult.Success) {
                                        fileStateManager.markSaved()
                                    }
                                }
                                true
                            }
                            Key.O -> {
                                fileOpsHandler.openFile().let { result ->
                                    if (result is FileOperationResult.Success) {
                                        code = result.content
                                        executionViewModel.clearTerminal()
                                        fileStateManager.refresh()
                                    }
                                }
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
                    fileName = fileState.displayName,
                    dirtyIndicator = fileState.dirtyIndicator,
                    focusRequester = editorFocusRequester,
                    onNew = {
                        fileOpsHandler.newFile().let { result ->
                            if (result is FileOperationResult.Success) {
                                code = result.content
                                terminalBuffer.clear()
                                fileStateManager.refresh()
                            }
                        }
                    },
                    onOpen = {
                        fileOpsHandler.openFile().let { result ->
                            if (result is FileOperationResult.Success) {
                                code = result.content
                                terminalBuffer.clear()
                                fileStateManager.refresh()
                            }
                        }
                    },
                    onSave = {
                        fileOpsHandler.saveFile(code, selectedLanguage?.fileExtension ?: "kts").let { result ->
                            if (result is FileOperationResult.Success) {
                                fileStateManager.markSaved()
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
