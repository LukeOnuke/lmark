package com.lukeonuke.lmark;

import com.lukeonuke.lmark.gui.StartWindow;
import com.lukeonuke.lmark.gui.util.FileUtils;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class LMarkApplication extends Application {

    private static List<String> arguments;

    public static void launchApp(String[] args) {
        arguments =  Arrays.asList(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            if(arguments.size() > 0){
                FileUtils.getInstance("example.md");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new StartWindow(primaryStage).show();
    }
}
