package com.skiddie.file

data class FileState(
    val displayName: String = "Untitled",
    val fullPath: String = "Untitled",
    val dirtyIndicator: String = "",
    val isDirty: Boolean = false
)
