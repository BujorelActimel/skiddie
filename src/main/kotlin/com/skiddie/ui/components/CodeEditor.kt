package com.skiddie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    fileName: String = "Untitled",
    dirtyIndicator: String = "",
    onNew: () -> Unit = {},
    onOpen: () -> Unit = {},
    onSave: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(code)) }

    LaunchedEffect(code) {
        if (textFieldValue.text != code) {
            val newSelection = if (code.length >= textFieldValue.selection.start) {
                textFieldValue.selection
            } else {
                TextRange(code.length)
            }
            textFieldValue = TextFieldValue(code, newSelection)
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier.width(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dirtyIndicator,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNew) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "New File",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onOpen) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = "Open File",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onSave) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save File",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))

            val lines = remember(textFieldValue.text) {
                if (textFieldValue.text.isEmpty()) listOf("") else textFieldValue.text.split('\n')
            }
            val lineCount = lines.size
            val lineNumberWidth = remember(lineCount) {
                val digits = maxOf(3, lineCount.toString().length)
                (digits * 13 + 24).dp
            }

            val verticalScrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(lineNumberWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(verticalScrollState, enabled = false) // Sync with editor scroll
                            .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 400.dp) // Match editor padding
                    ) {
                        lines.forEachIndexed { index, _ ->
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                VerticalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    thickness = 1.dp
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = handleTextChange(textFieldValue, newValue)
                            onCodeChange(textFieldValue.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 400.dp)
                            .onPreviewKeyEvent { event ->
                                handleKeyEvent(event, textFieldValue) { newValue ->
                                    textFieldValue = newValue
                                    onCodeChange(newValue.text)
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = EmptyLineVisualTransformation()
                    )
                }
            }
        }
    }
}

class EmptyLineVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformedText = buildAnnotatedString {
            val lines = text.text.split('\n')
            lines.forEachIndexed { index, line ->
                if (line.isEmpty()) {
                    append(" ")
                } else {
                    append(line)
                }
                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var emptyLinesBefore = 0
                val originalText = text.text

                for (i in 0 until offset.coerceAtMost(originalText.length)) {
                    if (i > 0 && originalText[i - 1] == '\n' &&
                        (i >= originalText.length || originalText[i] == '\n')) {
                        emptyLinesBefore++
                    }
                }

                return offset + emptyLinesBefore
            }

            override fun transformedToOriginal(offset: Int): Int {
                var emptyLinesBefore = 0
                val lines = text.text.split('\n')
                var transformedOffset = 0

                for (line in lines) {
                    if (transformedOffset >= offset) break

                    if (line.isEmpty()) {
                        emptyLinesBefore++
                        transformedOffset += 2 // " " + "\n"
                    } else {
                        transformedOffset += line.length + 1 // line + "\n"
                    }
                }

                return (offset - emptyLinesBefore).coerceAtLeast(0)
            }
        }

        return TransformedText(transformedText, offsetMapping)
    }
}

private fun handleTextChange(
    oldValue: TextFieldValue,
    newValue: TextFieldValue
): TextFieldValue {
    return newValue
}

private fun handleKeyEvent(
    event: KeyEvent,
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown) {
        return false
    }

    // TODO: Add keyboard shortcuts here:
    // - Tab: Insert 4 spaces
    // - Shift+Tab: Dedent
    // - Enter: Auto-indentation
    // - Brackets: Auto-closing

    return false
}
