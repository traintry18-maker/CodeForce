package com.example.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.SupportedLanguage

enum class SyntaxCategory {
    COMMENT,
    STRING,
    REGEX_LITERAL,
    NUMBER,
    KEYWORD,
    FLOW_KEYWORD,
    TYPE,
    FUNCTION,
    TAG,
    ATTRIBUTE,
    PROPERTY,
    ANNOTATION,
    VARIABLE,
    CONSTANT,
    OPERATOR,
    PUNCTUATION,
    MARKDOWN_HEADER,
    MARKDOWN_CODE,
    MARKDOWN_EMPHASIS,
    MARKDOWN_LINK
}

data class RegexTokenRule(
    val pattern: Regex,
    val category: SyntaxCategory,
    val targetGroup: Int = 0
)

/**
 * High-performance, Regex-based syntax highlighting engine designed specifically
 * for Jetpack Compose AnnotatedString and VisualTransformation.
 * Supports real-time multi-language tokenization and coloring with priority-based
 * non-overlapping token tracking.
 */
object RegexSyntaxEngine {

    // Cache of compiled rules per language to guarantee instant execution
    private val languageRulesCache = mutableMapOf<SupportedLanguage, List<RegexTokenRule>>()

    init {
        for (lang in SupportedLanguage.entries) {
            languageRulesCache[lang] = buildRulesForLanguage(lang)
        }
    }

    fun getRulesForLanguage(language: SupportedLanguage): List<RegexTokenRule> {
        return languageRulesCache.getOrPut(language) { buildRulesForLanguage(language) }
    }

    /**
     * Highlights code text in real-time using priority-ordered regex tokens.
     */
    fun highlight(
        code: String,
        language: SupportedLanguage,
        colors: EditorColors
    ): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")
        val length = code.length

        // Claimed boolean mask: ensures higher priority tokens (like comments and strings)
        // are never overwritten by lower priority tokens (like keywords or numbers)
        val claimed = BooleanArray(length)
        val builder = AnnotatedString.Builder(code)
        val rules = getRulesForLanguage(language)

        for (rule in rules) {
            try {
                val matches = rule.pattern.findAll(code)
                for (match in matches) {
                    val group = if (rule.targetGroup in 0 until match.groups.size && match.groups[rule.targetGroup] != null) {
                        match.groups[rule.targetGroup]!!
                    } else {
                        continue
                    }

                    val start = group.range.first
                    val end = group.range.last + 1
                    if (start < 0 || end > length || start >= end) continue

                    // Check if already claimed by a higher-priority rule
                    var isClaimed = false
                    for (i in start until end) {
                        if (claimed[i]) {
                            isClaimed = true
                            break
                        }
                    }
                    if (isClaimed) continue

                    // Mark range as claimed
                    for (i in start until end) {
                        claimed[i] = true
                    }

                    // Apply Compose SpanStyle
                    val spanStyle = getSpanStyle(rule.category, colors)
                    builder.addStyle(spanStyle, start, end)
                }
            } catch (_: Exception) {
                // Ignore any regex evaluation errors gracefully
            }
        }

        return builder.toAnnotatedString()
    }

    private fun getSpanStyle(category: SyntaxCategory, colors: EditorColors): SpanStyle {
        return when (category) {
            SyntaxCategory.COMMENT -> SpanStyle(
                color = colors.comment,
                fontStyle = FontStyle.Italic
            )
            SyntaxCategory.STRING -> SpanStyle(
                color = colors.string
            )
            SyntaxCategory.REGEX_LITERAL -> SpanStyle(
                color = Color(0xFFD16969)
            )
            SyntaxCategory.NUMBER -> SpanStyle(
                color = colors.number
            )
            SyntaxCategory.KEYWORD -> SpanStyle(
                color = colors.keyword,
                fontWeight = FontWeight.Bold
            )
            SyntaxCategory.FLOW_KEYWORD -> SpanStyle(
                color = Color(0xFFC586C0), // VS Code control flow purple
                fontWeight = FontWeight.Bold
            )
            SyntaxCategory.TYPE -> SpanStyle(
                color = colors.type,
                fontWeight = FontWeight.Medium
            )
            SyntaxCategory.FUNCTION -> SpanStyle(
                color = colors.function
            )
            SyntaxCategory.TAG -> SpanStyle(
                color = colors.tag,
                fontWeight = FontWeight.SemiBold
            )
            SyntaxCategory.ATTRIBUTE -> SpanStyle(
                color = colors.attribute
            )
            SyntaxCategory.PROPERTY -> SpanStyle(
                color = Color(0xFF9CDCFE)
            )
            SyntaxCategory.ANNOTATION -> SpanStyle(
                color = colors.attribute,
                fontWeight = FontWeight.Medium
            )
            SyntaxCategory.VARIABLE -> SpanStyle(
                color = Color(0xFF9CDCFE)
            )
            SyntaxCategory.CONSTANT -> SpanStyle(
                color = Color(0xFF4FC1FF),
                fontWeight = FontWeight.SemiBold
            )
            SyntaxCategory.OPERATOR -> SpanStyle(
                color = colors.operator
            )
            SyntaxCategory.PUNCTUATION -> SpanStyle(
                color = colors.text.copy(alpha = 0.8f)
            )
            SyntaxCategory.MARKDOWN_HEADER -> SpanStyle(
                color = colors.keyword,
                fontWeight = FontWeight.Bold
            )
            SyntaxCategory.MARKDOWN_CODE -> SpanStyle(
                color = colors.string,
                background = Color(0x22808080)
            )
            SyntaxCategory.MARKDOWN_EMPHASIS -> SpanStyle(
                color = colors.type,
                fontStyle = FontStyle.Italic
            )
            SyntaxCategory.MARKDOWN_LINK -> SpanStyle(
                color = Color(0xFF4EC9B0)
            )
        }
    }

    private fun buildRulesForLanguage(language: SupportedLanguage): List<RegexTokenRule> {
        val rules = mutableListOf<RegexTokenRule>()

        when (language) {
            SupportedLanguage.JAVASCRIPT, SupportedLanguage.TYPESCRIPT,
            SupportedLanguage.REACT, SupportedLanguage.REACT_NATIVE -> {
                buildJavaScriptAndReactRules(rules, isTypeScript = (language == SupportedLanguage.TYPESCRIPT), isReact = (language == SupportedLanguage.REACT || language == SupportedLanguage.REACT_NATIVE))
            }
            SupportedLanguage.PYTHON -> {
                buildPythonRules(rules)
            }
            SupportedLanguage.KOTLIN -> {
                buildKotlinRules(rules)
            }
            SupportedLanguage.JAVA -> {
                buildJavaRules(rules)
            }
            SupportedLanguage.C, SupportedLanguage.CPP -> {
                buildCppRules(rules)
            }
            SupportedLanguage.CSHARP -> {
                buildCSharpRules(rules)
            }
            SupportedLanguage.HTML -> {
                buildHtmlRules(rules)
            }
            SupportedLanguage.CSS -> {
                buildCssRules(rules)
            }
            SupportedLanguage.SQL -> {
                buildSqlRules(rules)
            }
            SupportedLanguage.PHP -> {
                buildPhpRules(rules)
            }
            SupportedLanguage.SWIFT -> {
                buildSwiftRules(rules)
            }
            SupportedLanguage.DART -> {
                buildDartRules(rules)
            }
            SupportedLanguage.RUBY -> {
                buildRubyRules(rules)
            }
            SupportedLanguage.LUA -> {
                buildLuaRules(rules)
            }
            SupportedLanguage.TERMINAL -> {
                buildTerminalRules(rules)
            }
        }

        return rules
    }

    // --- Language Rule Builders ---

    private fun buildJavaScriptAndReactRules(
        rules: MutableList<RegexTokenRule>,
        isTypeScript: Boolean,
        isReact: Boolean
    ) {
        // 1. Comments
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))

        // 2. Strings & Template Literals
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""`(?:[^`\\]|\\.)*`"""), SyntaxCategory.STRING))

        // 3. Regex literals (e.g. /pattern/g)
        rules.add(RegexTokenRule(Regex("""/(?:[^\n/\\\\]|\\\\.)+/[gimsuy]*"""), SyntaxCategory.REGEX_LITERAL))

        // 4. JSX Tags & Components (for React / JSX / TSX)
        if (isReact || isTypeScript) {
            // JSX closing and opening tags: </tag> or <tag
            rules.add(RegexTokenRule(Regex("""</?([a-zA-Z][\w.-]*)"""), SyntaxCategory.TAG, targetGroup = 1))
            // React PascalCase components: <Header, <Button, <App
            rules.add(RegexTokenRule(Regex("""<([A-Z][a-zA-Z0-9]*)"""), SyntaxCategory.TYPE, targetGroup = 1))
            // JSX Attributes: propName=
            rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_:][-a-zA-Z0-9_:.]*)\s*(?==)"""), SyntaxCategory.ATTRIBUTE, targetGroup = 1))
        }

        // 5. Decorators / Annotations (e.g. @decorator)
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*"""), SyntaxCategory.ANNOTATION))

        // 6. Function declarations & calls
        rules.add(RegexTokenRule(Regex("""\b(?:function|class)\s+([a-zA-Z_]\w*)"""), SyntaxCategory.FUNCTION, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        // 7. Control Flow Keywords
        val flowKeywords = "if|else|switch|case|default|return|try|catch|finally|throw|break|continue|for|while|do|await|yield"
        rules.add(RegexTokenRule(Regex("""\b($flowKeywords)\b"""), SyntaxCategory.FLOW_KEYWORD))

        // 8. Reserved Keywords
        val jsKeywords = "const|let|var|function|class|extends|super|this|new|import|export|from|as|default|static|async|typeof|instanceof|in|of|void|delete|null|undefined|true|false"
        val tsKeywords = if (isTypeScript) "|interface|type|enum|namespace|declare|abstract|readonly|implements|keyof|is|satisfies" else ""
        rules.add(RegexTokenRule(Regex("""\b($jsKeywords$tsKeywords)\b"""), SyntaxCategory.KEYWORD))

        // 9. Built-in Types & Classes
        val types = "String|Number|Boolean|Object|Array|Promise|Symbol|Map|Set|WeakMap|WeakSet|Date|RegExp|Error|Function|Math|JSON|Console|window|document|process|global|React|Component|NodeJS|Record|Partial|Required|Readonly|Pick|Omit|any|unknown|never|void"
        rules.add(RegexTokenRule(Regex("""\b($types|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        // 10. React Hook & Helper Builtins
        if (isReact) {
            val hooks = "useState|useEffect|useContext|useReducer|useCallback|useMemo|useRef|useImperativeHandle|useLayoutEffect|useId"
            rules.add(RegexTokenRule(Regex("""\b($hooks)\b"""), SyntaxCategory.FUNCTION))
        }

        // 11. Object properties (.property)
        rules.add(RegexTokenRule(Regex("""\.([a-zA-Z_]\w*)"""), SyntaxCategory.PROPERTY, targetGroup = 1))

        // 12. Numbers (Hex, Binary, Floats, Decimals)
        rules.add(RegexTokenRule(Regex("""\b0x[0-9a-fA-F]+\b|\b0b[01]+\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?\b"""), SyntaxCategory.NUMBER))

        // 13. Operators & Arrow Function
        rules.add(RegexTokenRule(Regex("""=>|===|!==|==|!=|<=|>=|&&|\|\||\+\+|--|\+=|-=|\*=|/=|%=|\?\.|\?\?|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))

        // 14. Brackets & Delimiters
        rules.add(RegexTokenRule(Regex("""[{}()\[\],]"""), SyntaxCategory.PUNCTUATION))
    }

    private fun buildPythonRules(rules: MutableList<RegexTokenRule>) {
        // 1. Comments
        rules.add(RegexTokenRule(Regex("""#.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))

        // 2. Triple-quoted multiline docstrings
        rules.add(RegexTokenRule(Regex(""""{3}[\s\S]*?"{3}"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'{3}[\s\S]*?'{3}"""), SyntaxCategory.STRING))

        // 3. Single / Double strings (including f-strings and raw strings)
        rules.add(RegexTokenRule(Regex("""[frubFRUB]?"(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""[frubFRUB]?'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        // 4. Decorators (@decorator)
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*(?:\.[a-zA-Z_]\w*)*"""), SyntaxCategory.ANNOTATION))

        // 5. Function & Class definitions
        rules.add(RegexTokenRule(Regex("""\bdef\s+([a-zA-Z_]\w*)"""), SyntaxCategory.FUNCTION, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\bclass\s+([a-zA-Z_]\w*)"""), SyntaxCategory.TYPE, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        // 6. Flow Keywords
        val flowKeywords = "if|elif|else|for|while|break|continue|pass|return|try|except|finally|raise|with|yield|match|case"
        rules.add(RegexTokenRule(Regex("""\b($flowKeywords)\b"""), SyntaxCategory.FLOW_KEYWORD))

        // 7. Reserved Keywords
        val pyKeywords = "def|class|import|from|as|lambda|global|nonlocal|assert|del|async|await|and|or|not|is|in|self|cls|True|False|None"
        rules.add(RegexTokenRule(Regex("""\b($pyKeywords)\b"""), SyntaxCategory.KEYWORD))

        // 8. Built-in Functions & Types
        val builtins = "print|len|range|sum|min|max|round|enumerate|zip|isinstance|issubclass|type|open|input|map|filter|all|any|id|dir|help|super|int|float|str|bool|list|dict|set|tuple|bytes|bytearray|object|Any|Optional|Union|List|Dict|Set|Tuple"
        rules.add(RegexTokenRule(Regex("""\b($builtins|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        // 9. Numbers
        rules.add(RegexTokenRule(Regex("""\b0x[0-9a-fA-F]+\b|\b0b[01]+\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?j?\b"""), SyntaxCategory.NUMBER))

        // 10. Operators
        rules.add(RegexTokenRule(Regex("""->|==|!=|<=|>=|\/\/|\*\*|\+=|-=|\*=|/=|%=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildKotlinRules(rules: MutableList<RegexTokenRule>) {
        // 1. Comments
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))

        // 2. Triple-quoted multiline raw strings
        rules.add(RegexTokenRule(Regex(""""{3}[\s\S]*?"{3}"""), SyntaxCategory.STRING))

        // 3. Regular Strings and Characters
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        // 4. Annotations (@Composable, @Override, etc.)
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*"""), SyntaxCategory.ANNOTATION))

        // 5. Function & Class Declarations
        rules.add(RegexTokenRule(Regex("""\bfun\s+(?:<[^>]+>\s+)?([a-zA-Z_]\w*)"""), SyntaxCategory.FUNCTION, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\bclass\s+([a-zA-Z_]\w*)"""), SyntaxCategory.TYPE, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        // 6. Flow Keywords
        val flowKeywords = "if|else|when|return|throw|try|catch|finally|break|continue|for|while|do"
        rules.add(RegexTokenRule(Regex("""\b($flowKeywords)\b"""), SyntaxCategory.FLOW_KEYWORD))

        // 7. Reserved Keywords
        val ktKeywords = "val|var|fun|class|interface|object|sealed|data|enum|package|import|as|is|in|this|super|override|abstract|open|private|protected|public|internal|companion|suspend|inline|reified|crossinline|noinline|tailrec|operator|infix|typealias|true|false|null|by|constructor|init"
        rules.add(RegexTokenRule(Regex("""\b($ktKeywords)\b"""), SyntaxCategory.KEYWORD))

        // 8. Types
        val ktTypes = "Int|Long|Float|Double|Boolean|String|Char|Byte|Short|Unit|Nothing|Any|List|Map|Set|Flow|StateFlow|SharedFlow|CoroutineScope|Job|Deferred|Result|Modifier"
        rules.add(RegexTokenRule(Regex("""\b($ktTypes|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        // 9. Numbers
        rules.add(RegexTokenRule(Regex("""\b0x[0-9a-fA-F]+[uUlL]*\b|\b0b[01]+[uUlL]*\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[fFdDlL]?\b"""), SyntaxCategory.NUMBER))

        // 10. Operators
        rules.add(RegexTokenRule(Regex("""->|::|\?:|\?\.|\+\+|--|===|!==|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildJavaRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*"""), SyntaxCategory.ANNOTATION))

        rules.add(RegexTokenRule(Regex("""\bclass\s+([a-zA-Z_]\w*)"""), SyntaxCategory.TYPE, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val flow = "if|else|switch|case|default|return|break|continue|for|while|do|try|catch|finally|throw|assert|yield"
        rules.add(RegexTokenRule(Regex("""\b($flow)\b"""), SyntaxCategory.FLOW_KEYWORD))

        val keywords = "abstract|boolean|byte|char|class|const|double|enum|extends|final|float|goto|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|short|static|strictfp|super|synchronized|this|throws|transient|void|volatile|record|sealed|permits|var|true|false|null"
        rules.add(RegexTokenRule(Regex("""\b($keywords)\b"""), SyntaxCategory.KEYWORD))

        val types = "String|Integer|Double|Float|Long|Boolean|Character|Byte|Short|Object|Class|System|Math|List|Map|Set|Collections|Arrays"
        rules.add(RegexTokenRule(Regex("""\b($types|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        rules.add(RegexTokenRule(Regex("""\b0x[0-9a-fA-F]+[lL]?\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[fFdDlL]?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""->|::|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildCppRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""#\s*(?:include|define|ifdef|ifndef|endif|pragma|if|else|elif).*$""", RegexOption.MULTILINE), SyntaxCategory.ANNOTATION))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val flow = "if|else|switch|case|default|return|break|continue|for|while|do|try|catch|throw|goto"
        rules.add(RegexTokenRule(Regex("""\b($flow)\b"""), SyntaxCategory.FLOW_KEYWORD))

        val cppKeywords = "int|float|double|char|void|long|short|unsigned|signed|struct|union|enum|typedef|sizeof|const|static|volatile|extern|auto|register|class|namespace|using|public|private|protected|virtual|friend|inline|template|typename|new|delete|this|operator|true|false|nullptr|constexpr|concept|requires"
        rules.add(RegexTokenRule(Regex("""\b($cppKeywords)\b"""), SyntaxCategory.KEYWORD))

        val types = "size_t|int32_t|int64_t|uint32_t|uint64_t|std|vector|string|map|unordered_map|set|unique_ptr|shared_ptr|cout|cin|endl"
        rules.add(RegexTokenRule(Regex("""\b($types|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        rules.add(RegexTokenRule(Regex("""\b0x[0-9a-fA-F]+[uUlL]*\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[fFdDlL]?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""->|::|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildCSharpRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""\$?"(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""\[[a-zA-Z_]\w*(?:\(.*?\))?\]"""), SyntaxCategory.ANNOTATION))

        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val flow = "if|else|switch|case|default|return|break|continue|for|foreach|while|do|try|catch|finally|throw|goto|await"
        rules.add(RegexTokenRule(Regex("""\b($flow)\b"""), SyntaxCategory.FLOW_KEYWORD))

        val csKeywords = "abstract|as|base|bool|byte|char|checked|class|const|decimal|delegate|double|enum|event|explicit|extern|false|finally|fixed|float|implicit|in|int|interface|internal|is|lock|long|namespace|new|null|object|operator|out|override|params|private|protected|public|readonly|record|ref|sbyte|sealed|short|sizeof|stackalloc|static|string|struct|this|true|typeof|uint|ulong|unchecked|unsafe|ushort|using|virtual|void|volatile|async|var"
        rules.add(RegexTokenRule(Regex("""\b($csKeywords)\b"""), SyntaxCategory.KEYWORD))

        rules.add(RegexTokenRule(Regex("""\b([A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))
        rules.add(RegexTokenRule(Regex("""\b0x[0-9a-fA-F]+[uUlL]*\b|\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[fFdDmM]?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""=>|\?\?|\?\.|\+\+|--|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildHtmlRules(rules: MutableList<RegexTokenRule>) {
        // 1. Comments
        rules.add(RegexTokenRule(Regex("""<!--[\s\S]*?-->"""), SyntaxCategory.COMMENT))

        // 2. Doctype
        rules.add(RegexTokenRule(Regex("""<!DOCTYPE\s+html>""", RegexOption.IGNORE_CASE), SyntaxCategory.KEYWORD))

        // 3. Script and Style tag content tags
        rules.add(RegexTokenRule(Regex("""</?(?:script|style)[^>]*>""", RegexOption.IGNORE_CASE), SyntaxCategory.TAG))

        // 4. Strings in attributes
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        // 5. HTML Tags
        rules.add(RegexTokenRule(Regex("""</?([a-zA-Z0-9_-]+)"""), SyntaxCategory.TAG, targetGroup = 1))

        // 6. Attributes (e.g. href=, src=, class=, id=)
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_-]+)\s*(?==)"""), SyntaxCategory.ATTRIBUTE, targetGroup = 1))

        // 7. Delimiters
        rules.add(RegexTokenRule(Regex("""[<>/=]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildCssRules(rules: MutableList<RegexTokenRule>) {
        // 1. Comments
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))

        // 2. Strings
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        // 3. At-rules (@media, @keyframes, @import)
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_-]+"""), SyntaxCategory.KEYWORD))

        // 4. Selectors: Classes (.title), IDs (#header), Pseudo (:hover, ::before)
        rules.add(RegexTokenRule(Regex("""\.[a-zA-Z_-][\w-]*"""), SyntaxCategory.TYPE))
        rules.add(RegexTokenRule(Regex("""#[a-zA-Z_-][\w-]*"""), SyntaxCategory.FUNCTION))
        rules.add(RegexTokenRule(Regex("""::?[a-zA-Z_-]+"""), SyntaxCategory.ATTRIBUTE))

        // 5. CSS Properties (color:, background:)
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z-]+)\s*(?=:)"""), SyntaxCategory.PROPERTY, targetGroup = 1))

        // 6. Hex Colors (#ffffff, #38bdf8)
        rules.add(RegexTokenRule(Regex("""#(?:[0-9a-fA-F]{3,8})\b"""), SyntaxCategory.CONSTANT))

        // 7. Numbers with units (12px, 1.5rem, 100%)
        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?(?:px|rem|em|%|vh|vw|pt|s|ms|deg|fr)\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))

        // 8. Common CSS values
        val cssValues = "none|block|inline|flex|grid|auto|relative|absolute|fixed|sticky|bold|normal|center|left|right|pointer|hidden|visible|transparent|inherit|initial"
        rules.add(RegexTokenRule(Regex("""\b($cssValues)\b"""), SyntaxCategory.KEYWORD))

        // 9. Operators & Delimiters
        rules.add(RegexTokenRule(Regex("""[{}:;,]"""), SyntaxCategory.PUNCTUATION))
    }

    private fun buildSqlRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""--.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))

        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))

        // SQL Functions
        val sqlFuncs = "COUNT|SUM|AVG|MIN|MAX|ROUND|COALESCE|NOW|CONCAT|CURRENT_TIMESTAMP|UPPER|LOWER|LENGTH|SUBSTRING|TRIM|DATE"
        rules.add(RegexTokenRule(Regex("""\b($sqlFuncs)\b""", RegexOption.IGNORE_CASE), SyntaxCategory.FUNCTION))

        // SQL Keywords
        val sqlKeywords = "SELECT|FROM|WHERE|INSERT|INTO|VALUES|UPDATE|SET|DELETE|CREATE|TABLE|DROP|ALTER|ADD|PRIMARY|KEY|FOREIGN|REFERENCES|JOIN|INNER|LEFT|RIGHT|FULL|OUTER|ON|GROUP|BY|ORDER|ASC|DESC|HAVING|LIMIT|OFFSET|UNION|ALL|DISTINCT|AS|AND|OR|NOT|IN|IS|NULL|LIKE|EXISTS|BETWEEN|CASE|WHEN|THEN|ELSE|END|DEFAULT|INDEX|VIEW"
        rules.add(RegexTokenRule(Regex("""\b($sqlKeywords)\b""", RegexOption.IGNORE_CASE), SyntaxCategory.KEYWORD))

        // Data Types
        val sqlTypes = "INT|INTEGER|BIGINT|SMALLINT|TINYINT|VARCHAR|CHAR|TEXT|TIMESTAMP|BOOLEAN|SERIAL|FLOAT|DOUBLE|DATE|TIME|DECIMAL|NUMERIC|BLOB|JSON"
        rules.add(RegexTokenRule(Regex("""\b($sqlTypes)\b""", RegexOption.IGNORE_CASE), SyntaxCategory.TYPE))

        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""[+\-*/%=<>!;,]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildPhpRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""#.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))

        rules.add(RegexTokenRule(Regex("""<\?php|\?>"""), SyntaxCategory.ANNOTATION))
        rules.add(RegexTokenRule(Regex("""\$[a-zA-Z_]\w*"""), SyntaxCategory.VARIABLE))

        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        rules.add(RegexTokenRule(Regex("""\bfunction\s+([a-zA-Z_]\w*)"""), SyntaxCategory.FUNCTION, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val phpKeywords = "echo|print|function|class|public|private|protected|static|new|return|if|else|elseif|while|do|for|foreach|as|break|continue|switch|case|default|include|require|include_once|require_once|namespace|use|extends|implements|interface|abstract|final|try|catch|finally|throw|match|fn|null|true|false"
        rules.add(RegexTokenRule(Regex("""\b($phpKeywords)\b"""), SyntaxCategory.KEYWORD))

        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""->|=>|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildSwiftRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))

        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*"""), SyntaxCategory.ANNOTATION))
        rules.add(RegexTokenRule(Regex("""\bfunc\s+([a-zA-Z_]\w*)"""), SyntaxCategory.FUNCTION, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val swiftKeywords = "let|var|func|return|if|else|guard|switch|case|default|for|in|while|repeat|break|continue|struct|class|enum|protocol|extension|typealias|init|deinit|self|Self|super|import|public|private|fileprivate|internal|open|final|static|mutating|async|await|throws|try|catch|defer|where|some|any|true|false|nil"
        rules.add(RegexTokenRule(Regex("""\b($swiftKeywords)\b"""), SyntaxCategory.KEYWORD))

        val swiftTypes = "Int|Double|Float|String|Bool|Array|Dictionary|Set|Optional|View|Text|VStack|HStack|ZStack|List|Button|Color"
        rules.add(RegexTokenRule(Regex("""\b($swiftTypes|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""->|\?\?|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildDartRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""//.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*"""), SyntaxCategory.ANNOTATION))

        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val dartKeywords = "var|final|const|late|void|class|abstract|extends|implements|with|mixin|enum|import|export|library|part|return|if|else|switch|case|default|for|while|do|break|continue|try|catch|finally|throw|rethrow|async|await|yield|sync|dynamic|true|false|null|this|super|new"
        rules.add(RegexTokenRule(Regex("""\b($dartKeywords)\b"""), SyntaxCategory.KEYWORD))

        val dartTypes = "int|double|String|bool|List|Map|Set|Future|Stream|Widget|StatelessWidget|StatefulWidget|State|BuildContext|Container|Text"
        rules.add(RegexTokenRule(Regex("""\b($dartTypes|[A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))

        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""=>|\?\?|\?\.|\+\+|--|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildRubyRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""=begin[\s\S]*?=end"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""#.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex(""":[a-zA-Z_]\w*"""), SyntaxCategory.CONSTANT))
        rules.add(RegexTokenRule(Regex("""@[a-zA-Z_]\w*"""), SyntaxCategory.VARIABLE))

        val rubyKeywords = "def|end|class|module|return|if|elsif|else|unless|while|until|for|in|break|next|redo|retry|yield|begin|rescue|ensure|raise|case|when|then|self|nil|true|false|require|require_relative|include|extend|attr_accessor|puts|print|p"
        rules.add(RegexTokenRule(Regex("""\b($rubyKeywords)\b"""), SyntaxCategory.KEYWORD))

        rules.add(RegexTokenRule(Regex("""\b([A-Z][a-zA-Z0-9_]*)\b"""), SyntaxCategory.TYPE))
        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""=>|==|!=|<=|>=|[+\-*/%=<>!&|^~?:;]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildLuaRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""--\[\[[\s\S]*?\]\]"""), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex("""--.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        rules.add(RegexTokenRule(Regex("""\bfunction\s+([a-zA-Z_]\w*)"""), SyntaxCategory.FUNCTION, targetGroup = 1))
        rules.add(RegexTokenRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxCategory.FUNCTION, targetGroup = 1))

        val luaKeywords = "and|break|do|else|elseif|end|false|for|function|goto|if|in|local|nil|not|or|repeat|return|then|true|until|while"
        rules.add(RegexTokenRule(Regex("""\b($luaKeywords)\b"""), SyntaxCategory.KEYWORD))

        rules.add(RegexTokenRule(Regex("""\b\d+(?:\.\d+)?\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""\.\.|~=|==|<=|>=|[+\-*/%=<>#]"""), SyntaxCategory.OPERATOR))
    }

    private fun buildTerminalRules(rules: MutableList<RegexTokenRule>) {
        rules.add(RegexTokenRule(Regex("""#.*$""", RegexOption.MULTILINE), SyntaxCategory.COMMENT))
        rules.add(RegexTokenRule(Regex(""""(?:[^"\\]|\\.)*""""), SyntaxCategory.STRING))
        rules.add(RegexTokenRule(Regex("""'(?:[^'\\]|\\.)*'"""), SyntaxCategory.STRING))

        // Variables: $VAR, ${VAR}
        rules.add(RegexTokenRule(Regex("""\$[a-zA-Z_0-9]+|\$\{[^}]+\}"""), SyntaxCategory.VARIABLE))

        // Command line flags: -f, --flag
        rules.add(RegexTokenRule(Regex("""--?[a-zA-Z_-]+"""), SyntaxCategory.ATTRIBUTE))

        val shellCommands = "echo|cd|ls|cat|grep|awk|sed|mkdir|rm|cp|mv|touch|chmod|curl|wget|git|npm|node|python|docker|export|source|alias|sudo|sleep|clear|if|then|else|fi|for|in|do|done|while|case|esac"
        rules.add(RegexTokenRule(Regex("""\b($shellCommands)\b"""), SyntaxCategory.KEYWORD))

        rules.add(RegexTokenRule(Regex("""\b\d+\b"""), SyntaxCategory.NUMBER))
        rules.add(RegexTokenRule(Regex("""[|><;&]"""), SyntaxCategory.OPERATOR))
    }
}
