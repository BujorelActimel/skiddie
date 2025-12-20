package com.skiddie.terminal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.skiddie.execution.OutputLine

class TerminalBuffer(
    private val maxLines: Int = 10000
) {
    private val lines: SnapshotStateList<OutputLine> = mutableStateListOf()

    fun addLine(line: OutputLine) {
        lines.add(line)

        if (lines.size > maxLines) {
            val toRemove = lines.size - maxLines
            repeat(toRemove) {
                lines.removeAt(0)
            }
        }
    }

    fun clear() {
        lines.clear()
    }

    fun getLines(): SnapshotStateList<OutputLine> {
        return lines
    }

    fun size(): Int {
        return lines.size
    }

    fun isEmpty(): Boolean {
        return lines.isEmpty()
    }

    fun isFull(): Boolean {
        return lines.size >= maxLines
    }
}
