package com.lukeonuke.mdedit.gui.util;

import java.awt.*;
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
     * Opens webpage in os default browser (if supported).
     * @param uri Path to webpage.
     * @return Boolean indicating ,did it open the browser?
     */
    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
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
}
