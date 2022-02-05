package com.lukeonuke.lmark;

import javafx.scene.text.Font;

public class ApplicationConstants {
    public static final String APPLICATION_CSS = "/gui/mainstyle-light.css";
    public static final String APPLICATION_DARK_CSS = "/gui/mainstyle-dark.css";
    public static final String WEB_MARKDOWN_CSS = "/gui/web/markdown-light.css";
    public static final String WEB_MARKDOWN_DARK_CSS = "/gui/web/markdown-dark.css";
    public static final String ICON = "/icon.png";
    public static final String RECENT_FILES_STORAGE = "recent-files.json";
    public static final String MAIN_WINDOW_TITLE = "lmark";
    public static final String PROPERTIES_AUTOSAVE_ENABLED = "autosaveEnabled";
    public static final String PROPERTIES_DARK_MODE_ENABLED = "darkModeEnabled";
    public static final String PROPERTIES_AUTO_DARK_MODE = "autoDarkMode";
    public static final String MARKDOWN_CACHE_PATH = "markdown-view-cache";
    public static final Font FONTS_AWESOME = Font.loadFont(LMark.class.getResourceAsStream("/gui/fa-6-solid.otf"), 12);
    public static final String TMP = "tmp/";
    public static final String APPDIR = ".lmark";
}
