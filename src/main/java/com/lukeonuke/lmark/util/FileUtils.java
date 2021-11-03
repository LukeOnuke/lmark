package com.lukeonuke.lmark.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import javafx.application.Platform;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {
    private static FileUtils instance;
    private File file;
    private PropertyChangeSupport fileChangeSupport = new PropertyChangeSupport(this);
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);


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
        setFile(new File(path).getAbsoluteFile());

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

            while ((line = reader.readLine()) != null) {
                sb.append(line);
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
        File oldFile = this.file;
        this.file = file;
        addToRecents(file);
        fileChangeSupport.firePropertyChange("file", oldFile, file);
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
        T memory = gson.fromJson(fileReader, type);
        fileReader.close();
        return memory;
    }

    public File getParentFile() {
        return file.getParentFile();
    }

    public void registerFileListener(PropertyChangeListener propertyChangeListener) {
        fileChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    public static void addToRecents(File recentFile) {
        recentFile = recentFile.getAbsoluteFile();
        File recentFilesStorage = FileUtils.getRelativeFile(ApplicationConstants.RECENT_FILES_STORAGE);
        ArrayList<String> recent;
        try {
            recent = FileUtils.readJSON(recentFilesStorage.getPath(),
                    new TypeToken<ArrayList<String>>() {
                    }.getType());

            if (recent == null) {
                recent = new ArrayList<>();
            }

            if (recent.contains(recentFile.getAbsolutePath())) {
                recent.remove(recentFile.getAbsolutePath());
                recent.add(0, recentFile.getAbsolutePath());
            }

            if (!recent.contains(recentFile.getAbsolutePath())) {
                recent.add(0, recentFile.getAbsolutePath());
            }

            if (recent.size() >= 10) {
                recent.remove(10);
            }

            FileUtils.writeJSON(recent, recentFilesStorage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static File getRelativeFile(String path) {
        return new File(getRelativeFile().getPath() + File.separator + path);
    }

    public static File getRelativeFile() {
        ArrayList<String> path = new ArrayList<>(Arrays.asList(LMark.class.getResource(ApplicationConstants.ICON).getPath().replace(File.separator, "/").split("[/]")));
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);
        StringBuilder sb = new StringBuilder();
        path.forEach(s -> {
            if(path.indexOf(s) != 0){
                sb.append(File.separator);
            }
            sb.append(s);
        });
        if(!sb.toString().endsWith(File.separator)){
            sb.append(File.separator);
        }
        logger.debug(new File(stripProtocol(sb.toString())).getPath());
        try {
            return new File(stripProtocol(sb.toString()));
        } catch (NullPointerException e) {
            logger.error("Error while getting relative file", e.getCause());
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return new File(".");
        }
    }

    public static String stripProtocol(String url){
        String[] urlArr = url.split(":");

        //It already has no protocol
        if(urlArr.length == 2){
            return url;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < urlArr.length; i++) {
            sb.append(urlArr[i]);
            if(i != urlArr.length - 1){
                sb.append(":");
            }
        }
        url = sb.toString();
        while(url.startsWith(File.separator)){
            url = url.substring(1);
        }
        return url;
    }
}
