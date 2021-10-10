package com.lukeonuke.lmark;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Registry {
    Properties prop = new Properties();
    String fileName = "app.properties";

    public Registry() {
        if(!new File(fileName).exists()){
            try {
                new File(fileName).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reset();
            save();
        }

        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void reset(){
        prop.put(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED, "true");
    }

    public String readOption(String key){
        return prop.getProperty(key);
    }

    public boolean readOptionAsBoolean(String key){
        return Boolean.parseBoolean(readOption(key));
    }

    public void save(){
        reset();
        try(FileOutputStream fos = new FileOutputStream(fileName)){
            prop.store(fos, "lmark configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
