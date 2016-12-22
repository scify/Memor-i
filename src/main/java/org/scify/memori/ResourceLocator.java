package org.scify.memori;

import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.helper.MemoriConfiguration;

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


    /**
     * Gets the content files and directories names of a resource path
     * @param dirPath the desired path name
     * @return a set of names of the paths
     */
    public List<String> getResourcesFromDirectory(String dirPath) {
        try {
            return this.getResourceFiles(dirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the content files and directories names of a resource path
     * @param path the desired path name
     * @return a set of names of the paths
     */
    private List<String> getResourceFiles( String path ) throws IOException {
        List<String> filenames = new ArrayList<>();

        try(
                InputStream in = getClass().getResourceAsStream( path );
                BufferedReader br = new BufferedReader( new InputStreamReader( in ) ) ) {
            String resource;

            while( (resource = br.readLine()) != null ) {
                filenames.add( resource );
            }
        }

        return filenames;
    }
}
