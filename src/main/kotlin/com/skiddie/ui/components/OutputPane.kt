package com.skiddie.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType

@Composable
fun OutputPane(
    outputLines: List<OutputLine>,
    stdinInput: String,
    onStdinInputChange: (String) -> Unit,
    onStdinSubmit: () -> Unit,
    onClear: () -> Unit,
    canSendInput: Boolean,
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

                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            val scrollState = rememberScrollState()
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(canSendInput) {
                if (canSendInput) {
                    focusRequester.requestFocus()
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (outputLines.isEmpty() && !canSendInput) {
                        Text(
                            "Program output will appear here...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        outputLines.forEach { line ->
                            OutputLineItem(line)
                        }

                        if (canSendInput) {
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "> ",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                BasicTextField(
                                    value = stdinInput,
                                    onValueChange = onStdinInputChange,
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
                                                onStdinSubmit()
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
                    }
                }
            }

            LaunchedEffect(outputLines.size, stdinInput) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }
    }
}

@Composable
private fun OutputLineItem(line: OutputLine) {
    val color = when (line.type) {
        OutputType.STDOUT -> MaterialTheme.colorScheme.onSurface
        OutputType.STDERR -> Color(0xFFfb4934)
        OutputType.STDIN -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        OutputType.SYSTEM -> Color(0xFF83a598)
    }

    val prefix = when (line.type) {
        OutputType.STDIN -> "> "
        OutputType.SYSTEM -> "• "
        else -> ""
    }

    Text(
        text = prefix + line.text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
