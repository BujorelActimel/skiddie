package com.skiddie.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.skiddie.ui.theme.TerminalColors

object AnsiParser {
    private val ansiRegex = Regex("\u001B\\[([0-9;]*)m")

    fun parse(text: String, defaultColor: Color, terminalColors: TerminalColors): AnnotatedString {
        if (!text.contains("\u001B[")) {
            return AnnotatedString(text, SpanStyle(color = defaultColor))
        }

        return buildAnnotatedString {
            var currentStyle = SpanStyle(color = defaultColor)
            var lastIndex = 0

            ansiRegex.findAll(text).forEach { match ->
                val textBefore = text.substring(lastIndex, match.range.first)
                if (textBefore.isNotEmpty()) {
                    pushStyle(currentStyle)
                    append(textBefore)
                    pop()
                }

                val codes = match.groupValues[1]
                currentStyle = parseAnsiCodes(codes, currentStyle, defaultColor, terminalColors)

                lastIndex = match.range.last + 1
            }

            if (lastIndex < text.length) {
                val remainingText = text.substring(lastIndex)
                pushStyle(currentStyle)
                append(remainingText)
                pop()
            }
        }
    }

    private fun parseAnsiCodes(
        codes: String,
        currentStyle: SpanStyle,
        defaultColor: Color,
        terminalColors: TerminalColors
    ): SpanStyle {
        if (codes.isEmpty()) {
            return currentStyle
        }

        var style = currentStyle
        val codeList = codes.split(";").mapNotNull { it.toIntOrNull() }

        for (code in codeList) {
            style = when (code) {
                0 -> SpanStyle(color = defaultColor)

                1 -> style.copy(fontWeight = FontWeight.Bold)
                3 -> style.copy(fontStyle = FontStyle.Italic)
                4 -> style.copy(textDecoration = TextDecoration.Underline)
                22 -> style.copy(fontWeight = FontWeight.Normal) // Normal weight
                23 -> style.copy(fontStyle = FontStyle.Normal)   // Not italic
                24 -> style.copy(textDecoration = null)          // Not underlined

                30 -> style.copy(color = terminalColors.ansiBlack)
                31 -> style.copy(color = terminalColors.ansiRed)
                32 -> style.copy(color = terminalColors.ansiGreen)
                33 -> style.copy(color = terminalColors.ansiYellow)
                34 -> style.copy(color = terminalColors.ansiBlue)
                35 -> style.copy(color = terminalColors.ansiMagenta)
                36 -> style.copy(color = terminalColors.ansiCyan)
                37 -> style.copy(color = terminalColors.ansiWhite)

                90 -> style.copy(color = terminalColors.ansiBrightBlack)
                91 -> style.copy(color = terminalColors.ansiBrightRed)
                92 -> style.copy(color = terminalColors.ansiBrightGreen)
                93 -> style.copy(color = terminalColors.ansiBrightYellow)
                94 -> style.copy(color = terminalColors.ansiBrightBlue)
                95 -> style.copy(color = terminalColors.ansiBrightMagenta)
                96 -> style.copy(color = terminalColors.ansiBrightCyan)
                97 -> style.copy(color = terminalColors.ansiBrightWhite)

                40 -> style.copy(background = terminalColors.ansiBlack)
                41 -> style.copy(background = terminalColors.ansiRed)
                42 -> style.copy(background = terminalColors.ansiGreen)
                43 -> style.copy(background = terminalColors.ansiYellow)
                44 -> style.copy(background = terminalColors.ansiBlue)
                45 -> style.copy(background = terminalColors.ansiMagenta)
                46 -> style.copy(background = terminalColors.ansiCyan)
                47 -> style.copy(background = terminalColors.ansiWhite)

                100 -> style.copy(background = terminalColors.ansiBrightBlack)
                101 -> style.copy(background = terminalColors.ansiBrightRed)
                102 -> style.copy(background = terminalColors.ansiBrightGreen)
                103 -> style.copy(background = terminalColors.ansiBrightYellow)
                104 -> style.copy(background = terminalColors.ansiBrightBlue)
                105 -> style.copy(background = terminalColors.ansiBrightMagenta)
                106 -> style.copy(background = terminalColors.ansiBrightCyan)
                107 -> style.copy(background = terminalColors.ansiBrightWhite)

                39 -> style.copy(color = defaultColor)
                49 -> style.copy(background = Color.Transparent)

                else -> style
            }
        }

        return style
    }

    fun stripAnsi(text: String): String {
        return text.replace(ansiRegex, "")
    }
}
