package com.skiddie.execution

import kotlinx.coroutines.*
import java.io.File

class ScriptExecutor(
    private val scope: CoroutineScope
) {
    private var currentProcess: Process? = null
    private var executionJob: Job? = null
    private var onOutputLine: ((OutputLine) -> Unit)? = null

    suspend fun execute(
        command: String,
        workingDirectory: File,
        onOutputLine: (OutputLine) -> Unit,
        onComplete: (ExecutionResult) -> Unit
    ): Job {
        this.onOutputLine = onOutputLine

        executionJob = scope.launch(Dispatchers.IO) {
            val outputLines = mutableListOf<OutputLine>()

            try {
                val commandList = parseCommand(command)
                val processBuilder = ProcessBuilder()
                    .command(commandList)
                    .directory(workingDirectory)
                    .redirectErrorStream(true)

                onOutputLine(OutputLine("Starting process...", OutputType.SYSTEM))

                currentProcess = processBuilder.start()
                val process = currentProcess!!

                val outputJob = launch(Dispatchers.IO) {
                    try {
                        val reader = process.inputStream.bufferedReader()
                        while (isActive) {
                            val line = reader.readLine() ?: break
                            val outputLine = OutputLine(line, OutputType.STDOUT)
                            outputLines.add(outputLine)
                            onOutputLine(outputLine)
                        }
                        reader.close()
                    } catch (e: Exception) {
                        if (isActive) {
                            val errorLine = OutputLine("Error reading output: ${e.message}", OutputType.STDERR)
                            outputLines.add(errorLine)
                            onOutputLine(errorLine)
                        }
                    }
                }

                outputJob.join()

                val exitCode = process.waitFor()

                val exitMessage = if (exitCode == 0) {
                    "Process completed successfully (exit code: $exitCode)"
                } else {
                    "Process exited with code: $exitCode"
                }

                val exitLine = OutputLine(exitMessage, OutputType.SYSTEM)
                outputLines.add(exitLine)
                onOutputLine(exitLine)

                onComplete(ExecutionResult(exitCode, outputLines))

            } catch (e: Exception) {
                val errorLine = OutputLine("Execution error: ${e.message}", OutputType.STDERR)
                outputLines.add(errorLine)
                onOutputLine(errorLine)
                onComplete(ExecutionResult(-1, outputLines))
            } finally {
                currentProcess = null
            }
        }

        return executionJob!!
    }

    fun stop() {
        executionJob?.cancel()
        currentProcess?.destroyForcibly()
        currentProcess = null

        onOutputLine?.invoke(
            OutputLine("Process terminated by user", OutputType.SYSTEM)
        )
    }

    fun sendInput(input: String) {
        val process = currentProcess ?: return

        try {
            onOutputLine?.invoke(OutputLine(input, OutputType.STDIN))

            process.outputStream.bufferedWriter().apply {
                write(input)
                newLine()
                flush()
            }
        } catch (e: Exception) {
            onOutputLine?.invoke(
                OutputLine("Failed to send input: ${e.message}", OutputType.STDERR)
            )
        }
    }

    fun isRunning(): Boolean = currentProcess?.isAlive == true

    private fun parseCommand(command: String): List<String> {
        return command
            .split(" ")
            .filter { it.isNotEmpty() }
    }
}
