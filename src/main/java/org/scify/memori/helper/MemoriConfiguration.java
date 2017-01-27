package org.scify.memori.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Created by pisaris on 21/12/2016.
 */
public class MemoriConfiguration {

    /**
     * Get a variable from project.properties file (given an input stream)
     * @param propertyName the name of the property
     * @return the value of the given property
     */
    public String getPropertyByName(InputStream propertyFileStream, String propertyName) {
        Properties props = new Properties();
        try {
            props.load(propertyFileStream);
            return props.getProperty(String.valueOf(propertyName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Given a property key, gets a value from resources/project.properties file
     * @param propertyKey the property key
     * @return the property value
     */
    public String getProjectProperty(String propertyKey) {
        return this.getDataPackProperty(propertyKey, "/project_additional.properties");
    }

    public String getDataPackProperty(String propertyKey, String propertyFileName) {
//        System.out.println("getting property file: " + propertyFileName);
        //When loading a resource, the "/" means root of the main/resources directory
        InputStream inputStream = getClass().getResourceAsStream(propertyFileName);
        //if project_additional.properties file is not found, we load the default one
        if(inputStream == null) {
            inputStream = getClass().getResourceAsStream("/project.properties");
            //MemoriLogger.LOGGER.log(Level.SEVERE, "Property file: " + propertyFileName + " not found");
        }
        String propertyValue = this.getPropertyByName(inputStream, propertyKey);
        if(propertyValue == null) {
            inputStream = getClass().getResourceAsStream("/project.properties");
            propertyValue = this.getPropertyByName(inputStream, propertyKey);
        }
        return propertyValue;
    }

    /**
     * Gets the default user directory for the current architecture
     */
    public String getUserDir() {
        String userDir;
        if ((System.getProperty("os.name")).toUpperCase().contains("WINDOWS")) {
            userDir = System.getenv("AppData");
        } else {
            userDir = System.getProperty("user.dir");
        }
        return userDir + File.separator;
    }
}
