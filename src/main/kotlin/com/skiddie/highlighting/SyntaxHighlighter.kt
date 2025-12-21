package com.skiddie.highlighting

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.skiddie.language.Language
import org.treesitter.TSQuery
import org.treesitter.TSQueryCursor
import org.treesitter.TSQueryMatch

class SyntaxHighlighter(
    language: Language,
    private val theme: HighlightTheme
) : AutoCloseable {

    private val parser: TreeSitterParser
    private val query: TSQuery
    private val queryHighlighter: QueryHighlighter

    init {
        parser = TreeSitterParser(language.grammarPath)
        val queryText = loadQuery(language.name)
        query = TSQuery(parser.language, queryText)
        queryHighlighter = QueryHighlighter(theme)
    }

    fun highlight(text: String): AnnotatedString {
        if (text.isEmpty()) {
            return AnnotatedString("")
        }

        return try {
            val tree = parser.parse(text)

            buildHighlightedText(tree, text)
        } catch (e: Exception) {
            println("Error during syntax highlighting: ${e.message}")
            e.printStackTrace()
            AnnotatedString(text, SpanStyle(color = theme.default))
        }
    }

    private fun buildHighlightedText(tree: org.treesitter.TSTree, text: String): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            val cursor = TSQueryCursor()
            cursor.exec(query, tree.rootNode)

            val captures = mutableListOf<Triple<Int, Int, String>>()
            val match = TSQueryMatch()

            while (cursor.nextMatch(match)) {
                for (capture in match.getCaptures()) {
                    val node = capture.getNode()
                    val captureName = query.getCaptureNameForId(capture.getIndex())

                    val startByte = node.getStartByte()
                    val endByte = node.getEndByte()

                    val startChar = byteOffsetToCharOffset(text, startByte)
                    val endChar = byteOffsetToCharOffset(text, endByte)

                    if (startChar < endChar && startChar < text.length) {
                        captures.add(Triple(startChar, minOf(endChar, text.length), captureName))
                    }
                }
            }

            captures.sortBy { it.first }

            for ((start, end, captureName) in captures) {
                val color = queryHighlighter.getCaptureColor(captureName)
                try {
                    addStyle(
                        style = SpanStyle(color = color),
                        start = start,
                        end = end
                    )
                } catch (e: IllegalArgumentException) {
                }
            }
        }
    }

    private fun byteOffsetToCharOffset(text: String, byteOffset: Int): Int {
        if (byteOffset == 0) return 0

        val utf8Bytes = text.toByteArray(Charsets.UTF_8)
        if (byteOffset >= utf8Bytes.size) return text.length

        var charIndex = 0
        var byteIndex = 0

        while (charIndex < text.length && byteIndex < byteOffset) {
            val char = text[charIndex]
            byteIndex += char.toString().toByteArray(Charsets.UTF_8).size
            charIndex++
        }

        return charIndex
    }

    override fun close() {
        parser.close()
    }

    private fun loadQuery(languageName: String): String {
        val resourcePath = "/queries/${languageName.lowercase()}/highlights.scm"
        val inputStream = javaClass.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Query file not found: $resourcePath")

        return inputStream.bufferedReader().use { it.readText() }
    }

    companion object {
        fun create(language: Language, theme: HighlightTheme): SyntaxHighlighter? {
            return try {
                SyntaxHighlighter(language, theme)
            } catch (e: Exception) {
                println("Failed to create syntax highlighter for ${language.name}: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}
