
/**
 * Copyright 2016 SciFY.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scify.memori.screens;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.scify.memori.MainOptions;
import org.scify.memori.PlayerManager;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.helper.UTF8Control;
import org.scify.memori.interfaces.AudioEngine;
import org.scify.memori.interfaces.Player;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;


public class MainScreen extends Application {
    private final double mWidth = Screen.getPrimary().getBounds().getWidth();
    private final double mHeight = Screen.getPrimary().getBounds().getHeight();
    private final Rectangle graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    public static Scene scene;
    private Stage primaryStage;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private final MemoriConfiguration configuration;
    private static final AudioEngine audioEngine = FXAudioEngine.getInstance();
    ;

    public MainScreen() {
        MemoriLogger.initLogger();
        configuration = MemoriConfiguration.getInstance();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();

        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
        //Load fxml file (layout xml) for first screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/first_screen.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = loader.load();
        configuration.setProperty("TTS_URL", System.getProperty("ttsUrl"));
        // set as width and height the screen width and height
        MainOptions.mWidth = graphicsEnvironment.getWidth();
        MainOptions.mHeight = graphicsEnvironment.getHeight();
        // construct the scene (the content of the stage)
        Scene primaryScene = new Scene(root, mWidth, mHeight);
        primaryStage.setTitle("Memor-i");
        scene = primaryScene;
        this.primaryStage = primaryStage;
        addCloseHandlerOnStage();
        primaryScene.getStylesheets().add("css/style.css");

        primaryStage.requestFocus();
        primaryStage.sizeToScene();

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setFullScreen(true);
        primaryStage.setMaximized(true);
        FXRenderingEngine.setGamecoverIcon(primaryScene, "gameCoverImgContainer");

        setStageFavicon(primaryStage);
        sceneHandler.setMainWindow(primaryStage);
        sceneHandler.pushScene(primaryScene);

        primaryStage.show();
        new LanguageSelectionScreen(sceneHandler, primaryStage);
    }

    private void setStageFavicon(Stage primaryStage) {
        ResourceLocator resourceLocator = new ResourceLocator();
        String gameCoverImgPath = resourceLocator.getCorrectPathForFile(configuration.getDataPackProperty("IMAGES_BASE_PATH") + configuration.getDataPackProperty("GAME_COVER_IMG_PATH"), "game_cover.png");
        //set the "favicon"
        javafx.scene.image.Image faviconImage = new Image(gameCoverImgPath);
        primaryStage.getIcons().add(faviconImage);
    }

    private void addCloseHandlerOnStage() {
        primaryStage.setOnCloseRequest(t -> exitApplication());
    }

    public static void exitApplication() {
        System.out.println("Stage is closing");
        audioEngine.pauseCurrentlyPlayingAudios();
        // if player has logged in, perform call to set them as non-active
        Player player = PlayerManager.getLocalPlayer();
        PlayerManager playerManager = new PlayerManager();
        if (player != null) {
            System.err.println(player.getName());
            playerManager.setPlayerAsNotInGame();
            System.out.println("player set not in game. Closing...");
        } else {
            System.out.println("no local player. Closing...");
        }
        Platform.exit();
        System.exit(0);
    }
}
