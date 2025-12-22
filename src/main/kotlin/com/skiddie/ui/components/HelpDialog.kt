package com.skiddie.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Help", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HelpSection(
                    title = "File Operations",
                    shortcuts = listOf(
                        "Cmd/Ctrl + N" to "New file",
                        "Cmd/Ctrl + O" to "Open file",
                        "Cmd/Ctrl + S" to "Save file"
                    )
                )

                HelpSection(
                    title = "Execution",
                    shortcuts = listOf(
                        "Cmd/Ctrl + Enter" to "Run or stop script",
                        "Run button" to "Execute current script",
                        "Stop button" to "Terminate running script"
                    )
                )

                HelpSection(
                    title = "Editor",
                    shortcuts = listOf(
                        "Tab" to "Indent (add 4 spaces)",
                        "Shift + Tab" to "Unindent (remove up to 4 spaces)",
                        "Enter" to "New line with auto-indent",
                        "Auto-pair" to "Automatically close brackets and quotes"
                    )
                )

                HelpSection(
                    title = "Navigation",
                    shortcuts = listOf(
                        "Cmd/Ctrl + Left" to "Focus editor",
                        "Cmd/Ctrl + Right" to "Focus terminal"
                    )
                )

                HelpSection(
                    title = "Terminal",
                    shortcuts = listOf(
                        "Cmd/Ctrl + L" to "Clear terminal output",
                        "Enter" to "Send input to running process"
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        modifier = Modifier.width(500.dp)
    )
}

@Composable
private fun HelpSection(title: String, shortcuts: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        shortcuts.forEach { (shortcut, description) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = shortcut,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
