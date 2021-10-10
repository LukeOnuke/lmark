package com.lukeonuke.lmark.gui;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import com.lukeonuke.lmark.Registry;
import com.lukeonuke.lmark.event.CustomEvent;
import com.lukeonuke.lmark.event.SimpleScrollEvent;
import com.lukeonuke.lmark.gui.elements.Markdown;
import com.lukeonuke.lmark.gui.util.AnchorUtils;
import com.lukeonuke.lmark.gui.util.FileUtils;
import com.lukeonuke.lmark.gui.util.OSIntegration;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainAppWindow implements AppWindow {
    private Stage stage;
    private static final Logger logger = LoggerFactory.getLogger(AppWindow.class);
    private FileUtils fileUtils;
    private Registry registry = new Registry();
    boolean autosaveEnabled = registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED);

    public MainAppWindow(Stage stage) {
        this.stage = stage;
        fileUtils = FileUtils.getInstance();
    }

    @Override
    public void show() {

        AnchorPane root = new AnchorPane();

        //Initilise nodes
        SplitPane splitPane = new SplitPane();
        Markdown markdown = new Markdown();
        ScrollPane markdownContainer = new ScrollPane();
        AnchorPane editContainer = new AnchorPane();
        TextArea edit = new TextArea();
        MenuBar menuBar = new MenuBar();
        AnchorPane statusBar = new AnchorPane();
        AtomicReference<ScrollPane> editScrollPane = new AtomicReference<>(null);
        //Setup all stuffs
        markdownContainer.setContent(markdown.getNode());
        markdownContainer.setFitToWidth(true);
        markdownContainer.setFitToHeight(true);
        markdownContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        markdown.getNode().addEventHandler(CustomEvent.CUSTOM_EVENT_TYPE, customEvent -> {
            if (editScrollPane.get() == null) return;
            if (!markdown.getNode().isHover()) return;
            editScrollPane.get().setVvalue(((SimpleScrollEvent) customEvent).getScrollPercentage());
        });

        editContainer.getChildren().add(edit);

        edit.getStyleClass().clear();
        edit.getStyleClass().add("edit");
        edit.wrapTextProperty().set(true);

        AtomicBoolean isScrollListenerRegistered = new AtomicBoolean(false);
        edit.textProperty().addListener((observableValue, s, t1) -> {
            markdown.setMDContents(edit.getText());

            if(!autosaveEnabled){
                stage.setTitle(ApplicationConstants.MAIN_WINDOW_TITLE + " - " + fileUtils.getFile().getName() + "*");
            }

            if (isScrollListenerRegistered.get()) return;
            //Run when size is calculated
            Platform.runLater(() -> {
                editScrollPane.set((ScrollPane) edit.getChildrenUnmodifiable().get(0));

                editScrollPane.get().vvalueProperty().addListener(observable -> {
                    //stop unwanted 2 way coupling
                    if (!editScrollPane.get().isHover()) return;
                    markdown.scrollTo(editScrollPane.get().getVvalue());

                });
                isScrollListenerRegistered.set(true);
            });
        });

        //Menu bar
        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        MenuItem openFile = new MenuItem("Open");
        openFile.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(new Stage());
            if (file == null) return;
            fileUtils.setFile(file);
        });
        fileMenu.getItems().add(openFile);

        MenuItem saveFile = new MenuItem("Save");
        saveFile.setOnAction(actionEvent -> {
            fileUtils.saveFile(fileUtils.getFile(), edit.getText());
        });
        fileMenu.getItems().add(saveFile);

        MenuItem saveFileAs = new MenuItem("Save As");
        saveFileAs.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                fileUtils.saveFile(file, edit.getText());
            }
        });
        fileMenu.getItems().add(saveFileAs);

        MenuItem openFilePath = new MenuItem("Open File Path");
        openFilePath.setOnAction(actionEvent -> {
            OSIntegration.openPathInExplorer(FileUtils.getInstance().getFile().getAbsoluteFile().getParent());
        });
        fileMenu.getItems().add(openFilePath);

        Menu optionsMenu = new Menu("Options");
        menuBar.getMenus().add(optionsMenu);

        MenuItem cacheAndSettingsFolderOptions = new MenuItem("Open settings");
        cacheAndSettingsFolderOptions.setOnAction(actionEvent -> {

        });
        optionsMenu.getItems().add(cacheAndSettingsFolderOptions);

        Menu debug = new Menu("Debug");

        MenuItem showNonRenderedHTML = new MenuItem("Show non rendered HTML");
        showNonRenderedHTML.setOnAction(actionEvent -> {
            TextArea nonRenderedHTML = new TextArea();
            nonRenderedHTML.setEditable(false);
            nonRenderedHTML.setText((String) markdown.getNode().getEngine().executeScript("document.documentElement.outerHTML"));
            Stage stage = new Stage();
            Scene scene = new Scene(nonRenderedHTML);
            stage.setScene(scene);
            stage.setTitle("Debug - non rendered html");
            stage.setIconified(false);
            stage.getIcons().add(new Image(ApplicationConstants.ICON));
            stage.show();
        });
        debug.getItems().add(showNonRenderedHTML);

        optionsMenu.getItems().add(debug);


        Menu help = new Menu("Help");
        menuBar.getMenus().add(help);

        MenuItem aboutCreators = new MenuItem("About Creator");
        aboutCreators.setOnAction(observable -> {
            OSIntegration.openWebpage("https://github.com/LukeOnuke");
        });
        help.getItems().add(aboutCreators);

        MenuItem aboutCode = new MenuItem("View code (on github)");
        aboutCode.setOnAction(observable -> {
            OSIntegration.openWebpage("https://github.com/LukeOnuke/mdedit");
        });
        help.getItems().add(aboutCode);

        statusBar.setPrefHeight(25D);
        Label fileLabel = new Label();
        fileLabel.setText(FileUtils.detectCharset(fileUtils.getFile()) + " | " + fileUtils.getFile().getName());
        AnchorUtils.anchor(fileLabel, 5D, -1D, 5D, -1D);

        statusBar.getChildren().addAll(fileLabel);

        /*
         * ============================
         * ||   R E A D  F I L E     ||
         * ============================
         * */
        try {
            editScrollPane.set(null);
            String fileContents = fileUtils.readFile();
            edit.textProperty().set(fileContents);

            markdown.setMDContents(fileContents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Add to splitpane
        splitPane.getItems().add(markdownContainer);
        splitPane.getItems().add(editContainer);
        //Add to root
        root.getChildren().add(splitPane);
        root.getChildren().add(menuBar);
        root.getChildren().add(statusBar);

        AnchorUtils.anchorAllSides(markdown.getNode(), 0D);
        AnchorUtils.anchorAllSides(markdownContainer, 0D);
        AnchorUtils.anchorAllSides(edit, 0D);
        AnchorUtils.anchor(splitPane, 25D, 25D, 0D, 0D);
        AnchorUtils.anchor(menuBar, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(statusBar, -1D, 0D, 0D, 0D);

        //Init window
        Scene scene = new Scene(root, 640, 480);

        //Add css
        scene.getStylesheets().add(LMark.class.getResource(ApplicationConstants.APPLICATION_CSS).toExternalForm());

        stage.getIcons().add(new Image(ApplicationConstants.ICON));
        stage.setTitle(ApplicationConstants.MAIN_WINDOW_TITLE + " - " + fileUtils.getFile().getName());
        if(autosaveEnabled){
            stage.setTitle(stage.getTitle() + " | autosave enabled");
        }
        stage.setScene(scene);

        stage.onCloseRequestProperty().addListener((event) -> {
            if(autosaveEnabled){
                save(edit.getText());
            }

            Platform.exit();
            System.exit(0);
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.S)) {
                save(edit.getText());
            }

            if((keyEvent.getCode().equals(KeyCode.SPACE) || keyEvent.getCode().equals(KeyCode.PERIOD)) && autosaveEnabled){
                save(edit.getText());
            }
        });

        stage.show();
    }

    @Override
    public void hide() {

    }

    @Override
    public String getDescription() {
        return "Main window";
    }

    private void save(String text){
        if (!autosaveEnabled) stage.setTitle(ApplicationConstants.MAIN_WINDOW_TITLE + " - " + fileUtils.getFile().getName());
        fileUtils.saveFile(fileUtils.getFile(), text);
    }
}
