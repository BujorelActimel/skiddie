package com.skiddie.language

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.file.Paths

@Serializable
private data class LanguageConfig(
    val languages: List<Language>
)

object LanguageRegistry {
    private val languages = mutableMapOf<Int, Language>()
    private var nextId = 1

    private val json by lazy {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }

    private val userConfigDir by lazy {
        Paths.get(
            System.getProperty("user.home") ?: error("user.home system property not set"),
            ".config",
            "skiddie"
        ).toFile()
    }

    private val userLanguagesFile by lazy { File(userConfigDir, "user-languages.json") }

    fun load() {
        loadBuiltInLanguages()
        loadUserLanguages()
    }

    fun add(
        name: String,
        fileExtension: String,
        grammarPath: String,
        runCommand: String,
        isBuiltIn: Boolean  = false
    ): Language {
        val newLanguage = Language(
            nextId++,
            name,
            fileExtension,
            grammarPath,
            runCommand,
            isBuiltIn
        )

        languages[newLanguage.id] = newLanguage

        if (!newLanguage.isBuiltIn) {
            saveUserLanguages()
        }

        return newLanguage
    }

    fun remove(id: Int) {
        val language = languages[id] ?: error("Language with id $id not found")

        if (language.isBuiltIn) {
            error("Cannot delete built-in language: ${language.name}")
        }

        languages.remove(id)
        saveUserLanguages()
    }

    fun get(id: Int): Language? = languages[id]

    fun all(): List<Language> = languages.values.toList()

    private fun loadBuiltInLanguages() {
        val resourceStream = this::class.java.getResourceAsStream("/languages.json")
            ?: error("Built-in languages.json not found in resources")

        val configText = resourceStream.bufferedReader().use { it.readText() }
        val config = json.decodeFromString<LanguageConfig>(configText)

        config.languages.forEach { language ->
            languages[language.id] = language
            if (language.id >= nextId) {
                nextId = language.id + 1
            }
        }
    }

    private fun loadUserLanguages() {
        if (!userLanguagesFile.exists()) {
            return
        }

        val configText = userLanguagesFile.readText()
        val config = json.decodeFromString<LanguageConfig>(configText)

        config.languages.forEach { language ->
            languages[language.id] = language
            if (language.id >= nextId) {
                nextId = language.id + 1
            }
        }
    }

    private fun saveUserLanguages() {
        userConfigDir.mkdirs()

        val userLanguages = languages.values.filter { !it.isBuiltIn }
        val config = LanguageConfig(userLanguages)

        userLanguagesFile.writeText(json.encodeToString(config))
    }
}
