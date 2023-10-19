package org.scify.memori.helper;

import org.scify.memori.fx.FXAudioEngine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by pisaris on 21/12/2016.
 */
public class MemoriConfiguration {

    private static MemoriConfiguration instance = null;
    private final Properties props;
    private final String[] acceptedLanguageCodes = {"en", "el", "es", "it"};

    private MemoriConfiguration() throws IOException {
        props = new Properties();
        // When loading a resource, the "/" means root of the main/resources directory
        String additionalPropertiesFilePath = "/project_additional.properties";
        String defaultPropertiesFilePath = "/project.properties";
        InputStream additionalPropertiesFileInputStream = getClass().getResourceAsStream(additionalPropertiesFilePath);
        InputStream defaultPropertiesFileInputStream = getClass().getResourceAsStream(defaultPropertiesFilePath);
        Properties propAdditional = new Properties();
        props.load(defaultPropertiesFileInputStream);
        // if a project_additional.properties file is found, we also load the additional properties
        if (additionalPropertiesFileInputStream != null)
            propAdditional.load(additionalPropertiesFileInputStream);
        props.putAll(propAdditional);
    }

    public static MemoriConfiguration getInstance() {
        if (instance == null) {
            try {
                instance = new MemoriConfiguration();
            } catch (IOException e) {
                DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            }
        }
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
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }
        return null;
    }

    public String getPropertyByName(String propertyName) {
        return props.getProperty(propertyName);
    }

    /**
     * Given a property key, gets a value from resources/project.properties file
     *
     * @param propertyKey the property key
     * @return the property value
     */
    public String getDataPackProperty(String propertyKey) {
        return getPropertyByName(propertyKey);
    }

    /**
     * Changes the base project.properties file, setting the property with a new value.
     *
     * @param propertyKey      the property key
     * @param newPropertyValue the new property value
     */
    public void setDataPackProperty(String propertyKey, String newPropertyValue) {
        props.setProperty(propertyKey, newPropertyValue);
    }

    public void setProperty(String key, String value) {
        if (value != null && !value.equals(""))
            this.props.setProperty(key, value);
    }

    public boolean ttsEnabled() {
        return getPropertyByName("TTS_URL") != null;
    }

    public boolean authModeEnabled() {
        return this.props.containsKey("AUTH_TOKEN") && this.props.getProperty("AUTH_TOKEN") != null;
    }

    public boolean vsPlayerEnabled() {
        return getDataPackProperty("VS_PLAYER_ENABLED").equalsIgnoreCase("true");
    }

    public void setLang(String langCode) throws Exception {
        if (!Arrays.asList(acceptedLanguageCodes).contains(langCode))
            throw new Exception("Language incorrect! Code: " + langCode);
        setDataPackProperty("APP_LANG", langCode);
        if (langCode.equals("en") || langCode.equals("el") || langCode.equals("es"))
            updateDataPackageForLang(langCode);
        else
            updateDataPackageForLang("en");
    }

    protected void updateDataPackageForLang(String langCode) {
        String currentDataPack = getDataPackProperty("DATA_PACKAGE");
        String currentDataPackNoLang = StringUtils.substringBeforeLast(currentDataPack, "_");
        String currentDataPackNew = currentDataPackNoLang + "_" + langCode;
        URL url = MemoriConfiguration.class.getResource("/" + currentDataPackNew);
        if (url != null)
            setDataPackProperty("DATA_PACKAGE", currentDataPackNew);

        String currentDefaultDataPack = getDataPackProperty("DATA_PACKAGE_DEFAULT");
        String currentDefaultDataPackNoLang = StringUtils.substringBeforeLast(currentDefaultDataPack, "_");
        setDataPackProperty("DATA_PACKAGE_DEFAULT", currentDefaultDataPackNoLang + "_" + langCode);
        ResourceLocator.getInstance().loadRootDataPaths();
    }

    public static boolean inputMethodIsKeyboard() {
        return !MemoriConfiguration.getInstance().getDataPackProperty("INPUT_METHOD").equals("mouse_touch");
    }
}
