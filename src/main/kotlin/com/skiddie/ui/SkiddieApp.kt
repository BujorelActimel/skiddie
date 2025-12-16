package com.skiddie.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.skiddie.ui.components.CodeEditor
import com.skiddie.ui.components.OutputPane

@Composable
fun SkiddieApp() {
    var code by remember { mutableStateOf("// Write your Kotlin code here\n\nfun main() {\n    println(\"Hello, Skiddie!\")\n}") }
    var output by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
