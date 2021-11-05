package com.lukeonuke.lmark.gui;

import com.jthemedetecor.OsThemeDetector;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.Registry;
import com.lukeonuke.lmark.gui.elements.Title;
import com.lukeonuke.lmark.util.AnchorUtils;
import com.lukeonuke.lmark.util.OSIntegration;
import com.lukeonuke.lmark.util.ThemeManager;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SettingsWindow implements AppWindow{
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private final Registry registry = Registry.getInstance();
    private final OsThemeDetector detector = OsThemeDetector.getDetector();

    private Stage stage;

    private CheckBox autoSave;
    private CheckBox automaticDarkMode;
    private CheckBox darkMode;
    @Override
    public void show() {
        AnchorPane root = new AnchorPane();
        root.getStyleClass().add("gradient");

        AnchorPane container = new AnchorPane();
        container.getStyleClass().addAll("shadow", "rounded-top-corners", "bg-1");

        autoSave = new CheckBox("Auto save enabled");
        autoSave.setSelected(true);

        automaticDarkMode = new CheckBox("Sync dark mode with system");
        automaticDarkMode.setSelected(true);
        automaticDarkMode.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            darkMode.setDisable(automaticDarkMode.isSelected());
            if (automaticDarkMode.isSelected()) {
                themeManager.setDark(detector.isDark());
                darkMode.setSelected(themeManager.isDark());
            }
        });


        darkMode = new CheckBox("Dark mode");
        darkMode.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            themeManager.setDark(darkMode.isSelected());
        });

        Title title = new Title("Settings");

        root.getChildren().addAll(container, title);

        container.getChildren().addAll(darkMode, automaticDarkMode, autoSave);

        AnchorUtils.anchor(title, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(container, 60D, 0D, 50D, 50D);
        AnchorUtils.anchorTopLeft(autoSave, 20D, 20D);
        AnchorUtils.anchorTopLeft(automaticDarkMode, 40D, 20D);
        AnchorUtils.anchorTopLeft(darkMode, 60D, 20D);

        reload();

        Scene scene = new Scene(root);
        themeManager.addCss(scene);
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Settings");
        stage.getIcons().add(new Image(ApplicationConstants.ICON));
        stage.setMinWidth(400D);
        stage.setMinHeight(600D);
        stage.setWidth(400D);
        stage.setHeight(600D);

        stage.setOnCloseRequest(windowEvent -> {
            stage.hide();
            save();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings");
            alert.setHeaderText("Your settings have been saved");
            alert.setContentText(null);
            alert.initOwner(stage.getScene().getWindow());
            alert.showAndWait();
        });

        stage.show();

        stage.toFront();
        stage.setAlwaysOnTop(true);
    }

    @Override
    public void hide() {

    }

    @Override
    public String getDescription() {
        return null;
    }

    private void reload(){
        registry.save();
        registry.refresh();
        autoSave.setSelected(registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED));
        automaticDarkMode.setSelected(registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_AUTO_DARK_MODE));
        darkMode.setSelected(registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_DARK_MODE_ENABLED));
        darkMode.setDisable(automaticDarkMode.isSelected());
    }

    private void save(){
        registry.write(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED, autoSave.isSelected());
        registry.write(ApplicationConstants.PROPERTIES_AUTO_DARK_MODE, automaticDarkMode.isSelected());
        registry.write(ApplicationConstants.PROPERTIES_DARK_MODE_ENABLED, darkMode.isSelected());
        registry.save();
    }
}
