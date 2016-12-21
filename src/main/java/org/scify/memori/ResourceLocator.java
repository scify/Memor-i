package org.scify.memori;

import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.helper.MemoriConfiguration;
import java.net.URL;

/**
 * Responsible for locating and retrieving project resource files
 */
public class ResourceLocator {

    private String rootDataPath;
    private String rootDataPathDefault;

    public ResourceLocator() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        this.rootDataPath = "/" + configuration.getProjectProperty("DATA_PACKAGE") + "/";
        this.rootDataPathDefault = "/" + configuration.getProjectProperty("DATA_PACKAGE_DEFAULT") + "/";
    }

    /**
     * Given a path that represents a resource, tries to find if the resource is available in the current data pack
     * @param filePath the path of the desired file
     * @param fileName the name of the desired file
     * @return the resource path in which the file is available (current data pack or default data pack)
     */
    public String getCorrectPathForFile(String filePath, String fileName) {
        String file = this.rootDataPath + filePath + fileName;
        URL fileURL = FXAudioEngine.class.getResource(file);
        if(fileURL == null) {
            System.err.println("Loading: " + file);
            file = this.rootDataPathDefault + filePath + fileName;
        }
        System.err.println("Eventually loaded: " + file);
        return file;
    }
}
