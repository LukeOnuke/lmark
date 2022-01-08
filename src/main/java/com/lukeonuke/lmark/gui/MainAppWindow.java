package com.lukeonuke.lmark.gui;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMarkApplication;
import com.lukeonuke.lmark.Registry;
import com.lukeonuke.lmark.event.LinkStartHoverEvent;
import com.lukeonuke.lmark.event.LinkStopHoverEvent;
import com.lukeonuke.lmark.event.SimpleScrollEvent;
import com.lukeonuke.lmark.gui.elements.FileCell;
import com.lukeonuke.lmark.gui.elements.Markdown;
import com.lukeonuke.lmark.gui.elements.MarkdownArea;
import com.lukeonuke.lmark.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MainAppWindow implements AppWindow {
    private Stage stage;
    private static final Logger logger = LoggerFactory.getLogger(AppWindow.class);
    private FileUtils fileUtils;
    private Registry registry = Registry.getInstance();
    boolean autosaveEnabled = registry.readOptionAsBoolean(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED);
    private static final SimpleBooleanProperty isWorking = new SimpleBooleanProperty(false);
    boolean tampered = false;

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
        ScrollPane editContainer = new ScrollPane();
        MarkdownArea edit = new MarkdownArea();
        MenuBar menuBar = new MenuBar();
        FlowPane statusBar = new FlowPane(Orientation.HORIZONTAL);
        ProgressBar statusProgress = new ProgressBar();
        StackPane statusBarContainer = new StackPane(statusProgress, statusBar);
        AnchorPane fileBrowserContainer = new AnchorPane();
        FlowPane toolBar = new FlowPane();
        //Setup all stuffs
        markdownContainer.setContent(markdown.getNode());
        markdownContainer.setFitToWidth(true);
        markdownContainer.setFitToHeight(true);
        markdownContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        markdown.getNode().addEventHandler(SimpleScrollEvent.SIMPLE_SCROLL_EVENT_TYPE, lmarkEvent -> {
            //if (editScrollPane.get() == null) return;
            if (!markdown.getNode().isHover()) return;
            edit.setScrollY(lmarkEvent.getScrollPercentage());
        });

        Label hoveredLink = new Label();
        markdown.getNode().addEventHandler(LinkStartHoverEvent.LINK_START_HOVER_EVENT_TYPE, linkStartHoverEvent -> {
            String href = linkStartHoverEvent.getAnchorElement().getHref();
            hoveredLink.setText(href);
            if (!statusBar.getChildren().contains(hoveredLink)) statusBar.getChildren().add(hoveredLink);
        });
        markdown.getNode().addEventHandler(LinkStopHoverEvent.LINK_STOP_HOVER_EVENT_TYPE, linkStopHoverEvent -> {
            hoveredLink.setText("");
            statusBar.getChildren().remove(hoveredLink);
        });

        editContainer.setContent(edit);
        editContainer.setFitToWidth(true);
        editContainer.setFitToHeight(true);
        editContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        edit.getStyleClass().clear();
        edit.getStyleClass().addAll("edit", "text-area", "0-br");

        edit.replaceText("");
        edit.textProperty().addListener((observableValue, s, t1) -> {
            markdown.setMDContents(edit.getText());
            if(edit.getCaretPosition() == edit.getLength() - 1) markdown.scrollTo(1D);
            if (!s.equals("")) tampered = true;
            updateTitle();

            //Run when size is calculated
            /*Platform.runLater(() -> {
                editScrollPane.set((ScrollPane) edit.getChildrenUnmodifiable().get(0));

                editScrollPane.get().vvalueProperty().addListener(observable -> {
                    //stop unwanted 2 way coupling
                    if (!editScrollPane.get().isHover()) return;
                    markdown.scrollTo(editScrollPane.get().getVvalue());

                });
                isScrollListenerRegistered.set(true);
            });*/
        });

        edit.estimatedScrollYProperty().addListener((observableValue, aDouble, t1) -> {
            if(!edit.isHover()) return;
            markdown.scrollTo(edit.getScrollY());
        });

        //Menu bar
        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        MenuItem openFile = new MenuItem("Open");
        openFile.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(new Stage());
            if (file == null) return;
            logger.info(file.getPath());
            if (autosaveEnabled) save(edit.getText());
            fileUtils.setFile(file);
        });
        fileMenu.getItems().add(openFile);

        final Menu openRecent = new Menu("Open recent");
        fileMenu.getItems().add(openRecent);
        addRecentToMenu(fileMenu, openRecent);

        MenuItem saveFile = new MenuItem("Save");
        saveFile.setOnAction(actionEvent -> fileUtils.saveFile(fileUtils.getFile(), edit.getText()));
        fileMenu.getItems().add(saveFile);

        MenuItem saveFileAs = new MenuItem("Save As");
        saveFileAs.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown", "*.md", "*.MD"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Any", "*.*"));

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                if (file.getPath().toLowerCase().endsWith(".md")) {
                    fileUtils.saveFile(file, edit.getText());
                }
                if (file.getPath().toLowerCase().endsWith(".pdf")) {
                    writePDFToFile(markdown, file);
                }
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

        MenuItem print = new MenuItem("Print");
        print.setOnAction(actionEvent -> print(markdown));
        fileMenu.getItems().add(print);

        Menu documentMenu = new Menu("Document");

        MenuItem resizePreview = new MenuItem("Resize preview to pdf width");
        resizePreview.setOnAction(actionEvent -> {
            setWidthToA4(markdownContainer, splitPane);
        });

        MenuItem undo = new MenuItem("Undo (CTRL + Z)");
        undo.setOnAction(actionEvent -> edit.undo());

        MenuItem redo = new MenuItem("Redo (CTRL + Y)");
        redo.setOnAction(actionEvent -> edit.redo());

        documentMenu.getItems().addAll(undo, redo);

        menuBar.getMenus().add(documentMenu);


        Menu optionsMenu = new Menu("Options");
        menuBar.getMenus().add(optionsMenu);

        MenuItem cacheAndSettingsFolderOptions = new MenuItem("Open settings");
        cacheAndSettingsFolderOptions.setOnAction(actionEvent -> {
            SettingsWindow settingsWindow = new SettingsWindow();
            settingsWindow.show();
        });

        MenuItem openProgramFolder = new MenuItem("Open program folder");
        openProgramFolder.setOnAction(actionEvent -> {
            OSIntegration.openPathInExplorer(FileUtils.getRelativeFile());
        });

        optionsMenu.getItems().addAll(cacheAndSettingsFolderOptions, openProgramFolder);

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

        MenuItem toggleRecent = new MenuItem("Toggle arround files");
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

        //Status bar
        statusBar.setPrefHeight(25D);
        statusBar.getStyleClass().addAll("status-bar");
        Label fileLabel = new Label();
        fileLabel.setText(FileUtils.detectCharset(fileUtils.getFile()) + " | " + fileUtils.getFile().getName());
        AnchorUtils.anchor(fileLabel, 5D, -1D, 5D, -1D);

        statusBar.getChildren().addAll(fileLabel);

        statusProgress.setOpaqueInsets(new Insets(0));
        statusProgress.setProgress(0);
        statusProgress.setMaxHeight(Double.MAX_VALUE);
        statusProgress.setMaxWidth(Double.MAX_VALUE);
        isWorking.addListener(observable -> FxUtils.lazyRunOnPlatform(() -> {
            if (isWorking.get()) {
                statusProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            } else {
                statusProgress.setProgress(0);
            }
        }));
        statusBarContainer.prefWidthProperty().bind(statusProgress.minWidthProperty());
        statusBarContainer.prefHeightProperty().bind(statusProgress.minHeightProperty());

        StackPane.setMargin(statusBar, new Insets(0D));
        StackPane.setMargin(statusProgress, new Insets(0D));

        AnchorUtils.anchorAllSides(statusBar, 0D);
        AnchorUtils.anchorAllSides(statusProgress, 0D);

        //Arround files
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

        Button saveButton = FxUtils.createToolBarButton("\uF0C7", "CONTROL + S",
                actionEvent -> save(edit.getText()));

        Button printButton = FxUtils.createToolBarButton("\uF02F", "CONTROL + P",
                actionEvent -> print(markdown));

        Button boldButton = FxUtils.createToolBarButton("\uf032", "CONTROL + B",
                actionEvent -> edit.formatItalicize(2));

        Button italicButton = FxUtils.createToolBarButton("\uf033", "CONTROL + I",
                actionEvent -> edit.formatItalicize( 1));

        Button boldItalicButton = FxUtils.createToolBarButton("\uf032\uf033", "CONTROL + J",
                actionEvent -> edit.formatItalicize(3));

        Button strikethroughButton = FxUtils.createToolBarButton("\uf0cc", "CONTROL + O",
                actionEvent -> edit.formatStrikethrough());

        Button checkBoxButton = FxUtils.createToolBarButton("\uF14A", "CONTROL + R",
                actionEvent -> edit.checkListBulletFormat());

        Button bulletButton = FxUtils.createToolBarButton("\uF111", "CONTROL + E",
                actionEvent -> edit.dotBulletFormat());

        Button headingButton = FxUtils.createToolBarButton("\uf1dc", "CONTROL + T",
                actionEvent -> edit.titleFormat());

        toolBar.getChildren()
                .addAll(
                        saveButton,
                        printButton,
                        boldButton,
                        italicButton,
                        boldItalicButton,
                        strikethroughButton,
                        bulletButton,
                        checkBoxButton,
                        headingButton);
        toolBar.setMaxHeight(25D);
        toolBar.getStyleClass().addAll("tool-bar", "bg-1", "bottom-border");
        toolBar.setHgap(15D);

        /*
         * ============================
         * ||   R E A D  F I L E     ||
         * ============================
         * */
        fileUtils.registerFileListener(fileChangeEvent -> {
            if(fileChangeEvent.getOldValue() != null && autosaveEnabled) save(edit.getText(), (File) fileChangeEvent.getOldValue());
            readFileAndSet(edit, markdown);
            hoveredLink.setText("");
            statusBar.getChildren().remove(hoveredLink);

            addRecentToMenu(fileMenu, openRecent);
        });
        fileUtils.setFile(fileUtils.getFile());
        readFileAndSet(edit, markdown);

        //Add to splitpane
        splitPane.getItems().add(markdownContainer);
        splitPane.getItems().add(editContainer);
        //Add to root
        root.getChildren().add(splitPane);
        root.getChildren().add(menuBar);
        root.getChildren().add(statusBarContainer);
        root.getChildren().add(toolBar);

        AnchorUtils.anchorAllSides(markdown.getNode(), 0D);
        AnchorUtils.anchorAllSides(markdownContainer, 0D);
        AnchorUtils.anchorAllSides(edit, 0D);
        AnchorUtils.anchor(splitPane, 60D, 25D, 0D, 0D);
        AnchorUtils.anchor(menuBar, 0D, -1D, 0D, 0D);
        AnchorUtils.anchor(statusBarContainer, -1D, 0D, 0D, 0D);
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
            logger.info("Close request");

            if (autosaveEnabled) {
                save(edit.getText());
            } else {
                if (tampered) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("Are you sure you want to exit?");
                    alert.setContentText("Changes arent saved");

                    alert.initOwner(stage.getScene().getWindow());
                    alert.setTitle(alert.getHeaderText());
                    Optional<ButtonType> response = alert.showAndWait();
                    if (response.isPresent()) {
                        if (response.get().equals(ButtonType.CANCEL)) {
                            logger.info("Didnt close, going back to ");
                            event.consume();
                            return;
                        }
                    }
                }
            }
            stage.hide();
            Platform.exit();
            System.exit(0);
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.S)) {
                save(edit.getText());
            }

            if ((keyEvent.getCode().equals(KeyCode.SPACE) || keyEvent.getCode().equals(KeyCode.PERIOD)) && autosaveEnabled) {
                save(edit.getText());
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.I)) {
                edit.formatItalicize(1);
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.B)) {
                edit.formatItalicize(2);
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.J)) {
                edit.formatItalicize(3);
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.O)) {
                edit.formatStrikethrough();
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.E)) {
                edit.dotBulletFormat();
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.R)) {
                edit.checkListBulletFormat();
            }

            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.T)) {
                edit.titleFormat();
            }

            if(keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.P)){
                print(markdown);
            }
        });

        registry.registerRegistryChangeEvent(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED, autosaveEvent -> {
            autosaveEnabled = Boolean.parseBoolean((String) autosaveEvent.getNewValue());
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

    static volatile int workAmount = 0;
    private synchronized static void setIsWorking(boolean bool) {
        if (!bool) {
            workAmount--;
            if (workAmount <= 0) {
                isWorking.set(false);
            }
        }

        if (bool) {
            workAmount++;
            isWorking.set(true);
        }
    }

    private void save(String text, File file) {
        setIsWorking(true);
        fileUtils.saveFile(file, text);
        tampered = false;
        updateTitle();
        logger.info("Saved hash = " + text.hashCode());
        setIsWorking(false);
    }

    private void save(String text){
        save(text, fileUtils.getFile());
    }

    private void updateTitle() {
        stage.setTitle(ApplicationConstants.MAIN_WINDOW_TITLE + " - " + fileUtils.getFile().getPath());
        if (autosaveEnabled) {
            stage.setTitle(stage.getTitle() + " | autosave enabled");
        } else {
            if (tampered) stage.setTitle(stage.getTitle() + "*");
        }
    }

    private void readFileAndSet(MarkdownArea edit, Markdown markdown) {
        logger.info("Reading " + fileUtils.getFile().getPath());
        try {
            updateTitle();
            String fileContents = FileUtils.readSpecifiedFile(fileUtils.getFile());

            if (!fileContents.contains(System.lineSeparator())) {
                fileContents = fileContents.replace("\n", System.lineSeparator());
            }

            edit.replaceText(fileContents);
            markdown.setMDContents(fileContents);

            Platform.runLater(() -> edit.setScrollY(0D));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setWidthToA4(ScrollPane markdownContainer, SplitPane splitPane) {
        int index = splitPane.getItems().indexOf(markdownContainer);
        logger.info("Index " + index);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        double width = toolkit.getScreenResolution() * 8.26D;

        logger.info("[{}] Resolution is {}, width is {}", index, toolkit.getScreenResolution(), width);

        if (index == 0) {
            splitPane.setDividerPosition(0, width);
        } else {
            splitPane.setDividerPosition(index + 1, splitPane.getDividerPositions()[index] + width);
        }
    }

    private void writePDFToFile(Markdown markdown, File file) {
        setIsWorking(true);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            /*PdfConverterExtension.exportToPdf(fos, markdown.getContents(), "", Markdown.getOptions());*/

            ConverterProperties converterProperties = new ConverterProperties();
            converterProperties.setCharset(StandardCharsets.UTF_8.name());
            converterProperties.setBaseUri(fileUtils.getParentFile().getPath());
            HtmlConverter.convertToPdf(markdown.getPDFReadyDocument(), fos, converterProperties);

            fos.flush();
        } catch (IllegalArgumentException e) {
            //swalla
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PDF export error");
            alert.setHeaderText("An error occured whilst exporting " + file.getPath() + " to pdf");
            alert.setContentText(ex.getMessage());
            alert.initOwner(stage);
            alert.show();
        }
        setIsWorking(false);
    }

    private void print(Markdown markdown) {
        Thread t = new Thread(() -> {
            setIsWorking(true);
            Instant instant = Instant.now();
            File tmp = FileUtils.getRelativeFile(ApplicationConstants.TMP + instant.toEpochMilli() + ".pdf");

            writePDFToFile(markdown, tmp);
            try {
                PDDocument document = PDDocument.load(tmp);

                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(document));
                if (job.printDialog()) {
                    job.print();
                }

                document.close();
            } catch (PrinterAbortException printerAbortException) {
                FxUtils.lazyRunOnPlatform(() -> {
                    FxUtils.createAlert(Alert.AlertType.ERROR, "Printing error",
                                    "The print-job was unexpectedly aborted", printerAbortException.getMessage(), stage)
                            .show();
                });
            } catch (IOException | PrinterException ex) {
                FxUtils.lazyRunOnPlatform(() -> {
                    FxUtils.createAlert(Alert.AlertType.ERROR, "General printing error",
                                    "A error occurred whilst sending the document to the printer",
                                    ex.getMessage(), stage)
                            .show();
                });
            }
            setIsWorking(false);
        }, "print-worker");
        t.start();
    }

    private void addRecentToMenu(Menu fileMenu, Menu openRecent){
        try {
            ArrayList<String> recentsList = FileUtils.getRecentFiles();
            openRecent.getItems().clear();
            recentsList.forEach(s -> {
                MenuItem menuItem = new MenuItem(s);
                menuItem.setOnAction(actionEvent -> fileUtils.setFile(new File(s)));
                openRecent.getItems().add(menuItem);
            });
        } catch (IOException ioex) {
            logger.info("Couldn't load (file > recent)", ioex.getCause());
            fileMenu.getItems().remove(openRecent);
        }
    }
}
