package com.skiddie.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType
import com.skiddie.terminal.AnsiParser
import com.skiddie.terminal.TerminalMode
import com.skiddie.ui.theme.terminalColors

@Composable
fun TerminalPane(
    outputLines: List<OutputLine>,
    stdinInput: String,
    onStdinInputChange: (String) -> Unit,
    onStdinSubmit: () -> Unit,
    onClear: () -> Unit,
    terminalMode: TerminalMode,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Output",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = onClear,
                    enabled = terminalMode == TerminalMode.READ_ONLY
                ) {
                    Text("Clear")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))

            val listState = rememberLazyListState()
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(terminalMode) {
                if (terminalMode == TerminalMode.INTERACTIVE) {
                    focusRequester.requestFocus()
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SelectionContainer {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        if (outputLines.isEmpty() && terminalMode == TerminalMode.READ_ONLY) {
                            item {
                                Text(
                                    "Program output will appear here...",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(
                                count = outputLines.size,
                                key = { index -> "${outputLines[index].timeStamp}-$index" }
                            ) { index ->
                                OutputLineItem(outputLines[index])
                            }

                            if (terminalMode == TerminalMode.INTERACTIVE) {
                                item {
                                    StdinInputField(
                                        value = stdinInput,
                                        onValueChange = onStdinInputChange,
                                        onSubmit = onStdinSubmit,
                                        focusRequester = focusRequester
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(outputLines.size) {
                if (outputLines.isNotEmpty()) {
                    val lastIndex = outputLines.size + (if (terminalMode == TerminalMode.INTERACTIVE) 1 else 0) - 1
                    if (lastIndex >= 0) {
                        listState.scrollToItem(lastIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun StdinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "> ",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.key == Key.Enter &&
                        !event.isShiftPressed &&
                        !event.isCtrlPressed &&
                        !event.isAltPressed &&
                        !event.isMetaPressed) {
                        onSubmit()
                        true
                    } else {
                        false
                    }
                },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun OutputLineItem(line: OutputLine) {
    val terminalColors = MaterialTheme.terminalColors

    val styledText = remember(line.text, line.type, terminalColors) {
        val defaultColor = when (line.type) {
            OutputType.STDOUT -> terminalColors.stdout
            OutputType.STDERR -> terminalColors.stderr
            OutputType.STDIN -> terminalColors.stdin.copy(alpha = 0.6f)
            OutputType.SYSTEM -> terminalColors.system
        }

        val prefix = when (line.type) {
            OutputType.STDIN -> "> "
            OutputType.SYSTEM -> "• "
            else -> ""
        }

        if (prefix.isEmpty()) {
            AnsiParser.parse(line.text, defaultColor, terminalColors)
        } else {
            androidx.compose.ui.text.buildAnnotatedString {
                pushStyle(androidx.compose.ui.text.SpanStyle(color = defaultColor))
                append(prefix)
                pop()
                append(AnsiParser.parse(line.text, defaultColor, terminalColors))
            }
        }
    }

    Text(
        text = styledText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
