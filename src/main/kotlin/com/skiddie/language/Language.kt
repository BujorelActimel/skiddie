package com.skiddie.language

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: Int,
    val name: String,
    val fileExtension: String,
    val grammarPath: String,
    val runCommand: String,
    val isBuiltIn: Boolean = false
)
