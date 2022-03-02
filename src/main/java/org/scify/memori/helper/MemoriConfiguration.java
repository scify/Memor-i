package org.scify.memori.helper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by pisaris on 21/12/2016.
 */
public class MemoriConfiguration {

    private static MemoriConfiguration instance = null;
    private final Properties props;
    private final String[] acceptedLanguageCodes = {"en", "el", "es", "it"};
    private final String additionalPropertiesFilePath = File.separator + "project_additional.properties";
    private final File additionalPropertiesFile;

    private MemoriConfiguration() throws IOException {
        props = new Properties();
        //When loading a resource, the "/" means root of the main/resources directory
        InputStream additionalPropertiesFileInputStream = getClass().getResourceAsStream(additionalPropertiesFilePath);
        InputStream defaultPropertiesFileInputStream = getClass().getResourceAsStream(File.separator + "project.properties");
        Properties propAdditional = new Properties();
        props.load(defaultPropertiesFileInputStream);
        //if project_additional.properties file is not found, we load the default one
        if (additionalPropertiesFileInputStream != null)
            propAdditional.load(additionalPropertiesFileInputStream);
        props.putAll(propAdditional);
        additionalPropertiesFile = getOrCreateAdditionalPropertiesFile();
    }

    public static MemoriConfiguration getInstance() {
        if (instance == null) {
            try {
                instance = new MemoriConfiguration();
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
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
        try {
            FileOutputStream out = new FileOutputStream(additionalPropertiesFile);
            props.setProperty(propertyKey, newPropertyValue);
            props.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected File getOrCreateAdditionalPropertiesFile() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(additionalPropertiesFilePath);
            if (inputStream == null || inputStream.available() < 1) {
                return createAdditionalPropertiesFile();
            } else {
                File file = new File(Objects.requireNonNull(getClass().getResource(additionalPropertiesFilePath)).getFile());
                FileUtils.copyInputStreamToFile(inputStream, file);
                return file;
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected File createAdditionalPropertiesFile() throws URISyntaxException {
        URL url = this.getClass().getResource(File.separator);
        File parentDirectory = new File(new URI(Objects.requireNonNull(url).toString()));
        return new File(parentDirectory, additionalPropertiesFilePath.substring(1));
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
        if (langCode.equals("en") || langCode.equals("el"))
            updateDefaultDataPackageForLang(langCode);
        else
            updateDefaultDataPackageForLang("en");
    }

    protected void updateDefaultDataPackageForLang(String langCode) {
        String currentDefaultDataPack = getDataPackProperty("DATA_PACKAGE_DEFAULT");
        String currentDefaultDataPackNoLang = StringUtils.substringBeforeLast(currentDefaultDataPack, "_");
        setDataPackProperty("DATA_PACKAGE_DEFAULT", currentDefaultDataPackNoLang + "_" + langCode);
        ResourceLocator.getInstance().loadRootDataPaths();
    }
}
