package com.lukeonuke.lmark.gui.util;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Utilities for OS integration, like opening browser.
 *
 * @since 0.0.1
 * @author LukeOnuke
 */
public class OSIntegration {
    /**
     * Gets the desktop instance.
     * @return Desktop instance if desktop is supported on the system, otherwise it returns null
     * */
    public static Desktop getDesktopIfSupported(){
        if(Desktop.isDesktopSupported()){
            return Desktop.getDesktop();
        }
        return null;
    }

    /**
     * Opens webpage in os default browser (if supported).
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
     * @param file Path represented with a file instance
     * */
    public static void openPathInExplorer(File file){
        try {
            getDesktopIfSupported().open(file);
        } catch (IOException e) {

        }
    }

    /**
     * Open explorer to file path.
     * @param file Path represented with a string
     * */
    public static void openPathInExplorer(String file){
        openPathInExplorer(new File(file));
    }

    /**
     * Make a system specific notify (beep) sound. <i>Beep boop.<i/>
     * */
    public static void beep(){
        Toolkit.getDefaultToolkit().beep();
    }
}
