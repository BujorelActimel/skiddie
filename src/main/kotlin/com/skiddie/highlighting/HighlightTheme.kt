package com.skiddie.highlighting

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class HighlightTheme(
    val keyword: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val function: Color,
    val type: Color,
    val variable: Color,
    val operator: Color,
    val punctuation: Color,
    val property: Color,
    val constant: Color,
    val default: Color
)
