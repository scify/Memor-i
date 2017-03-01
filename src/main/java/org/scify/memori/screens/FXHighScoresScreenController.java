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

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.scify.memori.GameLevelService;
import org.scify.memori.MemoriGameLevel;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.HighScoresHandlerImpl;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.input.KeyCode.SPACE;

public class FXHighScoresScreenController {

    /**
     * An Audio Engine object, able to play sounds
     */
    private HighScoresHandlerImpl highScoreHandler;
    private FXAudioEngine audioEngine;
    protected FXSceneHandler sceneHandler;

    @FXML
    private Button level1;
    @FXML
    private Button level2;
    @FXML
    private Button level3;
    @FXML
    private Button level4;
    @FXML
    private Button level5;
    @FXML
    private Button level6;
    @FXML
    private Button level7;

    private MemoriConfiguration configuration;
    protected String miscellaneousSoundsBasePath;
    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    
    public void setParameters(FXSceneHandler sHandler, Scene scoresScene) {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
        //initialize the audio engine object
        audioEngine = new FXAudioEngine();

        highScoreHandler = new HighScoresHandlerImpl();
        sceneHandler = sHandler;
        sceneHandler.pushScene(scoresScene);
        scoresScene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    exitScreen();
                    break;
            }
        });

        FXRenderingEngine.setGamecoverIcon(scoresScene, "gameCoverImgContainer");

        VBox gameLevelsContainer = (VBox) scoresScene.lookup("#gameLevelsDiv");
        addGameLevelButtons(gameLevelsContainer);


//        scoresScene.lookup("#level1").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level1.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#level2").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level2.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#level3").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level3.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#level4").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level4.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#level5").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level5.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#level6").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level6.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#level7").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "level7.mp3", false);
//            }
//        });
//
//        scoresScene.lookup("#back").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
//            if (newPropertyValue) {
//                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "back.mp3", false);
//            }
//        });
    }

    /**
     * Gets all game levels available and adds a button for each one
     * @param buttonsContainer FXML container (div) for adding the buttons
     */
    private void addGameLevelButtons(VBox buttonsContainer) {
        GameLevelService gameLevelService = new GameLevelService();
        gameLevels = new ArrayList<>();
        gameLevels = gameLevelService.createGameLevels();
        for (MemoriGameLevel currLevel : gameLevels) {
            Button gameLevelBtn = new Button();
            gameLevelBtn.setText(currLevel.getLevelName());
            gameLevelBtn.getStyleClass().add("optionButton");
            gameLevelBtn.setId(String.valueOf(currLevel.getLevelCode()));
            gameLevelBtn.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(currLevel.getIntroScreenSound(), false);
                }
            });
            buttonsContainer.getChildren().add(gameLevelBtn);
            levelBtnHandler(gameLevelBtn, currLevel);
        }
    }

    /**
     * When the user clicks on a game level button, a new Game should start
     * @param gameLevelBtn the button clcked
     * @param gameLevel the game level associated with this button
     */
    protected void levelBtnHandler(Button gameLevelBtn, MemoriGameLevel gameLevel) {

        gameLevelBtn.setOnKeyPressed(event -> {
            if (event.getCode() == SPACE) {

                System.err.println("high score: " + highScoreHandler.getHighScoreForLevel(gameLevel.getLevelCode()));
                String timestampStr = highScoreHandler.getHighScoreForLevel(gameLevel.getLevelCode());
                // if there is a score in this level (ie if score is nt null)
                // play relevant audio clips
                // else play informative audio clip prompting to play the score
                if (timestampStr != null) {
                    String[] tokens = timestampStr.split(":");
                    int minutes = Integer.parseInt(tokens[1]);
                    int seconds = Integer.parseInt(tokens[2]);
                    if (minutes != 0) {
                        audioEngine.playNumSound(minutes);
                        System.out.println("minutes: " + minutes);
                        if (minutes > 1)
                            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "minutes.mp3", true);
                        else
                            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "minute.mp3", true);
                    }
                    if (minutes != 0 && seconds != 0)
                        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "and.mp3", true);
                    if (seconds != 0) {
                        audioEngine.playNumSound(seconds);
                        if (seconds > 1)
                            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "seconds.mp3", true);
                        else
                            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "second.mp3", true);
                    }
                } else {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "no_score.mp3", false);
                }
            }
        });
    }

    /**
     * Goes back to main screen
     * @param evt the keyboard event
     */
    @FXML
    protected void backToMainScreen(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            exitScreen();
        }
    }

    protected void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
