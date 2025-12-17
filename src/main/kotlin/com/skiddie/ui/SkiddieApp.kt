package com.skiddie.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.skiddie.language.LanguageRegistry
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.OutputPane
import com.skiddie.ui.components.ToolBar

@Composable
fun SkiddieApp() {
    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var output by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }

    val availableLanguages = remember { LanguageRegistry.all() }
    var selectedLanguage by remember { mutableStateOf(availableLanguages.firstOrNull()) }

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
