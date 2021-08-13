package com.lukeonuke.mdedit.gui.util;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;

public class FileUtils {
    private static FileUtils instance;
    private File file;

    public static FileUtils getInstance(String path) throws FileNotFoundException{
        if(instance == null){
            instance = new FileUtils(path);
        }
        return instance;
    }

    public static FileUtils getInstance() {
        return instance;
    }

    private FileUtils(String path) throws FileNotFoundException{
        file = new File(path).getAbsoluteFile();

        if (!file.exists()) {
            throw new FileNotFoundException("File not found");
        }
    }

    public static String getResourceAsString(String path) throws IOException, NullPointerException {
        File file = new File(FileUtils.class.getResource(path).getPath());
        return Files.readString(file.toPath());
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String readFile() throws IOException {
        return Files.readString(file.toPath(), Charset.forName(detectCharset(file)));
    }

    public void saveFile(File file, String string){
        try {
            Files.write(file.toPath(), string.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String detectCharset(File file){
        String charset = null;
        try {
            charset = UniversalDetector.detectCharset(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(charset == null){
            return "UTF-8";
        }
        return charset;
    }
}
