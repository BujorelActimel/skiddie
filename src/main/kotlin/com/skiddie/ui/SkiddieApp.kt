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
import com.skiddie.ui.viewmodel.ExecutionViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun SkiddieApp(
    onTitleChange: (String) -> Unit = {},
    actionFlow: StateFlow<AppAction?>? = null
) {
    val fileManager = remember { FileManager() }
    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var stdinInput by remember { mutableStateOf("") }
    var fileStateVersion by remember { mutableStateOf(0) }

    val availableLanguages = remember { LanguageRegistry.all() }
    var showHelpDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

    val executorScope = rememberCoroutineScope()
    val executionViewModel = remember {
        ExecutionViewModel(
            scope = executorScope,
            fileManager = fileManager,
            onFileStateChange = { fileStateVersion++ }
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

    LaunchedEffect(Unit) {
        editorFocusRequester.requestFocus()
    }

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

    LaunchedEffect(actionFlow) {
        actionFlow?.collect { action ->
            when (action) {
                AppAction.RunStop -> if (isRunning) stopScript() else runScript()
                AppAction.ClearTerminal -> executionViewModel.clearTerminal()
                AppAction.FocusEditor -> editorFocusRequester.requestFocus()
                AppAction.FocusTerminal -> terminalFocusRequester.requestFocus()
                AppAction.NewFile -> {
                    fileOpsHandler.newFile().let { result ->
                        if (result is FileOperationResult.Success) {
                            code = result.content
                            executionViewModel.clearTerminal()
                            fileStateVersion++
                        }
                    }
                }
                AppAction.SaveFile -> {
                    fileOpsHandler.saveFile(code, selectedLanguage?.fileExtension ?: "kts").let { result ->
                        if (result is FileOperationResult.Success) {
                            fileStateVersion++
                        }
                    }
                }
                AppAction.OpenFile -> {
                    fileOpsHandler.openFile().let { result ->
                        if (result is FileOperationResult.Success) {
                            code = result.content
                            executionViewModel.clearTerminal()
                            fileStateVersion++
                        }
                    }
                }
                null -> {/* Ignore null actions */}
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
