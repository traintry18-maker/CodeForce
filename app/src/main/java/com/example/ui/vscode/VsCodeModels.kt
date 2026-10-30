package com.example.ui.vscode

import com.example.data.model.CodeFileEntity

enum class VsCodeActivity(val title: String, val badgeCount: Int = 0) {
    EXPLORER("Explorer"),
    SEARCH("Search"),
    SOURCE_CONTROL("Source Control"),
    RUN_AND_DEBUG("Run and Debug"),
    EXTENSIONS("Extensions"),
    TESTING("Testing"),
    PYTHON("Python"),
    CLOUD_DEPLOY("Cloud Deploy"),
    SETTINGS("Settings")
}

enum class VsCodeBottomTab(val title: String) {
    PROBLEMS("PROBLEMS"),
    OUTPUT("OUTPUT"),
    DEBUG_CONSOLE("DEBUG CONSOLE"),
    TERMINAL("TERMINAL"),
    WEB_PREVIEW("WEB PREVIEW")
}

data class VsCodeExtension(
    val id: String,
    val name: String,
    val publisher: String,
    val version: String,
    val description: String,
    val rating: Float,
    val downloads: String,
    val isEnabled: Boolean = true
)

data class VsCodeSearchResult(
    val fileId: Long,
    val fileName: String,
    val lineNumber: Int,
    val lineText: String,
    val matchStart: Int,
    val matchEnd: Int
)

data class CommandPaletteItem(
    val id: String,
    val title: String,
    val category: String,
    val shortcut: String = "",
    val action: () -> Unit
)

data class EditorSettings(
    val fontFamily: String = "JetBrains Mono",
    val fontSize: Float = 13.5f,
    val tabSize: Int = 2,
    val wordWrap: Boolean = false,
    val lineNumbers: String = "on", // "on", "off", "relative"
    val minimap: Boolean = true,
    val bracketPairColorization: Boolean = true,
    val autoSave: String = "afterDelay", // "afterDelay", "onFocusChange", "off"
    val cursorBlinking: String = "blink", // "blink", "smooth", "solid"
    val statusBarVisible: Boolean = true,
    val breadcrumbsVisible: Boolean = true,
    val colorTheme: String = "VS Code Dark+"
)

data class AgentChatMessage(
    val id: String,
    val isUser: Boolean,
    val message: String,
    val codeBlock: String? = null,
    val timestamp: String = "",
    val isGenerating: Boolean = false
)

data class MenuItemDef(
    val label: String,
    val shortcut: String = "",
    val isDivider: Boolean = false,
    val hasSubmenu: Boolean = false,
    val isChecked: Boolean? = null,
    val onClick: () -> Unit = {}
)
