package com.skiddie.file

import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType
import java.io.File

/**
 * Centralized handler for all file operations (new, open, save).
 * Eliminates code duplication and provides consistent error handling.
 */
class FileOperationsHandler(
    private val fileManager: FileManager,
    private val onError: (String) -> Unit = {}
) {
    /**
     * Creates a new empty file.
     * @return Empty string for new file content
     */
    fun newFile(): FileOperationResult {
        return try {
            val content = fileManager.new()
            FileOperationResult.Success(content)
        } catch (e: Exception) {
            val message = "Error creating new file: ${e.message}"
            onError(message)
            FileOperationResult.Error(message)
        }
    }

    /**
     * Opens a file using a file dialog.
     * @return File content if successful, null if cancelled or error
     */
    fun openFile(): FileOperationResult {
        val file = FileDialogs.openFile(title = "Open Script") ?: return FileOperationResult.Cancelled

        return try {
            val content = fileManager.open(file.absolutePath)
            FileOperationResult.Success(content)
        } catch (e: Exception) {
            val message = "Error opening file: ${e.message}"
            onError(message)
            FileOperationResult.Error(message)
        }
    }

    /**
     * Saves the current file content.
     * If no file path exists, shows a save dialog.
     * @param content Content to save
     * @param currentLanguageExtension Extension for default filename (e.g., "kts", "swift")
     * @return true if save was successful
     */
    fun saveFile(content: String, currentLanguageExtension: String = "kts"): FileOperationResult {
        return try {
            if (fileManager.hasFile()) {
                // Save to existing file
                fileManager.save(content)
                FileOperationResult.Success(content)
            } else {
                // Show save dialog for new file
                val file = FileDialogs.saveFile(
                    title = "Save Script",
                    defaultName = "untitled.$currentLanguageExtension"
                ) ?: return FileOperationResult.Cancelled

                fileManager.save(content, file.absolutePath)
                FileOperationResult.Success(content)
            }
        } catch (e: Exception) {
            val message = "Error saving file: ${e.message}"
            onError(message)
            FileOperationResult.Error(message)
        }
    }
}

/**
 * Result of a file operation
 */
sealed class FileOperationResult {
    data class Success(val content: String) : FileOperationResult()
    data class Error(val message: String) : FileOperationResult()
    object Cancelled : FileOperationResult()
}
