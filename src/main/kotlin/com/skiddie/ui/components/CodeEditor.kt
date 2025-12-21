package com.skiddie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    focusRequester: FocusRequester? = null,
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
            val internalFocusRequester = remember { FocusRequester() }
            val actualFocusRequester = focusRequester ?: internalFocusRequester

            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(lineNumberWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(verticalScrollState, enabled = false)
                            .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 400.dp)
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
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            actualFocusRequester.requestFocus()
                        }
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
                            .focusRequester(actualFocusRequester)
                            .onPreviewKeyEvent { event ->
                                handleKeyEvent(event, textFieldValue) { newValue ->
                                    textFieldValue = newValue
                                    onCodeChange(newValue.text)
                                }
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

private fun handleTextChange(
    oldValue: TextFieldValue,
    newValue: TextFieldValue
): TextFieldValue {
    if (newValue.text.length == oldValue.text.length + 1 &&
        newValue.selection.collapsed) {

        val insertedPos = newValue.selection.start - 1
        if (insertedPos >= 0 && insertedPos < newValue.text.length) {
            val insertedChar = newValue.text[insertedPos]

            val closingChar = when (insertedChar) {
                '{' -> '}'
                '(' -> ')'
                '[' -> ']'
                '"' -> '"'
                '\'' -> '\''
                else -> null
            }

            if (closingChar != null) {
                val textWithClosing = newValue.text.substring(0, insertedPos + 1) +
                        closingChar +
                        newValue.text.substring(insertedPos + 1)

                return TextFieldValue(
                    text = textWithClosing,
                    selection = TextRange(insertedPos + 1)
                )
            }
        }
    }

    if (newValue.text.length == oldValue.text.length - 1 &&
        newValue.selection.collapsed &&
        oldValue.selection.collapsed) {

        val deletedPos = newValue.selection.start
        if (deletedPos >= 0 && deletedPos < newValue.text.length) {
            val nextChar = newValue.text[deletedPos]
            val deletedChar = if (deletedPos < oldValue.text.length) {
                oldValue.text[deletedPos]
            } else {
                null
            }

            val shouldDeletePair = when {
                deletedChar == '{' && nextChar == '}' -> true
                deletedChar == '(' && nextChar == ')' -> true
                deletedChar == '[' && nextChar == ']' -> true
                deletedChar == '"' && nextChar == '"' -> true
                deletedChar == '\'' && nextChar == '\'' -> true
                else -> false
            }

            if (shouldDeletePair) {
                val textWithoutPair = newValue.text.substring(0, deletedPos) +
                        newValue.text.substring(deletedPos + 1)

                return TextFieldValue(
                    text = textWithoutPair,
                    selection = TextRange(deletedPos)
                )
            }
        }
    }

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


    return when {
        event.key == Key.Tab && !event.isShiftPressed -> {
            val newText = textFieldValue.text.substring(0, textFieldValue.selection.start) +
                    "    " +
                    textFieldValue.text.substring(textFieldValue.selection.end)
            val newCursorPos = textFieldValue.selection.start + 4
            onValueChange(
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursorPos)
                )
            )
            true
        }

        event.key == Key.Tab && event.isShiftPressed -> {
            val text = textFieldValue.text
            val cursorPos = textFieldValue.selection.start

            val lineStart = text.lastIndexOf('\n', cursorPos - 1) + 1

            var spacesToRemove = 0
            for (i in lineStart until minOf(lineStart + 4, text.length)) {
                if (text[i] == ' ') {
                    spacesToRemove++
                } else {
                    break
                }
            }

            if (spacesToRemove > 0) {
                val newText = text.substring(0, lineStart) +
                        text.substring(lineStart + spacesToRemove)
                val newCursorPos = maxOf(lineStart, cursorPos - spacesToRemove)
                onValueChange(
                    TextFieldValue(
                        text = newText,
                        selection = TextRange(newCursorPos)
                    )
                )
            }
            true
        }

        event.key == Key.Enter && !event.isCtrlPressed && !event.isMetaPressed -> {
            val text = textFieldValue.text
            val cursorPos = textFieldValue.selection.start

            val lineStart = text.lastIndexOf('\n', cursorPos - 1) + 1
            val currentLine = text.substring(lineStart, cursorPos)

            val leadingSpaces = currentLine.takeWhile { it == ' ' }.length

            val trimmedLine = currentLine.trimEnd()
            val needsExtraIndent = trimmedLine.isNotEmpty() &&
                (trimmedLine.last() == '{' || trimmedLine.last() == '(' || trimmedLine.last() == '[')

            val indentation = " ".repeat(leadingSpaces + if (needsExtraIndent) 4 else 0)

            val newText = text.substring(0, cursorPos) +
                    "\n" + indentation +
                    text.substring(textFieldValue.selection.end)
            val newCursorPos = cursorPos + 1 + indentation.length

            onValueChange(
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursorPos)
                )
            )
            true
        }

        else -> false
    }
}
