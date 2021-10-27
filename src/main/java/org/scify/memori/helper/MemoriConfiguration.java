package org.scify.memori.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by pisaris on 21/12/2016.
 */
public class MemoriConfiguration {

    private static MemoriConfiguration instance = null;
    private final Properties props;

    private MemoriConfiguration() {
        props = new Properties();
    }

    public static MemoriConfiguration getInstance() {
        if (instance == null)
            instance = new MemoriConfiguration();

        return instance;
    }

    /**
     * Get a variable from project.properties file (given an input stream)
     *
     * @param propertyName the name of the property
     * @return the value of the given property
     */
    public String getPropertyByName(InputStream propertyFileStream, String propertyName) {
        if (props.containsKey(propertyName))
            return props.getProperty(propertyName);
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
     *
     * @param propertyKey the property key
     * @return the property value
     */
    public String getProjectProperty(String propertyKey) {
        return this.getDataPackProperty(propertyKey, "/project_additional.properties");
    }

    public String getDataPackProperty(String propertyKey, String propertyFileName) {
        //When loading a resource, the "/" means root of the main/resources directory
        InputStream inputStream = getClass().getResourceAsStream(propertyFileName);
        //if project_additional.properties file is not found, we load the default one
        if (inputStream == null) {
            inputStream = getClass().getResourceAsStream("/project.properties");
        }
        String propertyValue = this.getPropertyByName(inputStream, propertyKey);
        if (propertyValue == null) {
            inputStream = getClass().getResourceAsStream("/project.properties");
            propertyValue = this.getPropertyByName(inputStream, propertyKey);
        }
        return propertyValue;
    }

    public void setProperty(String key, String value) {
        this.props.setProperty(key, value);
    }
}
