package com.lukeonuke.lmark;

import com.lukeonuke.lmark.util.FileUtils;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.Properties;

public class Registry {
    Properties prop = new Properties();
    String fileName = FileUtils.getRelativeFile("app.properties").getPath();
    PropertyChangeSupport registryChangeSupport = new PropertyChangeSupport(this);
    private static Registry instance = null;

    private Registry() {
        File file = new File(fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
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

    public void write(String key, String value) {
        registryChangeSupport.firePropertyChange(key, prop.getProperty(key), value);
        prop.setProperty(key, value);
    }

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

    public void registerRegistryChangeEvent(String propertyName, PropertyChangeListener propertyChangeEvent){
        registryChangeSupport.addPropertyChangeListener(propertyName, propertyChangeEvent);
    }
}
