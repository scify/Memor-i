package org.scify.memori.helper;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.helper.MemoriConfiguration;
import java.io.*;
import java.net.URL;


/**
 * Responsible for locating and retrieving project resource files
 */
public class ResourceLocator {

    private String rootDataPath;
    private String rootDataPathDefault;
    private MemoriConfiguration configuration;

    public ResourceLocator() {
        configuration = new MemoriConfiguration();
        this.rootDataPath = "/" + configuration.getProjectProperty("DATA_PACKAGE") + "/";
        this.rootDataPathDefault = "/" + configuration.getProjectProperty("DATA_PACKAGE_DEFAULT") + "/";
    }



    /**
     * Given a path that represents a resource, tries to find if the resource is available in the current data pack
     * If not available, loads the corresponding file from the default pack
     *
     * @param path the path of the desired file
     * @param fileName the name of the desired file
     * @return the resource path in which the file is available (current data pack or default data pack)
     */
    public String getCorrectPathForFile(String path, String fileName) {
        String filePath = path + fileName;
        //check if there is a representation of the file in the additional data pack
        String additionalPackFileName = getFileNameEquivalentFromResourcePack(path + fileName);
        if(additionalPackFileName != null)
            filePath = additionalPackFileName;

        String file = this.rootDataPath + filePath;
        //System.err.println("trying to get: " + file);
        URL fileURL = FXAudioEngine.class.getResource(file);
        if (fileURL == null) {
            file = this.rootDataPathDefault + filePath;
            //System.out.println("File " + this.rootDataPath + path + fileName + " not found. Loaded default: " + file);
        }
        return file;
    }

    /**
     * Searches for the file name in the additional resource pack that corresponds to the file name of the game
     * @param file the file name
     * @return the name of the file if found, otherwise null
     */
    private String getFileNameEquivalentFromResourcePack(String file) {
        //System.out.println("Trying to get mapped property: " + file);
        MemoriConfiguration memoriConfiguration = new MemoriConfiguration();
        //When loading a resource, the "/" means root of the main/resources directory
        InputStream inputStream = getClass().getResourceAsStream(this.rootDataPath + "resources_map.properties");
        //if project_additional.properties file is not found, we load the default one
        if(inputStream == null) {
            return null;
        } else {
            return memoriConfiguration.getPropertyByName(inputStream, file);
        }
    }
}
