package com.lukeonuke.lmark.gui.util;

import com.google.gson.Gson;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtils {
    private static FileUtils instance;
    private File file;

    public static FileUtils getInstance(String path) throws FileNotFoundException {
        if (instance == null) {
            instance = new FileUtils(path);
        }
        return instance;
    }

    public static FileUtils getInstance() {
        return instance;
    }

    private FileUtils(String path) throws FileNotFoundException {
        file = new File(path).getAbsoluteFile();

        if (!file.exists()) {
            throw new FileNotFoundException("File not found");
        }
    }

    public static String getResourceAsString(String path) throws IOException, NullPointerException {
        StringBuilder sb = new StringBuilder();
        InputStream is = FileUtils.class.getResourceAsStream(path);
        try (InputStreamReader streamReader =
                     new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;

            int count = 0;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
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

    public void saveFile(File file, String string) {
        try {
            Files.write(file.toPath(), string.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String detectCharset(File file) {
        String charset = null;
        try {
            charset = UniversalDetector.detectCharset(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (charset == null) {
            return "UTF-8";
        }
        return charset;
    }

    public static String stripProtocolFromPath(String path) {
        String reFormatted = path;
        if(path.indexOf(':') != -1){
            reFormatted = path.substring(path.indexOf(':') + 1);
        }

        while (reFormatted.startsWith("/") || reFormatted.startsWith("\\")) {
            reFormatted = reFormatted.substring(1);
        }

        return reFormatted;
    }

    public static void writeJSON(Object source, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        Gson gson = new Gson();
        gson.toJson(source, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public static <T> T readJSON(String file, Type type) throws IOException {
        FileReader fileReader = new FileReader(file);
        Gson gson = new Gson();
        T memory =  gson.fromJson(fileReader, type);
        fileReader.close();
        return memory;
    }
}
