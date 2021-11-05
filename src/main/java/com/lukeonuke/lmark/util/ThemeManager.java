package com.lukeonuke.lmark.util;

import com.jthemedetecor.OsThemeDetector;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import com.lukeonuke.lmark.Registry;
import com.lukeonuke.lmark.gui.elements.Markdown;
import javafx.application.Platform;
import javafx.scene.Scene;

import java.util.ArrayList;

public class ThemeManager {
    private static ThemeManager themeManager;
    private ArrayList<Scene> scenes = new ArrayList<>();
    private ArrayList<Markdown> markdowns = new ArrayList<>();
    final OsThemeDetector detector = OsThemeDetector.getDetector();
    final Registry registry = Registry.getInstance();
    private boolean dark = false;
    private ThemeManager() {
        setDark(registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_DARK_MODE_ENABLED));

        if(registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_AUTO_DARK_MODE)) setDark(detector.isDark());

        detector.registerListener(aBoolean -> {
            if(!registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_AUTO_DARK_MODE)) return;
            setDark(detector.isDark());
        });
    }

    public static ThemeManager getInstance(){
        if (themeManager == null) {
            themeManager = new ThemeManager();
        }

        return themeManager;
    }

    public void addCss(Scene scene){
        scene.getStylesheets().add(getCurrentCss());
        if (!scenes.contains(scene)) scenes.add(scene);
    }

    private String getCurrentCss(){
        if(dark){
            return LMark.class.getResource(ApplicationConstants.APPLICATION_DARK_CSS).toExternalForm();
        }
        return LMark.class.getResource(ApplicationConstants.APPLICATION_CSS).toExternalForm();
    }

    public void setDark(boolean dark) {
        this.dark = dark;
        registry.write(ApplicationConstants.PROPERTIES_DARK_MODE_ENABLED, this.dark);
        scenes.forEach(scene -> {
            Platform.runLater(() -> {
                scene.getStylesheets().remove(LMark.class.getResource(ApplicationConstants.APPLICATION_DARK_CSS).toExternalForm());
                scene.getStylesheets().remove(LMark.class.getResource(ApplicationConstants.APPLICATION_CSS).toExternalForm());

                addCss(scene);
            });
        });

        markdowns.forEach(markdown -> Platform.runLater(markdown::refresh));
    }

    public boolean isDark() {
        return dark;
    }

    public String getWebCSS(Markdown markdown){
        if(!markdowns.contains(markdown)) markdowns.add(markdown);

        if(dark) return ApplicationConstants.WEB_MARKDOWN_DARK_CSS;
        return ApplicationConstants.WEB_MARKDOWN_CSS;
    }
}
