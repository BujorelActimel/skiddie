package com.skiddie.execution

data class OutputLine(
    val text: String,
    val type: OutputType,
    val timeStamp: Long = System.currentTimeMillis(),
)

enum class OutputType {
    STDOUT,
    STDERR,
    STDIN,
    SYSTEM,
}

data class ExecutionResult(
    val exitCode: Int,
    val lines: List<OutputLine>,
)
