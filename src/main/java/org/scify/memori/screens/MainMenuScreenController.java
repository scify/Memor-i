
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

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.scify.memori.MemoriGameLauncher;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.interfaces.AudioEngine;
import org.scify.memori.network.RequestManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class MainMenuScreenController implements Initializable {

    public Button sponsors;
    public Button versus_player;
    public VBox btnContainer;
    public Button versus_computer;
    private final MemoriConfiguration configuration;
    private final String miscellaneousSoundsBasePath;
    private Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private final AudioEngine audioEngine = FXAudioEngine.getInstance();;

    public MainMenuScreenController() {
        configuration = MemoriConfiguration.getInstance();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MemoriLogger.LOGGER.log(Level.INFO, "Java version: " + System.getProperty("java.version"));

        // if the game has vs_player option enabled, show the button
        // else hide it
        if (configuration.getProjectProperty("VS_PLAYER_ENABLED").equalsIgnoreCase("true")) {
            versus_player.setVisible(true);
        } else {
            btnContainer.getChildren().remove(versus_player);
        }

        if (configuration.getProjectProperty("VS_CPU_ENABLED").equalsIgnoreCase("true")) {
            versus_computer.setVisible(true);
        } else {
            btnContainer.getChildren().remove(versus_computer);
        }
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene) {
        this.primaryScene = levelsScreenScene;
        this.sceneHandler = sceneHandler;
        sceneHandler.pushScene(levelsScreenScene);
        FXRenderingEngine.setGamecoverIcon(this.primaryScene, "gameCoverImgContainer");
        attachButtonFocusHandlers();
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

        primaryScene.lookup("#headphones_adjustment").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
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

        if (configuration.getProjectProperty("VS_CPU_ENABLED").equalsIgnoreCase("true")) {
            primaryScene.lookup("#versus_computer").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_cpu.mp3", false);
                }
            });
        }

        if (configuration.getProjectProperty("VS_PLAYER_ENABLED").equalsIgnoreCase("true")) {
            primaryScene.lookup("#versus_player").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_player.mp3", false);
                }
            });
        }

        primaryScene.lookup("#my_scores").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "my_scores.mp3", false);
            }
        });

        primaryScene.lookup("#exit").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "exit.mp3", false);
            }
        });

        if (!configuration.getProjectProperty("APP_LANG").equalsIgnoreCase("en")) {
            primaryScene.lookup("#sponsors").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "sponsors.mp3", false);
                }
            });
        }
    }

    /**
     * Quits game
     *
     * @param evt the keyboard event
     */
    @FXML
    protected void exitGame(Event evt) {
        MemoriLogger.LOGGER.log(Level.INFO, evt.toString());
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE || keyEvt.getCode() == ESCAPE) {
                MainScreen.exitApplication();
            }
        } else {
            MainScreen.exitApplication();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializeTutorialGameEventHandler(Event evt) {
        MemoriLogger.LOGGER.log(Level.INFO, evt.toString());
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializeTutorialGame();
            }
        } else {
            initializeTutorialGame();
        }
    }

    protected void initializeTutorialGame() {
        MemoriGameLauncher memoriGameLauncher = new MemoriGameLauncher(sceneHandler);
        Thread thread = new Thread(() -> memoriGameLauncher.startTutorialGame());
        thread.start();
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializeSinglePlayerGameEventHandler(Event evt) {
        MemoriLogger.LOGGER.log(Level.INFO, evt.toString());
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializeSinglePlayerGame();
            }
        } else {
            initializeSinglePlayerGame();
        }
    }

    protected void initializeSinglePlayerGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new LevelsScreen(sceneHandler, GameType.SINGLE_PLAYER);
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializePvCGameEventHandler(Event evt) {
        MemoriLogger.LOGGER.log(Level.INFO, evt.toString());
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializePvCGame();
            }
        } else {
            initializePvCGame();
        }
    }

    protected void initializePvCGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new LevelsScreen(sceneHandler, GameType.VS_CPU);
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializePvPGameEventHandler(Event evt) {
        MemoriLogger.LOGGER.log(Level.INFO, evt.toString());
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializePvPGame();
            }
        } else {
            initializePvPGame();
        }
    }

    protected void initializePvPGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        if (RequestManager.networkAvailable()) {
            new RegisterOrLoginScreen(sceneHandler);
        } else {
            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "no_network.mp3", false);
        }

    }

    @FXML
    protected void myScoresEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                myScores();
            }
        } else {
            myScores();
        }
    }


    protected void myScores() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new FXHighScoresScreen(sceneHandler, sceneHandler.getMainWindow());
    }

    @FXML
    protected void goToSponsorsPageEventHandler(Event evt) {
        MemoriLogger.LOGGER.log(Level.INFO, evt.toString());
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                goToSponsorsPage();
            }
        } else {
            goToSponsorsPage();
        }
    }

    protected void goToSponsorsPage() {
        new SponsorsScreen(sceneHandler, sceneHandler.getMainWindow());
    }

    @FXML
    protected void goToLanguagesScreen(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                new LanguageSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
            }
        } else {
            new LanguageSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
        }
    }

    @FXML
    protected void headphonesAdjustmentEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                headphonesAdjustment();
            }
        } else {
            headphonesAdjustment();
        }
    }

    protected void headphonesAdjustment() {
        audioEngine.playBalancedSound(-1.0, this.miscellaneousSoundsBasePath + "left_headphone.mp3", true);
        audioEngine.playBalancedSound(1.0, this.miscellaneousSoundsBasePath + "right_headphone.mp3", true);
    }
}