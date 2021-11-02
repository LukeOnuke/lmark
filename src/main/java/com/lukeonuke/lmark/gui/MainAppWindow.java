package com.lukeonuke.lmark.gui;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMarkApplication;
import com.lukeonuke.lmark.Registry;
import com.lukeonuke.lmark.event.CustomEvent;
import com.lukeonuke.lmark.event.SimpleScrollEvent;
import com.lukeonuke.lmark.gui.elements.FileCell;
import com.lukeonuke.lmark.gui.elements.Markdown;
import com.lukeonuke.lmark.util.*;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainAppWindow implements AppWindow {
    private Stage stage;
    private static final Logger logger = LoggerFactory.getLogger(AppWindow.class);
    private FileUtils fileUtils;
    private Registry registry = Registry.getInstance();
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
        AnchorPane fileBrowserContainer = new AnchorPane();
        FlowPane toolBar = new FlowPane();
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
        edit.getStyleClass().addAll("edit", "text-area", "0-br");
        edit.wrapTextProperty().set(true);

        AtomicBoolean isScrollListenerRegistered = new AtomicBoolean(false);
        edit.textProperty().addListener((observableValue, s, t1) -> {
            markdown.setMDContents(edit.getText());

            if (!autosaveEnabled) {
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
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown", "*.md", "*.MD"));
            /*fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));*/
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Any", "*.*"));

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                if (file.getPath().toLowerCase().endsWith(".md")) {
                    fileUtils.saveFile(file, edit.getText());
                }
                /*if(file.getPath().toLowerCase().endsWith(".pdf")){
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        PdfConverterExtension.exportToPdf(fos, markdown.getContents(), "", BaseRendererBuilder.TextDirection.LTR);
                        fileUtils.saveFile(file, edit.getText());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }*/
                if (file.getPath().toLowerCase().endsWith(".html")) {
                    fileUtils.saveFile(file, markdown.getContents());
                }
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
            SettingsWindow settingsWindow = new SettingsWindow();
            settingsWindow.show();
        });
        optionsMenu.getItems().add(cacheAndSettingsFolderOptions);

        Menu debug = new Menu("Debug");

        MenuItem showNonRenderedHTML = new MenuItem("Show non rendered HTML");
        showNonRenderedHTML.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            DebugWindow debugWindow = new DebugWindow(stage);
            debugWindow.setText(markdown.getContents());
            debugWindow.show();
        });

        MenuItem showArguments = new MenuItem("Show arguments");
        showArguments.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            DebugWindow debugWindow = new DebugWindow(stage);
            debugWindow.setText(LMarkApplication.getArguments().toString());
            debugWindow.show();
        });

        debug.getItems().addAll(showNonRenderedHTML, showArguments);

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
            OSIntegration.openWebpage("https://github.com/LukeOnuke/lmark");
        });
        help.getItems().add(aboutCode);


        Menu view = new Menu("View");

        MenuItem toggleRecent = new MenuItem("Toggle recent");
        toggleRecent.setOnAction(actionEvent -> {
            if (splitPane.getItems().contains(fileBrowserContainer)) {
                splitPane.setPrefWidth(splitPane.getWidth());
                splitPane.getItems().remove(fileBrowserContainer);
            } else {
                splitPane.getItems().add(0, fileBrowserContainer);
            }
        });
        view.getItems().add(toggleRecent);

        menuBar.getMenus().add(view);

        statusBar.setPrefHeight(25D);
        Label fileLabel = new Label();
        fileLabel.setText(FileUtils.detectCharset(fileUtils.getFile()) + " | " + fileUtils.getFile().getName());
        AnchorUtils.anchor(fileLabel, 5D, -1D, 5D, -1D);

        statusBar.getChildren().addAll(fileLabel);

        FlowPane files = new FlowPane(Orientation.VERTICAL);
        files.getStyleClass().add("bg-2");
        ScrollPane filesContainer = new ScrollPane(files);
        filesContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        filesContainer.setFitToHeight(true);
        filesContainer.setFitToWidth(true);
        filesContainer.getStyleClass().add("bg-1");

        files.setAlignment(Pos.CENTER);

        Arrays.stream(fileUtils.getParentFile().listFiles()).iterator().forEachRemaining(file -> {
            if (!file.isFile()) return;
            FileCell currentFile = new FileCell(file);
            currentFile.setOnMouseClicked(mouseEvent -> {
                fileUtils.setFile(currentFile.getFile());
            });
            files.getChildren().add(currentFile);
        });

        AnchorUtils.anchorAllSides(fileBrowserContainer, 0D);
        AnchorUtils.anchorAllSides(filesContainer, 0D);
        fileBrowserContainer.getChildren().addAll(filesContainer);


        Button boldButton = new Button("B");
        boldButton.getStyleClass().addAll("bold");
        boldButton.setTooltip(new Tooltip("CONTROL + B"));
        boldButton.setOnAction(actionEvent -> formatItalicize(edit, 2));

        Button italicButton = new Button("I");
        italicButton.getStyleClass().addAll("italic");
        italicButton.setTooltip(new Tooltip("CONTROL + I"));
        italicButton.setOnAction(actionEvent -> formatItalicize(edit, 1));

        Button boldItalicButton = new Button("BI");
        boldItalicButton.getStyleClass().addAll("bold", "italic");
        boldItalicButton.setTooltip(new Tooltip("CONTROL + J"));
        boldItalicButton.setOnAction(actionEvent -> formatItalicize(edit, 3));

        Button strikethroughButton = new Button("S");
        strikethroughButton.getStyleClass().add("strikethrough");
        strikethroughButton.setTooltip(new Tooltip("CONTROL + O"));
        strikethroughButton.setOnAction(actionEvent -> formatStrikethrough(edit));

        toolBar.getChildren().addAll(boldButton, italicButton, boldItalicButton, strikethroughButton);
        toolBar.setMaxHeight(25D);
        toolBar.getStyleClass().addAll("tool-bar", "bg-1", "bottom-border");
        toolBar.setHgap(15D);

        /*
         * ============================
         * ||   R E A D  F I L E     ||
         * ============================
         * */
        Runnable readFile = () -> {
            try {
                editScrollPane.set(null);
                String fileContents = fileUtils.readFile();
                edit.textProperty().set(fileContents);

                markdown.setMDContents(fileContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        fileUtils.registerFileListener(fileChangeEvent -> {
            logger.info("Reading " + fileChangeEvent.getNewValue());
            readFile.run();
        });
        fileUtils.setFile(fileUtils.getFile());
        readFile.run();

        //Add to splitpane
        splitPane.getItems().add(markdownContainer);
        splitPane.getItems().add(editContainer);
        //Add to root
        root.getChildren().add(splitPane);
        root.getChildren().add(menuBar);
        root.getChildren().add(statusBar);
        root.getChildren().add(toolBar);

        AnchorUtils.anchorAllSides(markdown.getNode(), 0D);
        AnchorUtils.anchorAllSides(markdownContainer, 0D);
        AnchorUtils.anchorAllSides(edit, 0D);
        AnchorUtils.anchor(splitPane, 60D, 25D, 0D, 0D);
        AnchorUtils.anchor(menuBar, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(statusBar, -1D, 0D, 0D, 0D);
        AnchorUtils.anchor(toolBar, 25D, -1D, 0D, 0D);

        //Init window
        Scene scene = new Scene(root, 640, 480);

        //Add css
        ThemeManager.getInstance().addCss(scene);

        stage.getIcons().add(new Image(ApplicationConstants.ICON));
        stage.setTitle(ApplicationConstants.MAIN_WINDOW_TITLE + " - " + fileUtils.getFile().getName());
        updateTitle();
        stage.setScene(scene);

        stage.setOnCloseRequest((event) -> {
            logger.info("Closing");

            stage.hide();

            if (autosaveEnabled) {
                save(edit.getText());
            }
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.S)) {
                save(edit.getText());
            }

            if ((keyEvent.getCode().equals(KeyCode.SPACE) || keyEvent.getCode().equals(KeyCode.PERIOD)) && autosaveEnabled) {
                save(edit.getText());
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.I)) {
                formatItalicize(edit, 1);
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.B)) {
                formatItalicize(edit, 2);
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.J)) {
                formatItalicize(edit, 3);
            }


            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.O)) {
                formatStrikethrough(edit);
            }
        });

        registry.registerRegistryChangeEvent(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED, autosaveEvent -> {
            autosaveEnabled = Boolean.parseBoolean( (String) autosaveEvent.getNewValue());
            FxUtils.lazyRunOnPlatform(this::updateTitle);
        });

        stage.show();
    }

    @Override
    public void hide() {
        stage.hide();
    }

    @Override
    public String getDescription() {
        return "Main window";
    }

    private void save(String text) {

        fileUtils.saveFile(fileUtils.getFile(), text);
        logger.info("Saved hash = " + text.hashCode());
    }

    private void updateTitle(){
        stage.setTitle(ApplicationConstants.MAIN_WINDOW_TITLE + " - " + fileUtils.getFile().getPath());
        if (autosaveEnabled){
            stage.setTitle(stage.getTitle() + " | autosave enabled");
        }
    }

    private void formatItalicize(TextArea textArea, int count) {
        formatSelection(textArea, count, '*');
    }

    private void formatStrikethrough(TextArea textArea) {
        formatSelection(textArea, 2, '~');
    }

    private void formatSelection(TextArea textArea, int count, char character) {
        if (textArea.getSelection() == null) return;
        if (textArea.getSelection().getStart() == 0 && textArea.getSelection().getEnd() == 0) return;
        boolean isFormatted = true;
        for (int i = 0; i < count; i++) {
            isFormatted = isFormatted && selectionIsFormattedWithChar(textArea, i, character);
        }
        if (isFormatted) {
            for (int i = 0; i < count; i++) {
                unformatSelection(textArea);
            }
        } else {
            for (int i = 0; i < count; i++) {
                formatWithChar(textArea, character);
            }
        }
    }

    private void unformatSelection(TextArea textArea) {
        IndexRange selection = textArea.getSelection();
        StringBuilder text = new StringBuilder(textArea.getText());
        text.replace(selection.getStart() - 1, selection.getStart(), "");
        text.replace(selection.getEnd() - 1, selection.getEnd(), "");
        textArea.setText(text.toString());
        textArea.selectRange(selection.getStart() - 1, selection.getEnd() - 1);
    }

    private boolean selectionIsFormattedWithChar(TextArea textArea, int offset, char character) {
        IndexRange selection = textArea.getSelection();
        return textArea.getText().charAt(selection.getStart() - 1 - offset) == character && textArea.getText().charAt(selection.getEnd() + offset) == character;
    }

    private void formatWithChar(TextArea textArea, char character) {
        IndexRange selection = textArea.getSelection();
        StringBuilder text = new StringBuilder(textArea.getText());
        text.insert(selection.getStart(), character);
        text.insert(selection.getEnd() + 1, character);
        textArea.setText(text.toString());
        textArea.selectRange(selection.getStart() + 1, selection.getEnd() + 1);
    }
}
