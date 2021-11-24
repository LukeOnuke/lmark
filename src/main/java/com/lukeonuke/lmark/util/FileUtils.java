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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * File utils, one of the core systems of lmark
 *
 * @author lukak
 * @since 1.0.0
 */
public class FileUtils {
    private static FileUtils instance;
    /**
     * The current f i l e
     */
    private File file;
    /**
     * Bean for change event
     */
    private PropertyChangeSupport fileChangeSupport = new PropertyChangeSupport(this);
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);


    /**
     * Get instance with path, instantiate fist time
     */
    public static FileUtils getInstance(String path) throws FileNotFoundException {
        if (instance == null) {
            instance = new FileUtils(path);
        }
        return instance;
    }

    /**
     * The singleton get instance
     */
    public static FileUtils getInstance() {
        return instance;
    }

    /**
     * Constructor
     */
    private FileUtils(String path) throws FileNotFoundException {
        setFile(new File(path).getAbsoluteFile());

        if (!file.exists()) {
            throw new FileNotFoundException("File not found");
        }
    }

    /**
     * Get internal resource as string
     */
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

    /**
     * Get the current working file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the current working file.
     *
     * @param file The new working file.
     */
    public void setFile(File file) {
        File oldFile = this.file;
        this.file = file;
        addToRecents(file);
        fileChangeSupport.firePropertyChange("file", oldFile, file);
    }

    /**
     * Return contents of the working file as string
     *
     * @return String representation of the current working file
     */
    public String readFile() throws IOException {
        return readSpecifiedFile(file);
    }

    /**
     * Return contents of the specified file as string.
     *
     * @param specifiedFile Specified file to read.
     * @return String representation of the specified file
     */
    public static String readSpecifiedFile(File specifiedFile) throws IOException {
        return Files.readString(specifiedFile.toPath(), Charset.forName(detectCharset(specifiedFile)));
    }

    /**
     * Save string to file
     *
     * @param file   The file into witch the string will be written.
     * @param string The string to be written
     */
    public void saveFile(File file, String string) {
        //POSIX compliant text files
        boolean endsWithNewLine = string.endsWith("\n");
        if (!endsWithNewLine) {
            string = string + "\n";
        }

        try {
            Files.write(file.toPath(), string.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Detect charset of given file.
     *
     * @return String representation of charset, for example <code>UTF-8<code/>
     */
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

    /**
     * Serialise object to file, in JSON format.
     *
     * @param file   The file to be written it.
     * @param source The object to be written into the file.
     */
    public static void writeJSON(Object source, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        Gson gson = new Gson();
        gson.toJson(source, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * Read serialised object from file.
     *
     * @param file Path to file.
     * @param type Type of Object.
     * @return Deserialized object from file.
     */
    public static <T> T readJSON(String file, Type type) throws IOException {
        FileReader fileReader = new FileReader(file);
        Gson gson = new Gson();
        T memory = gson.fromJson(fileReader, type);
        fileReader.close();
        return memory;
    }

    /**
     * Get parent file of the current working file.
     *
     * @return Parent of working file <code>file.getParentFile()<code/>.
     */
    public File getParentFile() {
        return file.getParentFile();
    }

    /**
     * Register file change listener.
     *
     * @param propertyChangeListener Change listener.
     */
    public void registerFileListener(PropertyChangeListener propertyChangeListener) {
        if (!Arrays.asList(fileChangeSupport.getPropertyChangeListeners()).contains(propertyChangeListener))
            fileChangeSupport.addPropertyChangeListener("file", propertyChangeListener);
    }

    /**
     * Add file to the recent list.
     *
     * @param recentFile The file to be added to the recent list.
     */
    public static void addToRecents(File recentFile) {
        recentFile = recentFile.getAbsoluteFile();
        File recentFilesStorage = FileUtils.getRelativeFile(ApplicationConstants.RECENT_FILES_STORAGE);
        ArrayList<String> recent;
        try {
            recent = FileUtils.getRecentFiles();

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

    public static ArrayList<String> getRecentFiles() throws IOException {
        ArrayList<String> recentFilesList = new ArrayList<>();
        File recentFilesStorage = FileUtils.getRelativeFile(ApplicationConstants.RECENT_FILES_STORAGE);

        if (!recentFilesStorage.exists()) {

            recentFilesStorage.createNewFile();
            FileUtils.writeJSON(recentFilesList, recentFilesStorage);

        }
        recentFilesList = FileUtils.readJSON(recentFilesStorage.getPath(),
                new TypeToken<ArrayList<String>>() {
                }.getType());

        if (recentFilesList == null) {
            recentFilesList = new ArrayList<>();
        }


        File recent;
        String s;
        for (int i = 0; i < recentFilesList.size(); i++) {
            s = recentFilesList.get(i);
            recent = new File(s);
            if (!recent.exists()) {
                recentFilesList.remove(s);
            }
        }

        return recentFilesList;
    }

    /**
     * Get a file relative to the virtual working directory.
     *
     * @param path Relative path to file.
     * @return Absolute file pointing to the selected child of relative working directory.
     */
    public static File getRelativeFile(String path) {
        return new File(getRelativeFile().getPath() + File.separator + path);
    }

    /**
     * Get the virtual working directory.
     *
     * @return File object pointing to the virtual working dirrectory.
     */
    public static File getRelativeFile() {
        ArrayList<String> path = new ArrayList<>(
                Arrays.asList(
                        LMark.class.getResource(ApplicationConstants.ICON).getPath()
                                .replace(File.separator, "/")
                                .split("[/]")
                )
        );
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);

        StringBuilder sb = new StringBuilder();
        path.forEach(s -> {
            if (path.indexOf(s) != 0) {
                sb.append(File.separator);
            }
            sb.append(s);
        });
        if (!sb.toString().endsWith(File.separator)) {
            sb.append(File.separator);
        }
        logger.debug(new File(stripProtocol(sb.toString())).getPath());
        try {
            return new File(stripProtocol(URLDecoder.decode(sb.toString(), Charset.defaultCharset())));
        } catch (NullPointerException e) {
            logger.error("Error while getting relative file", e.getCause());
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return new File(".");
        }
    }

    /**
     * Strip protocol from url.
     *
     * Every JDK derived from <strong>Oracle JDK</strong> loves adding protocol prefixes to everything.
     * For example files get <code>file:c:\path\to\file</code>.
     *
     * @param url The url to be stripped.
     * @return Only the path of the url with no protocol identifier.
     */
    public static String stripProtocol(String url) {

        try {
            URL parsedUrl = new URL(url);
            url = parsedUrl.getPath();
        } catch (MalformedURLException e) {
            // Swallow
        }

        /*String[] urlArr = url.split(":");

        StringBuilder sb = new StringBuilder();
        if(urlArr.length == 2 ) {
            sb.append(urlArr[0]);
            sb.append(":");
        }

        for (int i = 1; i < urlArr.length; i++) {
            sb.append(urlArr[i]);
            if (i != urlArr.length - 1) {
                sb.append(":");
            }
        }
        url = sb.toString();
        while (url.startsWith(File.separator)) {
            url = url.substring(1);
        }*/
        return url;
    }
}
