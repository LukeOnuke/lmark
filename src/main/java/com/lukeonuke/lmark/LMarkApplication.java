package com.lukeonuke.lmark;

import com.lukeonuke.lmark.gui.MainAppWindow;
import com.lukeonuke.lmark.gui.StartWindow;
import com.lukeonuke.lmark.util.FileUtils;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class LMarkApplication extends Application {

    private static List<String> arguments;
    private final Logger logger = LoggerFactory.getLogger(LMarkApplication.class);

    public static void launchApp(String[] args) {
        arguments = Arrays.asList(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Virtual working directory " + FileUtils.getRelativeFile().getPath());

        if (!arguments.isEmpty()) {
            logger.info("Found arguments");
            arguments.forEach(logger::info);


            arguments.forEach(s -> {
                try {
                    File file = new File(s);
                    if (file.exists() && file.isFile()) {
                        FileUtils.getInstance(file.getPath());
                        new MainAppWindow(primaryStage).show();
                        return;
                    }
                } catch (Exception ex) {
                    logger.info(s);
                }
            });

        }else{
            new StartWindow(primaryStage).show();
        }
    }

    public static List<String> getArguments() {
        return arguments;
    }
}
