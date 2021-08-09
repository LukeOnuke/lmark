package com.lukeonuke.mdedit;

import com.lukeonuke.mdedit.gui.util.AnchorUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        AnchorPane root = new AnchorPane();

        //Add css
        root.getStylesheets().add("style-light.css");
        //Initilise nodes
        SplitPane splitPane = new SplitPane();
        Text markdown = new Text("your-markdown");
        ScrollPane markdownContainer = new ScrollPane();
        ScrollPane editContainer = new ScrollPane();
        //Setup all stuffs
        markdownContainer.setContent(markdown);
        markdownContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        markdownContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        editContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        editContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);



        try {
            markdown.setText(Files.readString(new File("example.md").toPath()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //markdown.setContent("# mongus \\n amogus sus");

        //Add to splitpane
        splitPane.getItems().add(markdownContainer);
        splitPane.getItems().add(editContainer);
        //Add to root
        root.getChildren().add(splitPane);

        AnchorUtils.anchorAllSides(markdown, 0D);
        AnchorUtils.anchorAllSides(markdownContainer, 0D);
        AnchorUtils.anchorAllSides(splitPane, 0D);

        //Init window
        Scene scene = new Scene(root, 640, 480);
        stage.getIcons().add(new Image("icon.png"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}