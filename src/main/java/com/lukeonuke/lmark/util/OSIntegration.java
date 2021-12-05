package com.lukeonuke.lmark.util;

import com.lukeonuke.lmark.ApplicationConstants;
import com.lukeonuke.lmark.LMark;
import com.sun.javafx.util.Utils;
import javafx.scene.control.Alert;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Utilities for OS integration, like opening browser.
 *
 * @author LukeOnuke
 * @since 0.0.1
 */
public class OSIntegration {

    /**
     * Gets the desktop instance.
     *
     * @return Desktop instance if desktop is supported on the system, otherwise it returns null
     */
    public static Desktop getDesktopIfSupported() {
        if (Desktop.isDesktopSupported()) {
            return Desktop.getDesktop();
        }
        return null;
    }

    /**
     * Opens webpage in os default browser (if supported).
     *
     * @param uri Path to webpage.
     * @return Boolean indicating ,did it open the browser?
     */
    public static boolean openWebpage(URI uri) {
        Desktop desktop = getDesktopIfSupported();
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Opens webpage in os default browser (if supported).
     *
     * @param url Path to webpage.
     * @return Boolean indicating ,did it open the browser?
     */
    public static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Opens webpage in os default browser (if supported).
     *
     * @param path Path to webpage.
     * @return Boolean indicating ,did it open the browser?
     */
    public static boolean openWebpage(String path) {
        try {
            return openWebpage(new URI(path));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Open explorer to file path.
     *
     * @param file Path represented with a file instance
     */
    public static void openPathInExplorer(File file) {
        try {
            Desktop d = getDesktopIfSupported();

            if (d == null) return;
            if (!d.isSupported(Desktop.Action.OPEN)) return;

            d.open(file);
        } catch (IOException e) {
            FxUtils.lazyRunOnPlatform(() -> {
                FxUtils.createAlert(Alert.AlertType.ERROR, "System error", "Could not open URL in browser", e.getMessage(), null);
            });
        }
    }

    /**
     * Open explorer to file path.
     *
     * @param file Path represented with a string
     */
    public static void openPathInExplorer(String file) {
        openPathInExplorer(new File(file));
    }

    /**
     * Make a system specific notify (beep) sound. <i>Beep boop.<i/>
     */
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    private static OperatingSystem OS;
    static {
        if (Utils.isWindows()) OS = OperatingSystem.WINDOWS;
        if (Utils.isMac()) OS = OperatingSystem.MACOS;
        if (Utils.isUnix()) OS = OperatingSystem.UNIX;
    }

    /**
     * Get operating system.
     * @return Detected operating system.
     * @since 2.2.0
     * */
    public static OperatingSystem getOS(){
        return OS;
    }

    public static File getAppData(){
        if(OS.equals(OperatingSystem.MACOS)){
            return new File(File.separator + "Library" +
                    File.separator + "Application Support" +
                    File.separator + LMark.class.getName() + File.separator);
        }
        return new File(System.getProperty("user.home") +
                File.separator + ApplicationConstants.APPDIR + File.separator);
    }
}
