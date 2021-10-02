package com.lukeonuke.lmark.gui;

import com.google.gson.reflect.TypeToken;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import com.lukeonuke.lmark.gui.elements.FileCell;
import com.lukeonuke.lmark.gui.elements.Title;
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
import java.util.concurrent.atomic.AtomicReference;

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
        Title open = new Title("Open");
        ListView<FileCell> recentFiles = new ListView<>();

        open.getStyleClass().addAll("title");

        dropFileHere.getStyleClass().addAll("card", "center-text", "shadow", "bg-1");

        root.getStyleClass().add("gradient");

        recentFiles.getStyleClass().addAll("shadow");

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

            recentFilesList.forEach(s -> {
                recentFiles.getItems().add(new FileCell(new File(s)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        recentFiles.setOnMouseClicked(mouseEvent -> {
            String s = recentFiles.getSelectionModel().getSelectedItem().getFile().getPath();

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
                        //TODO: Add recent files from bottom to top
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

        AnchorUtils.anchor(open, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(dropFileHere, 80D, -1D, 100D, 100D);
        AnchorUtils.anchor(recentFiles, 150D, 0D, 100D, 100D);

        root.getChildren().addAll(dropFileHere, open, recentFiles);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(LMark.class.getResource(ApplicationConstants.APPLICATION_CSS).toExternalForm());

        stage.setScene(scene);
        stage.setTitle("LMark - home");
        stage.getIcons().add(new Image(ApplicationConstants.ICON));
        stage.show();

        AtomicReference<Double> maxListPrefWidth = new AtomicReference<>((double) 0);
        recentFiles.getItems().forEach(node -> {
            if(maxListPrefWidth.get() < node.getLayoutWidth()){
                maxListPrefWidth.set(node.getLayoutWidth());
            }
        });

        stage.setWidth(maxListPrefWidth.get() + 400D);
        stage.centerOnScreen();
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
