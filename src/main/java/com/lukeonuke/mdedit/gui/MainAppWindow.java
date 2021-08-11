package com.lukeonuke.mdedit.gui;

import com.lukeonuke.mdedit.gui.AppWindow;
import com.lukeonuke.mdedit.gui.elements.Markdown;
import com.lukeonuke.mdedit.gui.util.AnchorUtils;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MainAppWindow implements AppWindow {
    private Stage stage;

    public MainAppWindow(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void show() {
        AnchorPane root = new AnchorPane();

        //Add css
        root.getStylesheets().add("/gui/style-light.css");

        //Initilise nodes
        SplitPane splitPane = new SplitPane();
        Markdown markdown = new Markdown();
        ScrollPane markdownContainer = new ScrollPane();
        AnchorPane editContainer = new AnchorPane();
        TextArea edit = new TextArea();
        //Setup all stuffs
        markdownContainer.setContent(markdown.getNode());
        markdownContainer.setFitToWidth(true);
        markdownContainer.setFitToHeight(true);
        markdownContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        editContainer.getChildren().add(edit);


        edit.wrapTextProperty().set(true);
        edit.textProperty().addListener((observableValue, s, t1) -> {
            markdown.setMDContents(s);
        });

        edit.scrollTopProperty().addListener((observableValue, number, t1) -> {

        });

        ScrollBar vBarEdit = (ScrollBar) edit.lookup(".scroll-bar:vertical");
        ScrollBar hBarEdit = (ScrollBar) edit.lookup(".scroll-bar:horizontal");
        ScrollBar vBarMarkdown = (ScrollBar) markdown.getNode().lookup(".scroll-bar:vertical");
        ScrollBar hBarMarkdown = (ScrollBar) markdown.getNode().lookup(".scroll-bar:horizontal");

        edit.scrollTopProperty().addListener(((observableValue, number, t1) -> {

        }));



        try {
            String fileContents = Files.readString(new File("example.md").toPath());
            edit.setText(fileContents);
            markdown.setMDContents(fileContents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Add to splitpane
        splitPane.getItems().add(markdownContainer);
        splitPane.getItems().add(editContainer);
        //Add to root
        root.getChildren().add(splitPane);

        AnchorUtils.anchorAllSides(markdown.getNode(), 0D);
        AnchorUtils.anchorAllSides(markdownContainer, 0D);
        AnchorUtils.anchorAllSides(edit, 0D);
        AnchorUtils.anchorAllSides(splitPane, 0D);

        //Init window
        Scene scene = new Scene(root, 640, 480);
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(scene);

        stage.onCloseRequestProperty().addListener((event) -> {
            Platform.exit();
            System.exit(0);
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
}
