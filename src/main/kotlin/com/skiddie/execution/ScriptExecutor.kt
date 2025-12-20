package com.skiddie.execution

import kotlinx.coroutines.*
import java.io.File

class ScriptExecutor(
    private val scope: CoroutineScope
) {
    private var currentProcess: Process? = null
    private var executionJob: Job? = null
    private var processStdinWriter: java.io.BufferedWriter? = null
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

                withContext(Dispatchers.Main) {
                    onOutputLine(OutputLine("Starting process...", OutputType.SYSTEM))
                }

                currentProcess = processBuilder.start()

                processStdinWriter = currentProcess?.outputStream?.bufferedWriter()
                val process = currentProcess ?: run {
                    val errorLine = OutputLine("Failed to start process", OutputType.STDERR)
                    outputLines.add(errorLine)
                    withContext(Dispatchers.Main) {
                        onOutputLine(errorLine)
                    }
                    onComplete(ExecutionResult(-1, outputLines))
                    return@launch
                }

                val outputJob = launch(Dispatchers.IO) {
                    try {
                        val reader = process.inputStream.reader()
                        val buffer = StringBuilder()

                        while (isActive) {
                            val char = reader.read()
                            if (char == -1) break

                            if (char == '\n'.code || char == '\r'.code) {
                                // Skip if it's just a CR before LF (Windows line ending)
                                if (char == '\r'.code) {
                                    reader.mark(1)
                                    val next = reader.read()
                                    if (next != '\n'.code && next != -1) {
                                        reader.reset()
                                    }
                                }

                                if (buffer.isNotEmpty()) {
                                    val line = buffer.toString()
                                    buffer.clear()

                                    val outputLine = OutputLine(line, OutputType.STDOUT)
                                    outputLines.add(outputLine)

                                    withContext(Dispatchers.Main) {
                                        onOutputLine(outputLine)
                                    }
                                }
                            } else {
                                buffer.append(char.toChar())
                            }
                        }

                        if (buffer.isNotEmpty()) {
                            val line = buffer.toString()
                            val outputLine = OutputLine(line, OutputType.STDOUT)
                            outputLines.add(outputLine)
                            withContext(Dispatchers.Main) {
                                onOutputLine(outputLine)
                            }
                        }

                        reader.close()
                    } catch (e: Exception) {
                        if (isActive) {
                            val errorLine = OutputLine("Error reading output: ${e.message}", OutputType.STDERR)
                            outputLines.add(errorLine)
                            withContext(Dispatchers.Main) {
                                onOutputLine(errorLine)
                            }
                        }
                    }
                }

                outputJob.join()

                val exitCode = process.waitFor()
                val exitMessage = "Process exited with code: $exitCode"

                val exitLine = OutputLine(exitMessage, OutputType.SYSTEM)
                outputLines.add(exitLine)
                withContext(Dispatchers.Main) {
                    onOutputLine(exitLine)
                }

                onComplete(ExecutionResult(exitCode, outputLines))

            } catch (e: Exception) {
                val errorLine = OutputLine("Execution error: ${e.message}", OutputType.STDERR)
                outputLines.add(errorLine)
                withContext(Dispatchers.Main) {
                    onOutputLine(errorLine)
                }
                onComplete(ExecutionResult(-1, outputLines))
            } finally {
                processStdinWriter?.close()
                processStdinWriter = null
                currentProcess = null
            }
        }

        return executionJob ?: throw IllegalStateException("Execution job not initialized")
    }

    fun stop() {
        executionJob?.cancel()
        processStdinWriter?.close()
        processStdinWriter = null
        currentProcess?.destroyForcibly()
        currentProcess = null

        onOutputLine?.invoke(
            OutputLine("Process terminated by user", OutputType.SYSTEM)
        )
    }

    suspend fun sendInput(input: String) {
        val writer = processStdinWriter ?: return
        val process = currentProcess ?: return

        try {
            withContext(Dispatchers.Main) {
                onOutputLine?.invoke(OutputLine(input, OutputType.STDIN))
            }

            withContext(Dispatchers.IO) {
                if (process.isAlive) {
                    writer.write(input)
                    writer.newLine()
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onOutputLine?.invoke(
                    OutputLine("Failed to send input: ${e.message}", OutputType.STDERR)
                )
            }
        }
    }

    fun isRunning(): Boolean = currentProcess?.isAlive == true

    private fun parseCommand(command: String): List<String> {
        return command
            .split(" ")
            .filter { it.isNotEmpty() }
    }
}
