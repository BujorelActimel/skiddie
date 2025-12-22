package com.skiddie.file

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FileStateManager(
    private val fileManager: FileManager
) {
    private val _state = MutableStateFlow(FileState())
    val state: StateFlow<FileState> = _state.asStateFlow()

    fun updateContent(newContent: String) {
        fileManager.markDirty(newContent)
        _state.value = FileState(
            displayName = fileManager.getDisplayName(),
            fullPath = fileManager.getFullDisplayPath(),
            dirtyIndicator = fileManager.getDirtyIndicator(),
            isDirty = fileManager.isDirty()
        )
    }

    fun markSaved() {
        _state.value = FileState(
            displayName = fileManager.getDisplayName(),
            fullPath = fileManager.getFullDisplayPath(),
            dirtyIndicator = "",
            isDirty = false
        )
    }

    fun refresh() {
        _state.value = FileState(
            displayName = fileManager.getDisplayName(),
            fullPath = fileManager.getFullDisplayPath(),
            dirtyIndicator = fileManager.getDirtyIndicator(),
            isDirty = fileManager.isDirty()
        )
    }
}
