package com.example.syntax

import androidx.compose.ui.graphics.Color

enum class EditorThemeMode(val displayName: String) {
    VS_CODE_DARK("VS Code Dark"),
    DRACULA("Dracula Modern"),
    MONOKAI("Monokai Pro"),
    CYBERPUNK("Cyberpunk Neon"),
    GITHUB_LIGHT("GitHub Light")
}

data class EditorColors(
    val background: Color,
    val currentLineBackground: Color,
    val gutterBackground: Color,
    val gutterText: Color,
    val activeGutterText: Color,
    val text: Color,
    val keyword: Color,
    val type: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val function: Color,
    val operator: Color,
    val tag: Color,
    val attribute: Color,
    val selection: Color
)

object SyntaxThemes {
    val vsCodeDark = EditorColors(
        background = Color(0xFF1E1E1E),
        currentLineBackground = Color(0xFF282828),
        gutterBackground = Color(0xFF1E1E1E),
        gutterText = Color(0xFF6E7681),
        activeGutterText = Color(0xFFC6C6C6),
        text = Color(0xFFD4D4D4),
        keyword = Color(0xFF569CD6),
        type = Color(0xFF4EC9B0),
        string = Color(0xFFCE9178),
        number = Color(0xFFB5CEA8),
        comment = Color(0xFF6A9955),
        function = Color(0xFFDCDCAA),
        operator = Color(0xFFD4D4D4),
        tag = Color(0xFF569CD6),
        attribute = Color(0xFF9CDCFE),
        selection = Color(0xFF264F78)
    )

    val dracula = EditorColors(
        background = Color(0xFF282A36),
        currentLineBackground = Color(0xFF44475A),
        gutterBackground = Color(0xFF21222C),
        gutterText = Color(0xFF6272A4),
        activeGutterText = Color(0xFFF8F8F2),
        text = Color(0xFFF8F8F2),
        keyword = Color(0xFFFF79C6),
        type = Color(0xFF8BE9FD),
        string = Color(0xFFF1FA8C),
        number = Color(0xFFBD93F9),
        comment = Color(0xFF6272A4),
        function = Color(0xFF50FA7B),
        operator = Color(0xFFFF79C6),
        tag = Color(0xFFFF79C6),
        attribute = Color(0xFF50FA7B),
        selection = Color(0xFF44475A)
    )

    val monokai = EditorColors(
        background = Color(0xFF272822),
        currentLineBackground = Color(0xFF3E3D32),
        gutterBackground = Color(0xFF272822),
        gutterText = Color(0xFF75715E),
        activeGutterText = Color(0xFFF8F8F2),
        text = Color(0xFFF8F8F2),
        keyword = Color(0xFFF92672),
        type = Color(0xFF66D9EF),
        string = Color(0xFFE6DB74),
        number = Color(0xFFAE81FF),
        comment = Color(0xFF75715E),
        function = Color(0xFFA6E22E),
        operator = Color(0xFFF92672),
        tag = Color(0xFFF92672),
        attribute = Color(0xFFA6E22E),
        selection = Color(0xFF49483E)
    )

    val cyberpunk = EditorColors(
        background = Color(0xFF0B0E14),
        currentLineBackground = Color(0xFF1B2234),
        gutterBackground = Color(0xFF0B0E14),
        gutterText = Color(0xFF4B5563),
        activeGutterText = Color(0xFF00F0FF),
        text = Color(0xFFE2E8F0),
        keyword = Color(0xFFFF007F),
        type = Color(0xFF00F0FF),
        string = Color(0xFF00FF9F),
        number = Color(0xFFFFE600),
        comment = Color(0xFF64748B),
        function = Color(0xFF7928CA),
        operator = Color(0xFF00F0FF),
        tag = Color(0xFFFF007F),
        attribute = Color(0xFF00F0FF),
        selection = Color(0xFF2A2050)
    )

    val githubLight = EditorColors(
        background = Color(0xFFFFFFFF),
        currentLineBackground = Color(0xFFF6F8FA),
        gutterBackground = Color(0xFFF6F8FA),
        gutterText = Color(0xFF8C959F),
        activeGutterText = Color(0xFF24292F),
        text = Color(0xFF24292F),
        keyword = Color(0xFFCF222E),
        type = Color(0xFF0550AE),
        string = Color(0xFF0A3069),
        number = Color(0xFF0550AE),
        comment = Color(0xFF6E7781),
        function = Color(0xFF8250DF),
        operator = Color(0xFFCF222E),
        tag = Color(0xFF116329),
        attribute = Color(0xFF0550AE),
        selection = Color(0xFFDDF4FF)
    )

    fun getTheme(mode: EditorThemeMode): EditorColors = when (mode) {
        EditorThemeMode.VS_CODE_DARK -> vsCodeDark
        EditorThemeMode.DRACULA -> dracula
        EditorThemeMode.MONOKAI -> monokai
        EditorThemeMode.CYBERPUNK -> cyberpunk
        EditorThemeMode.GITHUB_LIGHT -> githubLight
    }
}
