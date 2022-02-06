package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.util.FileUtils;
import com.lukeonuke.lmark.util.FxUtils;
import com.sun.source.tree.Tree;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

public class FileTreeView extends TreeView<String> {
    private SimpleObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
    private SimpleObjectProperty<Integer> recursionDepth = new SimpleObjectProperty<>();

    private Logger logger = LoggerFactory.getLogger(FileTreeView.class);

    public FileTreeView(File f) {
        this.getStyleClass().addAll("file-tree-view");
        fileProperty.addListener((observableValue, file, t1) -> {
            render();
        });

        recursionDepth.set(5);
        fileProperty.set(f);
    }

    private FileTreeView(TreeItem treeItem) {
        super(treeItem);
    }

    private void render(){
        Thread t = new Thread(() -> {
            TreeItem<String> root = getFileTree(fileProperty.get());
            root.setValue(fileProperty.get().getPath());

            FxUtils.lazyRunOnPlatform(() -> this.setRoot(root));
        });
        t.start();
    }

    private TreeItem<String> getTreeItem(String s){
        TreeItem<String> treeItem = new TreeItem<>();
        treeItem.setValue(s);
        return treeItem;
    }

    private TreeItem<String> constructTreeItem(File file){
        TreeItem<String> treeItem = getTreeItem(file.getName());
        treeItem.setGraphic(new FileGraphic(file));

        return treeItem;
    }

    private TreeItem<String> getFileTree(File file){
        //Get recursion depth
        if (file.getAbsolutePath().split(Pattern.quote(File.separator)).length - recursionDepth.get() >= fileProperty.get().getAbsolutePath().split(Pattern.quote(File.separator)).length) return constructTreeItem(file);

        if (file.isDirectory()) {
            TreeItem<String> directoryTree = constructTreeItem(file);
            if(file.listFiles() == null) return constructTreeItem(file);
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                if (FileUtils.supports(listFile) || listFile.isDirectory()) directoryTree.getChildren().add(getFileTree(listFile));
            }
            return directoryTree;
        }

        return constructTreeItem(file);
    }
}
