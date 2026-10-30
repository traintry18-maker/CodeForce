package com.example.syntax

import androidx.compose.ui.text.AnnotatedString
import com.example.data.model.SupportedLanguage

/**
 * Public facade for syntax highlighting across the application.
 * Utilizes [RegexSyntaxEngine] with a high-performance in-memory cache
 * for real-time 60 FPS rendering in Jetpack Compose text editors.
 */
object CodeHighlighter {

    private data class CacheKey(
        val codeHash: Int,
        val length: Int,
        val language: SupportedLanguage,
        val colorsHash: Int
    )

    // Fast bounded cache for highlighting results
    private val cache = LinkedHashMap<CacheKey, AnnotatedString>(32, 0.75f, true)
    private const val MAX_CACHE_SIZE = 48

    /**
     * Highlights the given code string for real-time Jetpack Compose rendering.
     */
    fun highlight(
        code: String,
        language: SupportedLanguage,
        colors: EditorColors
    ): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        val key = CacheKey(
            codeHash = code.hashCode(),
            length = code.length,
            language = language,
            colorsHash = colors.hashCode()
        )

        synchronized(cache) {
            val cached = cache[key]
            if (cached != null) return cached
        }

        val result = RegexSyntaxEngine.highlight(code, language, colors)

        synchronized(cache) {
            if (cache.size >= MAX_CACHE_SIZE) {
                val iterator = cache.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            cache[key] = result
        }

        return result
    }

    /**
     * Clears cached highlighting results.
     */
    fun clearCache() {
        synchronized(cache) {
            cache.clear()
        }
    }
}
