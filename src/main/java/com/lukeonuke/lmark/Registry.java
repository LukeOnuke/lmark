package com.lukeonuke.lmark;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Registry {
    Properties prop = new Properties();
    String fileName = "app.properties";
    private static Registry instance = null;

    private Registry() {
        if(!new File(fileName).exists()){
            try {
                new File(fileName).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reset();
            save();
        }

        refresh();
    }

    public static Registry getInstance(){
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    public void refresh(){
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reset(){
        prop.setProperty(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED, "true");
        prop.setProperty(ApplicationConstants.PROPERTIES_AUTO_DARK_MODE, "true");
        prop.setProperty(ApplicationConstants.PROPERTIES_DARK_MODE_ENABLED, "false");
    }

    public String readOption(String key){
        return prop.getProperty(key);
    }

    public void write(String key, String value) {prop.setProperty(key, value);}
    public void write(String key, boolean value){write(key, Boolean.toString(value));}

    public boolean readOptionAsBoolean(String key){
        return Boolean.parseBoolean(readOption(key));
    }

    public void save(){
        try(FileOutputStream fos = new FileOutputStream(fileName)){
            prop.store(fos, "lmark configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
