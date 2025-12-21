package com.skiddie.highlighting

import org.treesitter.TSParser
import org.treesitter.TSLanguage
import org.treesitter.TSTree
import org.treesitter.TreeSitterKotlin
import org.treesitter.TreeSitterSwift

class TreeSitterParser(grammarName: String) : AutoCloseable {
    private val parser: TSParser = TSParser()
    val language: TSLanguage
    private var currentTree: TSTree? = null

    init {
        language = loadGrammar(grammarName)
        parser.setLanguage(language)
    }

    fun parse(text: String): TSTree {
        val newTree = parser.parseString(null, text)
        currentTree = newTree
        return newTree
    }

    fun parseIncremental(text: String, oldTree: TSTree): TSTree {
        return parse(text)
    }

    override fun close() {
        currentTree = null
    }

    private fun loadGrammar(grammarName: String): TSLanguage {
        return when (grammarName.lowercase()) {
            "kotlin" -> TreeSitterKotlin()
            "swift" -> TreeSitterSwift()
            else -> throw IllegalArgumentException("Unsupported language: $grammarName")
        }
    }
}
