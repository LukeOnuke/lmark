package com.lukeonuke.lmark.gui;

import com.google.gson.reflect.TypeToken;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.gui.elements.FileCell;
import com.lukeonuke.lmark.gui.elements.Title;
import com.lukeonuke.lmark.util.AnchorUtils;
import com.lukeonuke.lmark.util.FileUtils;
import com.lukeonuke.lmark.util.ThemeManager;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        AnchorPane recentFilesContainer = new AnchorPane();

        open.getStyleClass().addAll("title");

        dropFileHere.getStyleClass().addAll("card", "center-text", "shadow", "bg-1");

        root.getStyleClass().add("gradient");


        ArrayList<String> recentFilesList = new ArrayList<>();
        File recentFilesStorage = FileUtils.getRelativeFile(ApplicationConstants.RECENT_FILES_STORAGE);
        try {
            if (!recentFilesStorage.exists()) {

                recentFilesStorage.createNewFile();
                FileUtils.writeJSON(recentFilesList, recentFilesStorage);

            }
            recentFilesList = FileUtils.readJSON(recentFilesStorage.getPath(),
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
            if (recentFiles.getSelectionModel().getSelectedItem() == null) return;
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

        root.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            File file = db.getFiles().get(0);

            try {
                FileUtils.getInstance(file.getPath());
                hide();

                FileUtils.addToRecents(file);

                MainAppWindow mainAppWindow = new MainAppWindow(stage);
                Platform.runLater(mainAppWindow::show);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        recentFilesContainer.getStyleClass().addAll("shadow", "bg-1", "rounded-top-corners");
        Label recentTitle = new Label("Recent files");
        recentTitle.getStyleClass().addAll("h2", "pd-l");
        Label recentDescription = new Label("Recent files appear below");
        recentDescription.getStyleClass().addAll("pd-l", "bottom-border", "pd-d");
        recentFilesContainer.getChildren().addAll(recentTitle, recentDescription, recentFiles);
        AnchorUtils.anchor(recentTitle, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(recentDescription, 30D, -1, 0D, 0D);
        AnchorUtils.anchor(recentFiles, 50D, 0D, 0D, 0D);


        AnchorUtils.anchor(open, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(dropFileHere, 80D, -1D, 100D, 100D);
        AnchorUtils.anchor(recentFilesContainer, 150D, 0D, 100D, 100D);

        root.getChildren().addAll(dropFileHere, open, recentFilesContainer);

        Scene scene = new Scene(root);

        ThemeManager.getInstance().addCss(scene);

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
