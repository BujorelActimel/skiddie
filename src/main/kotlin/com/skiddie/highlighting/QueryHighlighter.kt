package com.skiddie.highlighting

import androidx.compose.ui.graphics.Color

class QueryHighlighter(private val theme: HighlightTheme) {

    fun getCaptureColor(captureName: String): Color {
        val name = captureName.removePrefix("@")

        return when {
            name.startsWith("keyword") -> theme.keyword
            name.startsWith("string") -> theme.string
            name.startsWith("comment") -> theme.comment
            name.startsWith("number") || name == "boolean" -> theme.number
            name.startsWith("function") -> theme.function
            name.startsWith("type") -> theme.type
            name.startsWith("variable") -> theme.variable

            else -> theme.default
        }
    }
}
