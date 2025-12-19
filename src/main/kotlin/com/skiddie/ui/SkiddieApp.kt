package com.skiddie.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType
import com.skiddie.execution.ScriptExecutor
import com.skiddie.file.FileDialogs
import com.skiddie.file.FileManager
import com.skiddie.language.LanguageRegistry
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.OutputPane
import com.skiddie.ui.components.ToolBar
import kotlinx.coroutines.launch

@Composable
fun SkiddieApp(
    onTitleChange: (String) -> Unit = {}
) {
    val fileManager = remember { FileManager() }

    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var outputLines by remember { mutableStateOf<List<OutputLine>>(emptyList()) }
    var stdinInput by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var fileStateVersion by remember { mutableStateOf(0) } // Trigger recomposition when file state changes

    val availableLanguages = remember { LanguageRegistry.all() }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

    var executor by remember { mutableStateOf<ScriptExecutor?>(null) }
    val executorScope = rememberCoroutineScope()

    LaunchedEffect(code, fileStateVersion) {
        fileManager.markDirty(code)
        val path = fileManager.getFullDisplayPath()
        val indicator = fileManager.getDirtyIndicator()
        onTitleChange("$path$indicator")
    }

    val fileName = remember(fileStateVersion, code) {
        fileManager.markDirty(code)
        fileManager.getDisplayName()
    }

    val dirtyIndicator = remember(fileStateVersion, code) {
        fileManager.markDirty(code)
        fileManager.getDirtyIndicator()
    }

    val onStdinSubmit: () -> Unit = {
        if (stdinInput.isNotEmpty() && isRunning) {
            executor?.sendInput(stdinInput)
            stdinInput = ""
        }
    }

    val onClearOutput: () -> Unit = {
        outputLines = emptyList()
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
                onRun = {
                    selectedLanguage?.let { language ->
                        try {
                            outputLines = emptyList()
                            isRunning = true

                            if (fileManager.hasFile() && fileManager.isDirty()) {
                                try {
                                    fileManager.save(code)
                                    fileStateVersion++
                                } catch (e: Exception) {
                                    outputLines = listOf(
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
                                        outputLines = outputLines + line
                                    },
                                    onComplete = { result ->
                                        isRunning = false
                                        tempFile.delete()
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            outputLines = listOf(
                                OutputLine("Failed to start: ${e.message}", OutputType.STDERR)
                            )
                            isRunning = false
                        }
                    } ?: run {
                        outputLines = listOf(
                            OutputLine("No language selected", OutputType.SYSTEM)
                        )
                    }
                },
                onStop = {
                    executor?.stop()
                    isRunning = false
                },
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
                    onNew = {
                        code = fileManager.new()
                        outputLines = emptyList()
                        fileStateVersion++
                    },
                    onOpen = {
                        FileDialogs.openFile(title = "Open Script")?.let { file ->
                            try {
                                code = fileManager.open(file.absolutePath)
                                outputLines = emptyList()
                                fileStateVersion++
                            } catch (e: Exception) {
                                outputLines = listOf(
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
                            outputLines = listOf(
                                OutputLine("Error saving file: ${e.message}", OutputType.STDERR)
                            )
                        }
                    },
                    modifier = Modifier.weight(0.65f).fillMaxHeight()
                )

                VerticalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))

                OutputPane(
                    outputLines = outputLines,
                    stdinInput = stdinInput,
                    onStdinInputChange = { stdinInput = it },
                    onStdinSubmit = onStdinSubmit,
                    onClear = onClearOutput,
                    canSendInput = isRunning,
                    modifier = Modifier.weight(0.35f).fillMaxHeight()
                )
            }
        }
    }
}
