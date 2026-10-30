package com.example

import com.example.data.model.SupportedLanguage
import com.example.syntax.CodeHighlighter
import com.example.syntax.RegexSyntaxEngine
import com.example.syntax.SyntaxThemes
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying syntax highlighting engine functionality.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRegexSyntaxEngine_JavaScript() {
        val code = """
            // This is a comment
            const greeting = "Hello World";
            function calculate(x, y) {
                return x + y * 42;
            }
        """.trimIndent()

        val highlighted = CodeHighlighter.highlight(code, SupportedLanguage.JAVASCRIPT, SyntaxThemes.vsCodeDark)
        assertNotNull(highlighted)
        assertEquals(code, highlighted.text)
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }

    @Test
    fun testRegexSyntaxEngine_Python() {
        val code = """
            # Python script
            def analyze_data(samples):
                for item in samples:
                    if item > 10:
                        print(f"Value: {item}")
                return True
        """.trimIndent()

        val highlighted = CodeHighlighter.highlight(code, SupportedLanguage.PYTHON, SyntaxThemes.dracula)
        assertNotNull(highlighted)
        assertEquals(code, highlighted.text)
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }

    @Test
    fun testRegexSyntaxEngine_Kotlin() {
        val code = """
            package com.example
            
            // Model class
            data class User(val id: Int, val name: String)
            
            fun main() {
                val user = User(1, "Alice")
                println(user)
            }
        """.trimIndent()

        val highlighted = CodeHighlighter.highlight(code, SupportedLanguage.KOTLIN, SyntaxThemes.monokai)
        assertNotNull(highlighted)
        assertEquals(code, highlighted.text)
        assertTrue(highlighted.spanStyles.isNotEmpty())
    }

    @Test
    fun testRegexSyntaxEngine_HtmlAndCss() {
        val html = """<div class="card"><h1 id="title">Title</h1></div>"""
        val css = """.card { display: flex; color: #ffffff; padding: 12px; }"""

        val htmlHighlighted = CodeHighlighter.highlight(html, SupportedLanguage.HTML, SyntaxThemes.vsCodeDark)
        val cssHighlighted = CodeHighlighter.highlight(css, SupportedLanguage.CSS, SyntaxThemes.vsCodeDark)

        assertTrue(htmlHighlighted.spanStyles.isNotEmpty())
        assertTrue(cssHighlighted.spanStyles.isNotEmpty())
    }
}

