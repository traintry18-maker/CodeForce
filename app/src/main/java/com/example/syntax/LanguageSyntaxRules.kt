package com.example.syntax

import com.example.data.model.SupportedLanguage

data class LanguageRule(
    val keywords: Set<String>,
    val types: Set<String>,
    val builtins: Set<String>,
    val lineCommentPrefix: String? = "//",
    val blockCommentStart: String? = "/*",
    val blockCommentEnd: String? = "*/",
    val stringDelimiters: List<Char> = listOf('"', '\'')
)

object LanguageSyntaxRules {
    private val jsKeywords = setOf(
        "const", "let", "var", "function", "return", "if", "else", "for", "while",
        "do", "switch", "case", "break", "continue", "default", "import", "export",
        "from", "class", "extends", "super", "this", "new", "async", "await",
        "try", "catch", "finally", "throw", "typeof", "instanceof", "in", "of",
        "yield", "null", "undefined", "true", "false"
    )

    private val jsTypes = setOf(
        "String", "Number", "Boolean", "Object", "Array", "Promise", "Symbol",
        "Map", "Set", "WeakMap", "WeakSet", "Date", "RegExp", "Error", "Function"
    )

    private val reactBuiltins = setOf(
        "useState", "useEffect", "useContext", "useReducer", "useCallback", "useMemo",
        "useRef", "useImperativeHandle", "useLayoutEffect", "useId", "React", "Fragment"
    )

    private val pyKeywords = setOf(
        "def", "class", "return", "if", "elif", "else", "for", "while", "break",
        "continue", "pass", "import", "from", "as", "try", "except", "finally",
        "raise", "with", "lambda", "yield", "global", "nonlocal", "assert",
        "del", "async", "await", "True", "False", "None", "and", "or", "not", "is", "in"
    )

    private val pyTypes = setOf(
        "int", "float", "str", "bool", "list", "dict", "set", "tuple", "bytes",
        "object", "Any", "Optional", "Union", "List", "Dict", "Set", "Tuple"
    )

    private val kotlinKeywords = setOf(
        "val", "var", "fun", "class", "interface", "object", "sealed", "data",
        "enum", "return", "if", "else", "when", "for", "while", "do", "break",
        "continue", "package", "import", "as", "is", "in", "try", "catch",
        "finally", "throw", "this", "super", "override", "abstract", "open",
        "private", "protected", "public", "internal", "companion", "suspend",
        "inline", "reified", "crossinline", "noinline", "tailrec", "operator",
        "infix", "typealias", "true", "false", "null"
    )

    private val kotlinTypes = setOf(
        "Int", "Long", "Float", "Double", "Boolean", "String", "Char", "Byte",
        "Short", "Unit", "Nothing", "Any", "List", "Map", "Set", "Flow", "StateFlow",
        "SharedFlow", "CoroutineScope", "Job", "Deferred", "Result"
    )

    private val cCppKeywords = setOf(
        "int", "float", "double", "char", "void", "long", "short", "unsigned",
        "signed", "struct", "union", "enum", "typedef", "sizeof", "const",
        "static", "volatile", "extern", "auto", "register", "return", "if",
        "else", "switch", "case", "default", "break", "continue", "for", "while",
        "do", "goto", "class", "namespace", "using", "public", "private",
        "protected", "virtual", "friend", "inline", "template", "typename",
        "try", "catch", "throw", "new", "delete", "this", "operator", "true",
        "false", "nullptr", "constexpr", "concept", "requires", "co_await", "co_yield"
    )

    private val sqlKeywords = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
        "DELETE", "CREATE", "TABLE", "DROP", "ALTER", "ADD", "PRIMARY", "KEY",
        "FOREIGN", "REFERENCES", "INDEX", "JOIN", "INNER", "LEFT", "RIGHT",
        "FULL", "OUTER", "ON", "GROUP", "BY", "ORDER", "ASC", "DESC", "HAVING",
        "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT", "AS", "AND", "OR", "NOT",
        "IN", "IS", "NULL", "LIKE", "EXISTS", "BETWEEN", "CASE", "WHEN", "THEN",
        "ELSE", "END", "COUNT", "SUM", "AVG", "MIN", "MAX", "ROUND"
    )

    private val swiftKeywords = setOf(
        "let", "var", "func", "return", "if", "else", "guard", "switch", "case",
        "default", "for", "in", "while", "repeat", "break", "continue", "struct",
        "class", "enum", "protocol", "extension", "typealias", "init", "deinit",
        "self", "Self", "super", "import", "public", "private", "fileprivate",
        "internal", "open", "final", "static", "mutating", "async", "await",
        "throws", "try", "catch", "defer", "where", "some", "any", "true", "false", "nil"
    )

    private val dartKeywords = setOf(
        "var", "final", "const", "late", "void", "int", "double", "String", "bool",
        "class", "abstract", "extends", "implements", "with", "mixin", "enum",
        "import", "export", "library", "part", "return", "if", "else", "switch",
        "case", "default", "for", "while", "do", "break", "continue", "try",
        "catch", "finally", "throw", "rethrow", "async", "await", "yield",
        "sync", "dynamic", "Object", "Function", "Future", "Stream", "true", "false", "null"
    )

    private val csharpKeywords = setOf(
        "abstract", "as", "base", "bool", "break", "byte", "case", "catch", "char",
        "checked", "class", "const", "continue", "decimal", "default", "delegate",
        "do", "double", "else", "enum", "event", "explicit", "extern", "false",
        "finally", "fixed", "float", "for", "foreach", "goto", "if", "implicit",
        "in", "int", "interface", "internal", "is", "lock", "long", "namespace",
        "new", "null", "object", "operator", "out", "override", "params", "private",
        "protected", "public", "readonly", "record", "ref", "return", "sbyte",
        "sealed", "short", "sizeof", "stackalloc", "static", "string", "struct",
        "switch", "this", "throw", "true", "try", "typeof", "uint", "ulong",
        "unchecked", "unsafe", "ushort", "using", "virtual", "void", "volatile",
        "while", "async", "await", "var"
    )

    private val javaKeywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "record", "sealed",
        "permits", "var", "yield", "true", "false", "null"
    )

    private val phpKeywords = setOf(
        "echo", "print", "function", "class", "public", "private", "protected",
        "static", "new", "return", "if", "else", "elseif", "while", "do", "for",
        "foreach", "as", "break", "continue", "switch", "case", "default",
        "include", "require", "include_once", "require_once", "namespace", "use",
        "extends", "implements", "interface", "abstract", "final", "try", "catch",
        "finally", "throw", "match", "fn", "null", "true", "false"
    )

    private val rubyKeywords = setOf(
        "def", "end", "class", "module", "return", "if", "elsif", "else", "unless",
        "while", "until", "for", "in", "break", "next", "redo", "retry", "yield",
        "begin", "rescue", "ensure", "raise", "case", "when", "then", "self",
        "nil", "true", "false", "require", "require_relative", "include", "extend",
        "attr_accessor", "attr_reader", "attr_writer", "puts", "print", "p"
    )

    private val luaKeywords = setOf(
        "and", "break", "do", "else", "elseif", "end", "false", "for", "function",
        "goto", "if", "in", "local", "nil", "not", "or", "repeat", "return",
        "then", "true", "until", "while"
    )

    private val terminalKeywords = setOf(
        "echo", "cd", "ls", "cat", "grep", "awk", "sed", "mkdir", "rm", "cp",
        "mv", "touch", "chmod", "curl", "wget", "git", "npm", "node", "python",
        "docker", "export", "source", "alias", "sudo", "sleep", "clear", "if",
        "then", "else", "fi", "for", "in", "do", "done", "while", "case", "esac"
    )

    private val htmlKeywords = setOf(
        "html", "head", "body", "div", "span", "p", "a", "button", "input",
        "header", "footer", "main", "nav", "section", "article", "aside",
        "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "li", "table", "tr", "td", "th",
        "form", "label", "select", "option", "textarea", "img", "script", "style", "link", "meta"
    )

    private val cssKeywords = setOf(
        "display", "position", "margin", "padding", "width", "height", "color",
        "background", "border", "font-family", "font-size", "flex", "grid",
        "justify-content", "align-items", "border-radius", "box-shadow", "opacity",
        "transition", "transform", "animation", "overflow", "z-index", "none", "block", "inline"
    )

    fun getRule(language: SupportedLanguage): LanguageRule = when (language) {
        SupportedLanguage.HTML -> LanguageRule(
            keywords = htmlKeywords,
            types = setOf("id", "class", "style", "src", "href", "type", "name", "value"),
            builtins = setOf("DOCTYPE", "canvas", "svg", "video", "audio"),
            lineCommentPrefix = null,
            blockCommentStart = "<!--",
            blockCommentEnd = "-->"
        )
        SupportedLanguage.CSS -> LanguageRule(
            keywords = cssKeywords,
            types = setOf("px", "rem", "em", "%", "vh", "vw", "deg", "ms", "s"),
            builtins = setOf(":root", ":hover", ":focus", ":active", "::before", "::after", "@media", "@keyframes"),
            lineCommentPrefix = null,
            blockCommentStart = "/*",
            blockCommentEnd = "*/"
        )
        SupportedLanguage.JAVASCRIPT -> LanguageRule(
            keywords = jsKeywords,
            types = jsTypes,
            builtins = setOf("console", "window", "document", "Math", "JSON", "parseInt", "parseFloat", "setTimeout")
        )
        SupportedLanguage.TYPESCRIPT -> LanguageRule(
            keywords = jsKeywords + setOf("interface", "type", "enum", "declare", "namespace", "abstract", "readonly", "keyof"),
            types = jsTypes + setOf("any", "unknown", "never", "void", "Record", "Partial", "Required", "Readonly", "Pick"),
            builtins = setOf("console", "window", "document", "Math", "JSON")
        )
        SupportedLanguage.PHP -> LanguageRule(
            keywords = phpKeywords,
            types = setOf("string", "int", "float", "bool", "array", "object", "callable", "iterable"),
            builtins = setOf("json_encode", "json_decode", "header", "date", "var_dump", "isset", "empty"),
            lineCommentPrefix = "//"
        )
        SupportedLanguage.REACT, SupportedLanguage.REACT_NATIVE -> LanguageRule(
            keywords = jsKeywords + setOf("interface", "type"),
            types = jsTypes + setOf("JSX", "ReactNode", "CSSProperties", "StyleSheet"),
            builtins = reactBuiltins + setOf("console", "View", "Text", "TouchableOpacity", "ScrollView", "Image")
        )
        SupportedLanguage.PYTHON -> LanguageRule(
            keywords = pyKeywords,
            types = pyTypes,
            builtins = setOf("print", "len", "range", "sum", "min", "max", "round", "enumerate", "zip", "isinstance", "type", "open", "input"),
            lineCommentPrefix = "#",
            blockCommentStart = "\"\"\"",
            blockCommentEnd = "\"\"\""
        )
        SupportedLanguage.SQL -> LanguageRule(
            keywords = sqlKeywords,
            types = setOf("INT", "VARCHAR", "TEXT", "TIMESTAMP", "BOOLEAN", "SERIAL", "FLOAT", "DATE", "DECIMAL"),
            builtins = setOf("CURRENT_TIMESTAMP", "NOW", "COALESCE", "NULLIF"),
            lineCommentPrefix = "--"
        )
        SupportedLanguage.C, SupportedLanguage.CPP -> LanguageRule(
            keywords = cCppKeywords,
            types = setOf("size_t", "int32_t", "int64_t", "uint32_t", "vector", "string", "map", "unique_ptr", "shared_ptr"),
            builtins = setOf("printf", "malloc", "free", "cout", "cin", "endl", "std", "memcpy")
        )
        SupportedLanguage.CSHARP -> LanguageRule(
            keywords = csharpKeywords,
            types = setOf("Int32", "Int64", "String", "Boolean", "List", "Dictionary", "Task", "Action", "Func"),
            builtins = setOf("Console", "Math", "Environment", "Guid")
        )
        SupportedLanguage.JAVA -> LanguageRule(
            keywords = javaKeywords,
            types = setOf("Integer", "Long", "Double", "String", "Boolean", "List", "Map", "Set", "Stream", "Optional"),
            builtins = setOf("System", "Math", "Arrays", "Collections", "Objects")
        )
        SupportedLanguage.RUBY -> LanguageRule(
            keywords = rubyKeywords,
            types = setOf("Integer", "Float", "String", "Array", "Hash", "Symbol"),
            builtins = setOf("puts", "print", "p", "raise", "rand", "sleep"),
            lineCommentPrefix = "#",
            blockCommentStart = "=begin",
            blockCommentEnd = "=end"
        )
        SupportedLanguage.LUA -> LanguageRule(
            keywords = luaKeywords,
            types = setOf("number", "string", "boolean", "table", "function", "thread", "userdata"),
            builtins = setOf("print", "type", "tostring", "tonumber", "pairs", "ipairs", "setmetatable", "getmetatable"),
            lineCommentPrefix = "--",
            blockCommentStart = "--[[",
            blockCommentEnd = "]]"
        )
        SupportedLanguage.KOTLIN -> LanguageRule(
            keywords = kotlinKeywords,
            types = kotlinTypes,
            builtins = setOf("println", "print", "listOf", "mapOf", "setOf", "runBlocking", "launch", "async", "flow", "delay", "remember")
        )
        SupportedLanguage.DART -> LanguageRule(
            keywords = dartKeywords,
            types = setOf("Widget", "StatelessWidget", "StatefulWidget", "State", "BuildContext", "Color", "Container", "Text"),
            builtins = setOf("print", "runApp", "setState", "Future", "Stream")
        )
        SupportedLanguage.SWIFT -> LanguageRule(
            keywords = swiftKeywords,
            types = setOf("View", "Text", "VStack", "HStack", "ZStack", "List", "Button", "Color", "UUID", "State", "Binding"),
            builtins = setOf("print", "fatalError", "precondition")
        )
        SupportedLanguage.TERMINAL -> LanguageRule(
            keywords = terminalKeywords,
            types = setOf("PATH", "HOME", "USER", "SHELL", "PWD", "0", "1", "2"),
            builtins = setOf("set", "unset", "read", "printf", "trap"),
            lineCommentPrefix = "#"
        )
    }
}
