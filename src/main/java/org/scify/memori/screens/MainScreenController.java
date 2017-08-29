
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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.scify.memori.*;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.network.GameRequestManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

import static javafx.scene.input.KeyCode.*;

public class MainScreenController implements Initializable {


    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    private MemoriConfiguration configuration;
    private String miscellaneousSoundsBasePath;
    private Stage primaryStage;
    private Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();

    public MainScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * Pauses all sounds and exits the application
     */
    private void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        System.exit(0);
    }

    public void setParameters(Stage primaryStage, Scene primaryScene) {
        this.primaryScene = primaryScene;
        this.primaryStage = primaryStage;

        primaryScene.getStylesheets().add("css/style.css");
        primaryStage.show();
        primaryStage.requestFocus();
        primaryStage.sizeToScene();
        primaryStage.setFullScreen(true);
        primaryStage.setOnCloseRequest(we -> {
            System.out.println("Stage is closing");
            System.exit(0);
        });

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
        primaryScene.lookup("#welcome").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                //audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "welcome.mp3", false);
            }
        });
        attachButtonClickHandlers();
        primaryStage.show();
//        Thread gameRequestsThread = new Thread(this::queryServerForGameRequests);
//        gameRequestsThread.start();

    }

    private void queryServerForGameRequests() {
        String answer = null;
        int timesCalled = 0;
        while (answer == null) {
            timesCalled ++;
            ScheduledExecutorService scheduler = Executors
                    .newScheduledThreadPool(1);
            ScheduledFuture<String> future = scheduler.schedule(
                    new GameRequestManager("GET_REQUESTS"), 5, TimeUnit.SECONDS);
            try {
                answer = future.get();
                System.out.println(answer);
                System.out.println("times called: " + timesCalled);
                if(answer != null) {
                    listenForAnswerToGameRequest();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void listenForAnswerToGameRequest() {
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                // TODO: accept game request
                System.out.println("game request accepted");
                new GameRequestScreen(sceneHandler);
            } else if(event.getCode() == BACK_SPACE) {
                // TODO: reject game request
                System.out.println("game request rejected");
            }
        });
    }

    private void setStageFavicon(Stage primaryStage) {
        ResourceLocator resourceLocator = new ResourceLocator();
        String gameCoverImgPath = resourceLocator.getCorrectPathForFile(configuration.getProjectProperty("IMAGES_BASE_PATH") + configuration.getProjectProperty("GAME_COVER_IMG_PATH"),  "game_cover.png");
        //set the "favicon"
        Image faviconImage = new Image(gameCoverImgPath);
        primaryStage.getIcons().add(faviconImage);
    }


    /**
     * Attaches click handlers to fixed buttons (tutorial, exit, etc)
     */
    private void attachButtonClickHandlers() {
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
        if (evt.getCode() == SPACE) {
            exitScreen();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializeTutorialGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            GameLevelService gameLevelService = new GameLevelService();
            gameLevels = new ArrayList<>();
            gameLevels = gameLevelService.createGameLevels();
            MainOptions.TUTORIAL_MODE = true;
            MemoriGameLevel gameLevel = gameLevels.get(0);
            MainOptions.GAME_LEVEL_CURRENT = gameLevel.getLevelCode();
            MainOptions.NUMBER_OF_ROWS = (int) gameLevel.getDimensions().getX();
            MainOptions.NUMBER_OF_COLUMNS = (int) gameLevel.getDimensions().getY();
            MainOptions.GAME_TYPE = 1;
            LevelsScreenController controller = new LevelsScreenController();
            Thread thread = new Thread(() -> controller.startGame(gameLevel, sceneHandler));
            thread.start();
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializeSinglePlayerGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            MainOptions.GAME_TYPE = 1;
            audioEngine.pauseCurrentlyPlayingAudios();
            LevelsScreen highScoresScreen = new LevelsScreen(sceneHandler);
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializePvCGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {

            MainOptions.GAME_TYPE = 2;
            audioEngine.pauseCurrentlyPlayingAudios();
            LevelsScreen highScoresScreen = new LevelsScreen(sceneHandler);
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     * @param evt the click event
     */
    @FXML
    protected void initializePvPGame(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
//            MainOptions.GAME_TYPE = 3;
//            // if the player is already logged in, go directly to available players screen
//            PlayerManager playerManager = new PlayerManager();
//            String stringPlayerId = playerManager.getIdOfLastPlayer();
//            if(stringPlayerId != null) {
//                // go to available players screen
//                AvailablePlayersScreen availablePlayersScreen = new AvailablePlayersScreen(sceneHandler);
//            } else {
//                // else go to user name screen
//                UserNameScreen userNameScreen = new UserNameScreen(sceneHandler);
//            }
            new RegisterOrLoginScreen(sceneHandler);

        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }


    @FXML
    protected void myScores(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            audioEngine.pauseCurrentlyPlayingAudios();
            FXHighScoresScreen highScoresScreen = new FXHighScoresScreen(sceneHandler, sceneHandler.getMainWindow());
        } else if (evt.getCode() == ESCAPE) {
            System.exit(0);
        }
    }

    @FXML
    protected void goToSponsorsPage(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            SponsorsScreen sponsorsScreen = new SponsorsScreen(sceneHandler, sceneHandler.getMainWindow());
        } else if (evt.getCode() == ESCAPE) {
            System.exit(0);
        }
    }


    @FXML
    protected void headphonesAdjustment(KeyEvent evt) {
        if (evt.getCode() == SPACE) {
            audioEngine.playBalancedSound(-1.0, this.miscellaneousSoundsBasePath + "left_headphone.mp3", true);
            audioEngine.playBalancedSound(1.0, this.miscellaneousSoundsBasePath + "right_headphone.mp3", true);
        } else if (evt.getCode() == ESCAPE) {
            System.exit(0);
        }
    }


}
