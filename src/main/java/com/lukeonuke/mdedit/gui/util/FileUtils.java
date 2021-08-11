package com.lukeonuke.mdedit.gui.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileUtils {
    public static String getResourceAsString(String path) throws IOException, NullPointerException {
        File file = new File(FileUtils.class.getResource(path).getPath());
        return Files.readString(file.toPath());
    }
}
