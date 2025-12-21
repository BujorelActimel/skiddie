package com.skiddie.highlighting

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class SyntaxHighlightTransformation(
    private val highlighter: SyntaxHighlighter
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = highlighter.highlight(text.text)

        return TransformedText(
            text = highlighted,
            offsetMapping = OffsetMapping.Identity
        )
    }
}
