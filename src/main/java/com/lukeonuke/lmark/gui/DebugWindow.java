package com.lukeonuke.lmark.gui;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.util.ThemeManager;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DebugWindow implements AppWindow{
    Stage stage;
    TextArea textArea = new TextArea();

    public DebugWindow(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void show() {
        Scene scene = new Scene(textArea);
        ThemeManager.getInstance().addCss(scene);
        stage.getIcons().add(new Image(ApplicationConstants.ICON));
        stage.setTitle("lmark - Debug window");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void hide() {
        stage.hide();
    }

    @Override
    public String getDescription() {
        return null;
    }

    public void setText(String string){
        textArea.setText(string);
    }
}
