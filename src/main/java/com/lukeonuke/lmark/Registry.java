package com.lukeonuke.lmark;

import com.lukeonuke.lmark.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

public class Registry {
    Properties prop = new Properties();
    String fileName = FileUtils.getRelativeFile("app.properties").getPath();
    PropertyChangeSupport registryChangeSupport = new PropertyChangeSupport(this);
    Logger logger = LoggerFactory.getLogger(Registry.class);
    private static Registry instance = null;

    private Registry() {
        logger.info("Loading properties from {}", fileName);
        File file = new File(fileName);
        if(file.exists()){
            refresh();
        } else {
            try {
                file.createNewFile();
                reset();
                save();
            } catch (IOException e) {
                logger.error("Error whilst initilising Registry {}", e.getMessage());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.save();
            logger.info("Saved registry, shutdown hook terminated.");
        }, "registry-shutdown-hook"));

        logger.info(this.toString());
    }

    public static Registry getInstance(){
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    public void refresh(){
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try (FileInputStream fis = new FileInputStream(fileName)) {
                prop.load(fis);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        });
    }

    public void reset(){
        prop.setProperty(ApplicationConstants.PROPERTIES_AUTOSAVE_ENABLED, "true");
        prop.setProperty(ApplicationConstants.PROPERTIES_AUTO_DARK_MODE, "true");
        prop.setProperty(ApplicationConstants.PROPERTIES_DARK_MODE_ENABLED, "false");
        prop.setProperty(ApplicationConstants.PROPERTIES_VIEW_FILETREEVIEW, "true");
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

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(Registry.class.getName());
        sb.append(" ");
        sb.append(registryChangeSupport);
        sb.append(" Properties are");
        prop.forEach((o, o2) -> {
            sb.append(" , ");
            sb.append(o);
            sb.append(" : ");
            sb.append(o2);
        });
        sb.append(".");
        return sb.toString();
    }
}
