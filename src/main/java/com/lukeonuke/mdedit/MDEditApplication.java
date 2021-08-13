package com.lukeonuke.mdedit;

import com.lukeonuke.mdedit.gui.MainAppWindow;
import com.lukeonuke.mdedit.gui.StartWindow;
import com.lukeonuke.mdedit.gui.util.FileUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

public class MDEditApplication extends Application {

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FileUtils.getInstance("example.md");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new StartWindow(primaryStage).show();
    }
}
