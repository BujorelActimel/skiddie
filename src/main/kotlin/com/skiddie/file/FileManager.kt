package com.skiddie.file

import java.io.File
import java.nio.file.Paths

class FileManager {
    private var currentFilePath: String? = null
    private var savedContent: String = ""
    private var isDirty: Boolean = false

    private val tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "skiddie").toFile().apply {
        mkdirs()
    }

    fun new(initialContent: String = ""): String {
        currentFilePath = null
        savedContent = initialContent
        isDirty = false
        return initialContent
    }

    fun open(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            error("File does not exist: $filePath")
        }

        val content = file.readText()
        currentFilePath = filePath
        savedContent = content
        isDirty = false
        return content
    }

    fun save(content: String, filePath: String? = null): String {
        val pathToSave = filePath ?: currentFilePath ?: error("No file path specified")

        File(pathToSave).writeText(content)
        currentFilePath = pathToSave
        savedContent = content
        isDirty = false

        return pathToSave
    }

    fun saveAs(content: String, filePath: String): String {
        return save(content, filePath)
    }

    fun getTempFileForExecution(content: String, extension: String): File {
        val originalName = currentFilePath?.let { File(it).nameWithoutExtension } ?: "untitled"
        val tempFile = File.createTempFile("${originalName}_", ".$extension", tempDir)

        tempFile.writeText(content)
        return tempFile
    }

    fun markDirty(currentContent: String) {
        isDirty = currentContent != savedContent
    }

    fun getDisplayName(): String {
        return currentFilePath?.let { File(it).name } ?: "Untitled"
    }

    fun getFullDisplayPath(): String {
        return currentFilePath ?: "Untitled"
    }

    fun getDirtyIndicator(): String {
        return if (isDirty) "*" else ""
    }

    fun getCurrentFilePath(): String? = currentFilePath

    fun isDirty(): Boolean = isDirty

    fun hasFile(): Boolean = currentFilePath != null
}
