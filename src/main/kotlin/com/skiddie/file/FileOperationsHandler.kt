package com.skiddie.file

class FileOperationsHandler(
    private val fileManager: FileManager,
    private val onError: (String) -> Unit = {}
) {
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

    fun saveFile(content: String, currentLanguageExtension: String = "kts"): FileOperationResult {
        return try {
            if (fileManager.hasFile()) {
                fileManager.save(content)
                FileOperationResult.Success(content)
            } else {
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

sealed class FileOperationResult {
    data class Success(val content: String) : FileOperationResult()
    data class Error(val message: String) : FileOperationResult()
    object Cancelled : FileOperationResult()
}
