/*
 * MIT License
 *
 * Copyright (c) 2021 Luka Kresoja
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * */
package com.lukeonuke.lmark;


import com.lukeonuke.lmark.gui.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * LMARK
 * ======
 * Simple and effective markdown editor.
 */
public class LMark {
    public static void main(String[] args) {
        //Load all mandatory subsystems
        Registry.getInstance();
        registerToDesktop();
        //Finished base boot.

        LMarkApplication.launchApp(args);
    }

    private static void registerToDesktop(){
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            if(desktop.isSupported(Desktop.Action.APP_OPEN_FILE)){
                desktop.setOpenFileHandler(e -> {
                    try {
                        FileUtils.getInstance(e.getFiles().get(0).getPath());
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                        System.exit(-1);
                    }
                });
            }
        }
    }
}