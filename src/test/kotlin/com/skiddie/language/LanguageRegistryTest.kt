package com.skiddie.language

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LanguageRegistryTest {
    @BeforeEach
    fun setup() {
        val userConfigDir = File(
            System.getProperty("user.home"),
            ".config/skiddie"
        )
        val userLanguagesFile = File(userConfigDir, "user-languages.json")
        if (userLanguagesFile.exists()) {
            userLanguagesFile.delete()
        }
    }

    @Test
    fun `should load built-in languages`() {
        LanguageRegistry.load()

        val languages = LanguageRegistry.all()
        assertTrue(languages.isNotEmpty(), "Should have loaded languages")

        val kotlin = languages.find { it.name == "Kotlin" }
        assertNotNull(kotlin, "Should have Kotlin language")
        assertEquals("kts", kotlin.fileExtension)
        assertTrue(kotlin.isBuiltIn)

        val swift = languages.find { it.name == "Swift" }
        assertNotNull(swift, "Should have Swift language")
        assertEquals("swift", swift.fileExtension)
        assertTrue(swift.isBuiltIn)
    }

    @Test
    fun `should add user language`() {
        LanguageRegistry.load()

        val python = LanguageRegistry.add(
            name = "Python",
            fileExtension = "py",
            grammarPath = "/grammars/python.so",
            runCommand = "python3 {file}"
        )

        assertNotNull(python)
        assertEquals("Python", python.name)
        assertEquals("py", python.fileExtension)
        assertTrue(!python.isBuiltIn, "User-added language should not be built-in")

        val retrieved = LanguageRegistry.get(python.id)
        assertEquals(python, retrieved)
    }

    @Test
    fun `should persist user languages`() {
        LanguageRegistry.load()

        val python = LanguageRegistry.add(
            name = "Python",
            fileExtension = "py",
            grammarPath = "/grammars/python.so",
            runCommand = "python3 {file}"
        )

        val userConfigDir = File(
            System.getProperty("user.home"),
            ".config/skiddie"
        )
        val userLanguagesFile = File(userConfigDir, "user-languages.json")
        assertTrue(userLanguagesFile.exists(), "User languages file should be created")

        val content = userLanguagesFile.readText()
        assertTrue(content.contains("Python"), "Should contain Python language")
    }

    @Test
    fun `should remove user language`() {
        LanguageRegistry.load()

        val python = LanguageRegistry.add(
            name = "Python",
            fileExtension = "py",
            grammarPath = "/grammars/python.so",
            runCommand = "python3 {file}"
        )

        LanguageRegistry.remove(python.id)

        val retrieved = LanguageRegistry.get(python.id)
        assertEquals(null, retrieved, "Removed language should not be found")
    }

    @Test
    fun `should not remove built-in language`() {
        LanguageRegistry.load()

        val kotlin = LanguageRegistry.all().find { it.name == "Kotlin" }
        assertNotNull(kotlin)

        val exception = assertThrows<IllegalStateException> {
            LanguageRegistry.remove(kotlin.id)
        }

        assertTrue(exception.message!!.contains("Cannot delete built-in language"))
    }
}
