
/**
 * Copyright 2016 SciFY.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scify.memori.screens;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.scify.memori.MainOptions;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.helper.UTF8Control;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;


public class MainScreen extends Application {
    private double mWidth = Screen.getPrimary().getBounds().getWidth();
    private double mHeight = Screen.getPrimary().getBounds().getHeight();
    private Rectangle graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();


    public MainScreen() {
        MemoriLogger.initLogger();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MemoriConfiguration configuration = new MemoriConfiguration();
        //TODO (4): Here we add an additional directory called "additional_pack", which will contain the extra data pack, called "test_pack".
        //To test it, create an "additional_pack" directory with different files
        System.out.println("adding path:" + configuration.getUserDir() + "additional_pack");
        addPath(configuration.getUserDir() + "additional_pack");

        Locale locale = new Locale(configuration.getProjectProperty("APP_LANG"));
        //Load fxml file (layout xml) for first screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/first_screen.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = loader.load();
        MainScreenController controller = loader.getController();

        // set as width and height the screen width and height
        MainOptions.mWidth = graphicsEnvironment.getWidth() - 10;
        MainOptions.mHeight = graphicsEnvironment.getHeight() - 10;
        // construct the scene (the content of the stage)
        Scene primaryScene = new Scene(root, mWidth, mHeight);
        primaryStage.setTitle("Memor-i");
        controller.setParameters(primaryStage, primaryScene);
        System.err.println(configuration.getUserDir() + "data_packs");
    }

    /**
     * Adds a path to the classpath on runtime
     * http://stackoverflow.com/questions/7884393/can-a-directory-be-added-to-the-class-path-at-runtime
     *
     * TODO(5): This method is irrelevant with the MainScreen and should be added elsewhere.
     * Should we implement a helper class? Or just use one of the existing classes?
     *
     * @param path the path to be added
     * @throws Exception if the path is not found or cannot be read
     */
    public static void addPath(String path) throws Exception {
        File pathToFile = new File(path);
        if(!pathToFile.exists()) {
            MemoriLogger.LOGGER.log(Level.SEVERE, "Path: " + path + " not found");
            return;
        }
        URL pathUrl = new File(path).toURI().toURL();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class urlClass = URLClassLoader.class;
        Method method = urlClass.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(urlClassLoader, pathUrl);
    }
}
