package com.lukeonuke.lmark.util;

import javafx.application.Platform;

/**
 * Utility class for javaFX management.
 * @author lukak
 * @since 1.0.0
 * */
public class FxUtils {
    /**
     * Will lazy run stuff that needs to be run on the jfx main thread. Lazy run means that it will add it to the
     * <code>Platform.runLater()<code/> queue only if its not on the jfx main thread.
     * */
    public static void lazyRunOnPlatform(Runnable runnable){
        if(Platform.isFxApplicationThread()){
            runnable.run();
        }else{
            Platform.runLater(runnable);
        }
    }
}
