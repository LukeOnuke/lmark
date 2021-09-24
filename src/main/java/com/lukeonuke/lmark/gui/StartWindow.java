package com.lukeonuke.lmark.gui;

import com.google.gson.reflect.TypeToken;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import com.lukeonuke.lmark.gui.util.AnchorUtils;
import com.lukeonuke.lmark.gui.util.FileUtils;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class StartWindow implements AppWindow {
    private Stage stage;
    private Logger logger;

    public StartWindow(Stage stage) {
        logger = LoggerFactory.getLogger(StartWindow.class);
        this.stage = stage;
    }

    @Override
    public void show() {
        AnchorPane root = new AnchorPane();

        Label dropFileHere = new Label("Drop file here");
        Label open = new Label("Open");
        open.getStyleClass().addAll("text", "title");

        ListView<String> recentFiles = new ListView<>();
        ArrayList<String> recentFilesList = new ArrayList<>();
        File recentFilesStorage = new File(ApplicationConstants.RECENT_FILES_STORAGE);
        try {
            if (!recentFilesStorage.exists()) {

                recentFilesStorage.createNewFile();
                FileUtils.writeJSON(recentFilesList, recentFilesStorage);

            }
            recentFilesList = FileUtils.readJSON(ApplicationConstants.RECENT_FILES_STORAGE,
                    new TypeToken<ArrayList<String>>() {
                    }.getType());

            if(recentFilesList == null){
                recentFilesList = new ArrayList<>();
            }

            recentFiles.getItems().addAll(recentFilesList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recentFiles.setOnMouseClicked(mouseEvent -> {
            String s = recentFiles.getSelectionModel().getSelectedItem();

            if(s == null) return;

            logger.info("Selected in recents" + s);
            try {
                FileUtils.getInstance(s);
                hide();
                MainAppWindow mainAppWindow = new MainAppWindow(new Stage());
                Platform.runLater(mainAppWindow::show);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        recentFiles.getFocusModel().focusedItemProperty().addListener((observableValue, s, t1) -> {

        });

        root.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            File file = db.getFiles().get(0);

            try {
                FileUtils.getInstance(file.getPath());
                hide();

                ArrayList<String> recent;
                try {
                    recent = FileUtils.readJSON(ApplicationConstants.RECENT_FILES_STORAGE,
                            new TypeToken<ArrayList<String>>() {
                            }.getType());

                    if(recent == null){
                        recent = new ArrayList<>();
                    }
                    if(!recent.contains(file.getPath())){
                        recent.add(file.getPath());
                    }

                    if(recent.size() >= 10){
                        recent.remove(0);
                    }

                    logger.info("Recent files " + recent);
                    FileUtils.writeJSON(recent, recentFilesStorage);
                }catch (IOException ex){
                    ex.printStackTrace();
                }
                logger.info("got 3");


                MainAppWindow mainAppWindow = new MainAppWindow(new Stage());
                Platform.runLater(mainAppWindow::show);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        AnchorUtils.anchorTopLeft(open, 5D, 5D);
        AnchorUtils.anchorTopLeft(dropFileHere, 60D, 5D);
        AnchorUtils.anchor(recentFiles, 80D, 0D, 0D, 0D);

        root.getChildren().addAll(dropFileHere, open, recentFiles);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(LMark.class.getResource(ApplicationConstants.APPLICATION_CSS).toExternalForm());

        stage.setScene(scene);
        stage.setTitle("LMark - home");
        stage.getIcons().add(new Image(ApplicationConstants.ICON));
        stage.show();
    }

    @Override
    public void hide() {
        stage.hide();
    }

    @Override
    public String getDescription() {
        return "Start screen";
    }
}
