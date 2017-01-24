package org.scify.memori;

import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.PropertyHandlerImpl;
import org.scify.memori.helper.StringUtils;
import org.scify.memori.interfaces.PropertyHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        System.err.println("DATA_PACK: " + configuration.getProjectProperty("DATA_PACKAGE"));
    }



    /**
     * Given a path that represents a resource, tries to find if the resource is available in the current data pack
     * If not available, loads the corresponding file from the default pack
     *
     * @param filePath the path of the desired file
     * @param fileName the name of the desired file
     * @return the resource path in which the file is available (current data pack or default data pack)
     */
    public String getCorrectPathForFile(String filePath, String fileName) {
        //check if there is a representation of the file in the additional data pack
        String additionalPackFileName = getFileNameEquivalentFromResourcePack(filePath + fileName);
        if(additionalPackFileName != null)
            fileName = additionalPackFileName;
        System.out.println(fileName);
        String file = this.rootDataPath + filePath + fileName;
        URL fileURL = FXAudioEngine.class.getResource(file);
        if (fileURL == null) {
            file = this.rootDataPathDefault + filePath + fileName;
//            System.out.println("File " + this.rootDataPath + filePath + fileName + " not found. Loaded default: " + file);
        }
        return file;
    }

    /**
     * Searches for the file name in the additional resource pack that corresponds to the file name of the game
     * @param file the file name
     * @return the name of the file if found, otherwise null
     */
    private String getFileNameEquivalentFromResourcePack(String file) {
        //extract the path name (without the file) from the path given
//        String pathNoFile = StringUtils.substringBeforeLast(file, "/");
        //search in the map file for the equivalent file name
        String dataPackFileNameNoPath = configuration.getDataPackProperty(file, this.rootDataPath + "resources_map.properties");
        //if an equivalent file was added, return it.
        if(dataPackFileNameNoPath != null)
            return dataPackFileNameNoPath;
        return null;
    }

    /**
     * Gets the content files and directories names of a resource path
     *
     * @param dirPath the desired path name
     * @return a set of names of the paths
     */
    public List<String> getResourcesFromDirectory(String dirPath) {
        try {
            List<String> filenames = new ArrayList<>();
            try (
                    InputStream in = getClass().getResourceAsStream(dirPath);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    String resource;

                while ((resource = br.readLine()) != null) {
                    filenames.add(resource);
                }
            }
            return filenames;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
