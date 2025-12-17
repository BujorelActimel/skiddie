package com.skiddie.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.skiddie.file.FileDialogs
import com.skiddie.file.FileManager
import com.skiddie.language.LanguageRegistry
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.OutputPane
import com.skiddie.ui.components.ToolBar

@Composable
fun SkiddieApp(
    onTitleChange: (String) -> Unit = {}
) {
    val fileManager = remember { FileManager() }

    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var output by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var fileStateVersion by remember { mutableStateOf(0) } // Trigger recomposition when file state changes

    val availableLanguages = remember { LanguageRegistry.all() }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

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
                    // TODO: Execute script
                    isRunning = true
                    output = "Running script..."
                },
                onStop = {
                    // TODO: Stop execution
                    isRunning = false
                    output += "\nStopped."
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
                        output = ""
                        fileStateVersion++
                    },
                    onOpen = {
                        FileDialogs.openFile(title = "Open Script")?.let { file ->
                            try {
                                code = fileManager.open(file.absolutePath)
                                output = ""
                                fileStateVersion++
                            } catch (e: Exception) {
                                output = "Error opening file: ${e.message}"
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
                            output = "Error saving file: ${e.message}"
                        }
                    },
                    modifier = Modifier.weight(0.65f).fillMaxHeight()
                )

                OutputPane(
                    output = output,
                    modifier = Modifier.weight(0.35f).fillMaxHeight()
                )
            }
        }
    }
}
