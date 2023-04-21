
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
import org.scify.memori.helper.*;
import org.scify.memori.helper.analytics.AnalyticsManager;
import org.scify.memori.interfaces.AudioEngine;
import org.scify.memori.interfaces.Player;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class MainScreen extends Application {
    private final double mWidth = Screen.getPrimary().getBounds().getWidth();
    private final double mHeight = Screen.getPrimary().getBounds().getHeight();
    private final Rectangle graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getMaximumWindowBounds();
    public static Scene scene;
    private Stage primaryStage;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private final MemoriConfiguration configuration;
    private static final AudioEngine audioEngine = FXAudioEngine.getInstance();

    public MainScreen() {
        MemoriLogger.initLogger();
        configuration = MemoriConfiguration.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler.getInstance());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.setUpStageAndUIElements(primaryStage);
        AnalyticsManager.getInstance().logEvent("app_started");
    }

    private void setUpStageAndUIElements(Stage primaryStage) throws Exception {
        try {
            MemoriConfiguration configuration = MemoriConfiguration.getInstance();

            MemoriLogger.LOGGER.log(Level.INFO, "Java version: " + System.getProperty("java.version"));
            Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
            MemoriLogger.LOGGER.log(Level.INFO, "Locale: " + locale.getLanguage());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/first_screen.fxml"),
                    ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));

            Parent root = loader.load();
            if (System.getProperty("ttsUrl") != null)
                configuration.setProperty("TTS_URL", System.getProperty("ttsUrl"));
            if (System.getProperty("authToken") != null)
                configuration.setProperty("AUTH_TOKEN", System.getProperty("authToken"));
            if (Arrays.asList(new String[] { "keyboard", "mouse_touch" }).contains(System.getProperty("inputMethod")))
                configuration.setProperty("INPUT_METHOD", System.getProperty("inputMethod"));
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

            try {
                configuration.setLang(System.getProperty("lang"));
                if (Arrays.asList(new String[] { "keyboard", "mouse_touch" }).contains(System.getProperty("inputMethod")))
                    new MainMenuScreen(sceneHandler, sceneHandler.getMainWindow());
                else
                    new InputMethodSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
            } catch (Exception e) {
                new LanguageSelectionScreen(sceneHandler, primaryStage);
            }
        } catch (Exception e) {
            MemoriLogger.LOGGER.log(Level.INFO, "Exception: " + e.getMessage());
            throw e;
        }
    }

    private void setStageFavicon(Stage primaryStage) {
        ResourceLocator resourceLocator = ResourceLocator.getInstance();
        String gameCoverImgPath = resourceLocator
                .getCorrectPathForFile(configuration.getDataPackProperty("IMAGES_BASE_PATH")
                        + configuration.getDataPackProperty("GAME_COVER_IMG_PATH"), "game_cover.png");
        // set the "favicon"
        javafx.scene.image.Image faviconImage = new Image(gameCoverImgPath);
        primaryStage.getIcons().add(faviconImage);
    }

    private void addCloseHandlerOnStage() {
        primaryStage.setOnCloseRequest(t -> exitApplication());
    }

    public static void exitApplication() {
        System.out.println("App is exiting...");
        audioEngine.pauseCurrentlyPlayingAudios();
        // if player has logged in, perform call to set them as non-active
        Player player = PlayerManager.getLocalPlayer();
        PlayerManager playerManager = new PlayerManager();
        if (player != null) {
            playerManager.setPlayerAsNotInGame();
        }
        Platform.exit();
        System.exit(0);
        Platform.setImplicitExit(true);
    }
}
