package com.lukeonuke.lmark.gui.elements;

import com.lukeonuke.lmark.util.FileUtils;
import com.lukeonuke.lmark.util.FxUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

    private void render() {
        Thread t = new Thread(() -> {
            TreeItem<String> root = getFileTree(fileProperty.get());
            root.setValue(fileProperty.get().getPath());

            FxUtils.lazyRunOnPlatform(() -> this.setRoot(root));
        });
        t.start();
    }

    public void setFile(File file){
        fileProperty.set(file);
    }

    private TreeItem<String> getFileTree(File file) {
        //Get recursion depth
        if (file.getAbsolutePath().split(Pattern.quote(File.separator)).length - recursionDepth.get() >= fileProperty.get().getAbsolutePath().split(Pattern.quote(File.separator)).length)
            return new FileTreeItem(file);

        if (file.isDirectory()) {
            FileTreeItem directoryTree = new FileTreeItem(file);
            if (file.listFiles() == null) return new FileTreeItem(file);
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                if (FileUtils.supports(listFile) || listFile.isDirectory())
                    directoryTree.getChildren().add(getFileTree(listFile));
            }
            return directoryTree;
        }

        return new FileTreeItem(file);
    }
}
