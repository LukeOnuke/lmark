package com.lukeonuke.lmark.gui.elements;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import javafx.scene.text.Text;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

public class FileGraphic extends Text {
    private static HashMap<String, String> graphics;

    static {
        try {
            graphics = new Gson().fromJson(Files.newBufferedReader(Path.of(LMark.class.getResource("/graphics.json").toURI())), new TypeToken<HashMap<String, String>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            //unrecoverable
        }
    }

    public FileGraphic(File file) {
        this.getStyleClass().addAll("file-graphic");
        this.setFont(ApplicationConstants.FONTS_AWESOME);
        if (file.isDirectory()) {setTextFromGraphics("dir"); return;};
        String name = file.getName();
        setTextFromGraphics(name.substring(name.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT));
    }

    private void setTextFromGraphics(String key) {
        if (!graphics.containsKey(key)) key = "unknown";
        this.setText(graphics.get(key));
    }

    private FileGraphic(String s) {
        super(s);
    }

    private FileGraphic(double v, double v1, String s) {
        super(v, v1, s);
    }

    @Override
    public String toString() {
        return FileGraphic.class.getName() + ":" + this.getText();
    }
}
