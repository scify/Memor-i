
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

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.scify.memori.*;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.RequestManager;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.*;

public class MainScreenController implements Initializable {

    private MemoriConfiguration configuration;
    private String miscellaneousSoundsBasePath;
    private Stage primaryStage;
    private static Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();

    public MainScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void setParameters(Stage primaryStage, Scene primaryScene) {
        this.primaryScene = primaryScene;
        this.primaryStage = primaryStage;
        addCloseHandlerOnStage();
        primaryScene.getStylesheets().add("css/style.css");
        primaryStage.show();
        primaryStage.requestFocus();
        primaryStage.sizeToScene();
        primaryStage.setFullScreen(true);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        FXRenderingEngine.setGamecoverIcon(primaryScene, "gameCoverImgContainer");

        setStageFavicon(primaryStage);
        sceneHandler.setMainWindow(primaryStage);
        sceneHandler.pushScene(primaryScene);

        attachButtonFocusHandlers();
        primaryStage.show();

    }

    private void setStageFavicon(Stage primaryStage) {
        ResourceLocator resourceLocator = new ResourceLocator();
        String gameCoverImgPath = resourceLocator.getCorrectPathForFile(configuration.getProjectProperty("IMAGES_BASE_PATH") + configuration.getProjectProperty("GAME_COVER_IMG_PATH"),  "game_cover.png");
        //set the "favicon"
        Image faviconImage = new Image(gameCoverImgPath);
        primaryStage.getIcons().add(faviconImage);
    }

    public static void screenPoppedUI() {
        primaryScene.lookup("#welcome").requestFocus();
    }


    /**
     * Attaches focus handlers to fixed buttons (tutorial, exit, etc)
     */
    private void attachButtonFocusHandlers() {

        primaryScene.lookup("#welcome").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "welcome.mp3", false);
            }
        });

        primaryScene.lookup("#headphonesAdjustment").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "headphones_adjustment.mp3", false);
            }
        });

        primaryScene.lookup("#tutorial").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "tutorial.mp3", false);
            }
        });

        primaryScene.lookup("#single_player").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "single_player.mp3", false);
            }
        });

        primaryScene.lookup("#versus_computer").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_cpu.mp3", false);
            }
        });

        primaryScene.lookup("#versus_player").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_player.mp3", false);
            }
        });

        primaryScene.lookup("#myScores").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "my_scores.mp3", false);
            }
        });

        primaryScene.lookup("#exit").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "exit.mp3", false);
            }
        });

        primaryScene.lookup("#sponsors").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "sponsors.mp3", false);
            }
        });
    }

    /**
     * Quits game
     *
     * @param evt the keyboard event
     */
    @FXML
    protected void exitGame(KeyEvent evt) {
        if (evt.getCode() == SPACE || evt.getCode() == ESCAPE) {
            exitApplication();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializeTutorialGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            MemoriGameLauncher memoriGameLauncher = new MemoriGameLauncher(sceneHandler);
            Thread thread = new Thread(() -> memoriGameLauncher.startTutorialGame());
            thread.start();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializeSinglePlayerGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            audioEngine.pauseCurrentlyPlayingAudios();
            new LevelsScreen(sceneHandler, GameType.SINGLE_PLAYER);
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializePvCGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            audioEngine.pauseCurrentlyPlayingAudios();
            new LevelsScreen(sceneHandler, GameType.VS_CPU);
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializePvPGame(KeyEvent evt) {
        audioEngine.pauseCurrentlyPlayingAudios();
        if (evt.getCode() == SPACE) {
            if(RequestManager.networkAvailable()) {
                if(PlayerManager.getLocalPlayer() == null)
                    new RegisterOrLoginScreen(sceneHandler);
                else
                    new InvitePlayerScreen(sceneHandler);
            }
            else {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "no_network.mp3", false);
            }
        }
    }


    @FXML
    protected void myScores(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            audioEngine.pauseCurrentlyPlayingAudios();
            new FXHighScoresScreen(sceneHandler, sceneHandler.getMainWindow());
        }
    }

    @FXML
    protected void goToSponsorsPage(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            new SponsorsScreen(sceneHandler, sceneHandler.getMainWindow());
        }
    }


    @FXML
    protected void headphonesAdjustment(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            audioEngine.playBalancedSound(-1.0, this.miscellaneousSoundsBasePath + "left_headphone.mp3", true);
            audioEngine.playBalancedSound(1.0, this.miscellaneousSoundsBasePath + "right_headphone.mp3", true);
        }
    }

    private void addCloseHandlerOnStage() {
        primaryStage.setOnCloseRequest(t -> exitApplication());
    }

    private void exitApplication() {
        System.out.println("Stage is closing");
        audioEngine.pauseCurrentlyPlayingAudios();
        // if player has logged in, perform call to set them as non-active
        Player player = PlayerManager.getLocalPlayer();
        PlayerManager playerManager = new PlayerManager();
        if(player != null) {
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
