package com.skiddie.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skiddie.language.Language

@Composable
fun ToolBar(
    selectedLanguage: Language?,
    availableLanguages: List<Language>,
    onLanguageSelected: (Language) -> Unit,
    onRun: () -> Unit,
    onStop: () -> Unit,
    onHelp: () -> Unit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Skiddie",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    availableLanguages = availableLanguages,
                    onLanguageSelected = onLanguageSelected,
                    enabled = !isRunning
                )

                Button(
                    onClick = onRun,
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFb8bb26)
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run")
                }

                Button(
                    onClick = onStop,
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onHelp) {
                Icon(
                    Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: Language?,
    availableLanguages: List<Language>,
    onLanguageSelected: (Language) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = selectedLanguage?.name ?: "Select Language",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("▼", style = MaterialTheme.typography.bodyMedium)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableLanguages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("+ Add Language...") },
                onClick = {
                    expanded = false
                    // TODO: Open add language dialog
                }
            )
        }
    }
}
