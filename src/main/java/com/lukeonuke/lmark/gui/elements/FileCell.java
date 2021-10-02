package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.gui.util.AnchorUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import java.io.File;

public class FileCell extends AnchorPane {
    File file;
    Label title;
    Label text;
    private FileCell() {

    }

    public FileCell(File path){
        file = path;

        title = new Label(path.getName());
        title.setFont(new Font(20D));
        title.getStyleClass().add("file-cell-title");
        AnchorUtils.anchorTopLeft(title, 5D, 5D);

        text = new Label(path.getPath());
        text.setStyle("-fx-text-fill: gray;");
        text.getStyleClass().add("file-cell-text");
        AnchorUtils.anchor(text, -1D, 5D, 5D, 5D);

        this.getChildren().addAll(title, text);
        this.setMinHeight(50D);

        this.getStyleClass().add("file-cell");
    }

    private FileCell(Node... nodes) {
        super(nodes);
    }

    public File getFile() {
        return file;
    }

    public double getLayoutWidth(){
        return Math.max(title.getLayoutBounds().getWidth(), text.getLayoutBounds().getWidth()) + this.getPadding().getLeft() + this.getPadding().getRight();
    }
}
