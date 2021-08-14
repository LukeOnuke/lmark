package com.lukeonuke.mdedit.gui;

import com.lukeonuke.mdedit.gui.util.AnchorUtils;
import com.lukeonuke.mdedit.gui.util.FileUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;

public class StartWindow implements AppWindow{
    private Stage stage;

    public StartWindow(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void show() {
        AnchorPane root = new AnchorPane();

        Label label = new Label("Drop file here");

        root.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            File file = db.getFiles().get(0);

            try {
                FileUtils.getInstance(file.getPath());
                hide();
                MainAppWindow mainAppWindow = new MainAppWindow(new Stage());
                mainAppWindow.show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        AnchorUtils.anchorAllSides(label, 200D);

        root.getChildren().addAll(label);

        Scene scene =new Scene(root);
        stage.setScene(scene);
        stage.setTitle("MDEdit - home");
        stage.getIcons().add(new Image("icon.png"));
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
