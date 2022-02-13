package com.lukeonuke.lmark.gui.elements;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.io.File;

public class FileTreeItem extends TreeItem<String> {
    File file;
    public FileTreeItem(File file) {
        this.file = file;

        this.setValue(file.getName());
        this.setGraphic(new FileGraphic(file));
    }

    private FileTreeItem(String s) {
        super(s);
    }

    private FileTreeItem(String s, Node node) {
        super(s, node);
    }

    public File getFile() {
        return file;
    }
}
