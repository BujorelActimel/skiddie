package com.skiddie.ui.viewmodel

import com.skiddie.execution.OutputLine
import com.skiddie.execution.OutputType
import com.skiddie.execution.ScriptExecutor
import com.skiddie.file.FileManager
import com.skiddie.language.Language
import com.skiddie.terminal.TerminalBuffer
import com.skiddie.terminal.TerminalMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExecutionViewModel(
    private val scope: CoroutineScope,
    private val fileManager: FileManager,
    private val onFileStateChange: () -> Unit = {}
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _terminalMode = MutableStateFlow(TerminalMode.READ_ONLY)
    val terminalMode: StateFlow<TerminalMode> = _terminalMode.asStateFlow()

    val terminalBuffer = TerminalBuffer(maxLines = 10000)

    private var executor: ScriptExecutor? = null

    fun runScript(code: String, language: Language) {
        try {
            terminalBuffer.clear()
            _terminalMode.value = TerminalMode.INTERACTIVE
            _isRunning.value = true

            if (fileManager.hasFile() && fileManager.isDirty()) {
                try {
                    fileManager.save(code)
                    onFileStateChange()
                } catch (e: Exception) {
                    terminalBuffer.addLine(
                        OutputLine("Warning: Failed to autosave before run: ${e.message}", OutputType.SYSTEM)
                    )
                }
            }

            val tempFile = fileManager.getTempFileForExecution(
                content = code,
                extension = language.fileExtension
            )

            if (executor == null) {
                executor = ScriptExecutor(scope)
            }

            val command = language.runCommand.replace("{file}", tempFile.absolutePath)

            scope.launch {
                executor?.execute(
                    command = command,
                    workingDirectory = tempFile.parentFile,
                    onOutputLine = { line ->
                        terminalBuffer.addLine(line)
                    },
                    onComplete = { _ ->
                        _isRunning.value = false
                        _terminalMode.value = TerminalMode.READ_ONLY
                        tempFile.delete()
                    }
                )
            }
        } catch (e: Exception) {
            terminalBuffer.clear()
            terminalBuffer.addLine(
                OutputLine("Failed to start: ${e.message}", OutputType.STDERR)
            )
            _terminalMode.value = TerminalMode.READ_ONLY
            _isRunning.value = false
        }
    }

    fun stopScript() {
        executor?.stop()
        _isRunning.value = false
        _terminalMode.value = TerminalMode.READ_ONLY
    }

    fun sendInput(input: String) {
        if (input.isNotEmpty() && _isRunning.value) {
            scope.launch {
                executor?.sendInput(input)
            }
        }
    }

    fun clearTerminal() {
        terminalBuffer.clear()
    }
}
