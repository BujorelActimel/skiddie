package com.skiddie.file

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

object FileDialogs {
    fun openFile(parent: Frame? = null, title: String = "Open File"): File? {
        val dialog = FileDialog(parent, title, FileDialog.LOAD)
        dialog.isVisible = true

        val directory = dialog.directory
        val filename = dialog.file

        return if (directory != null && filename != null) {
            File(directory, filename)
        } else {
            null
        }
    }

    fun saveFile(parent: Frame? = null, title: String = "Save File", defaultName: String = "untitled.kts"): File? {
        val dialog = FileDialog(parent, title, FileDialog.SAVE)
        dialog.file = defaultName
        dialog.isVisible = true

        val directory = dialog.directory
        val filename = dialog.file

        return if (directory != null && filename != null) {
            File(directory, filename)
        } else {
            null
        }
    }
}
