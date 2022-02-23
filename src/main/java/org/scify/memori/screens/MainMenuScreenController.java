
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
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.network.RequestManager;
import org.scify.memori.tts.TTSFacade;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class MainMenuScreenController extends GameTypeSelectionScreenController implements Initializable {

    public Button versus_player;
    public Button headphones_adjustment;
    @FXML
    Button tutorialBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        // if the game has vs_player option enabled, show the button
        // else hide it
        if (configuration.vsPlayerEnabled())
            versus_player.setVisible(true);
        else
            btnContainer.getChildren().remove(versus_player);
        if (configuration.authModeEnabled())
            btnContainer.getChildren().remove(headphones_adjustment);
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene scene) {
        super.setParameters(sceneHandler, scene);
        if (MemoriConfiguration.getInstance().ttsEnabled())
            TTSFacade.postGameStatus("started");
        attachButtonFocusHandlers();
    }

    /**
     * Attaches focus handlers to fixed buttons (tutorial, exit, etc)
     */
    protected void attachButtonFocusHandlers() {
        super.attachButtonFocusHandlers();
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        primaryScene.lookup("#welcome").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !configuration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "welcome.mp3", false);
            }
        });

        if (!configuration.authModeEnabled())
            primaryScene.lookup("#headphones_adjustment").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "headphones_adjustment.mp3", false);
                }
            });

        primaryScene.lookup("#change_language_btn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "change_language.mp3", false);
            }
        });

        primaryScene.lookup("#browse_games").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "find_more_games.mp3", false);
            }
        });

        if (!configuration.ttsEnabled())
            primaryScene.lookup("#tutorialBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "tutorial.mp3", false);
                }
            });

        if (configuration.vsPlayerEnabled()) {
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
    protected void exitGame(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE || keyEvt.getCode() == ESCAPE) {
                exitApp();
            }
        } else {
            exitApp();
        }
    }

    private void exitApp() {
        if (MemoriConfiguration.getInstance().ttsEnabled())
            TTSFacade.postGameStatus("finished");
        MainScreen.exitApplication();
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializePvPGameEventHandler(Event evt) {
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

    public void initializeGameFlavorsScreen(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                new GameFlavorSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
            }
        } else {
            new GameFlavorSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
        }
    }
}
