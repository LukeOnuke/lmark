package com.lukeonuke.mdedit;

import com.lukeonuke.mdedit.gui.MainAppWindow;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class MDEdit extends Application {

    @Override
    public void start(Stage stage) {
        new MainAppWindow(stage).show();
    }

    public static void main(String[] args) {
        launch();
    }

}