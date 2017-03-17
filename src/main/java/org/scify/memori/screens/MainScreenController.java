
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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.scify.memori.GameLevelService;
import org.scify.memori.MainOptions;
import org.scify.memori.MemoriGameLevel;
import org.scify.memori.ResourceLocator;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXMemoriGame;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class MainScreenController implements Initializable {


    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    private MemoriConfiguration configuration;
    private String miscellaneousSoundsBasePath;

    public MainScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
        System.err.println("Constructor running...");
    }

    protected Stage primaryStage;
    protected Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

                MainOptions.gameLevel = gameLevel.getLevelCode();
                MainOptions.NUMBER_OF_ROWS = (int) gameLevel.getDimensions().getX();
                MainOptions.NUMBER_OF_COLUMNS = (int) gameLevel.getDimensions().getY();
                Thread thread = new Thread(() -> startNormalGame(gameLevel));
                thread.start();
            } else if (event.getCode() == ESCAPE) {
                exitScreen();
            }
        });
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
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                System.exit(0);
            }
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
        VBox gameLevelsContainer = (VBox) primaryScene.lookup("#gameLevelsDiv");
        addGameLevelButtons(gameLevelsContainer);

        primaryScene.lookup("#welcome").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "welcome.mp3", false);
            }
        });
        attachButtonClickHandlers();
        primaryStage.show();

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
            MainOptions.TUTORIAL_MODE = true;
            MemoriGameLevel gameLevel = gameLevels.get(0);
            MainOptions.gameLevel = gameLevel.getLevelCode();
            MainOptions.NUMBER_OF_ROWS = (int) gameLevel.getDimensions().getX();
            MainOptions.NUMBER_OF_COLUMNS = (int) gameLevel.getDimensions().getY();
            Thread thread = new Thread(() -> startNormalGame(gameLevel));
            thread.start();
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

    private void startNormalGame(MemoriGameLevel gameLevel) {
        MemoriLogger.LOGGER.log(Level.INFO, "Starting a new game on level: " + gameLevel.getLevelName());
        audioEngine.pauseCurrentlyPlayingAudios();
        FXMemoriGame game = new FXMemoriGame(sceneHandler, gameLevel);
        game.initialize();

        // Run game in separate thread
        ExecutorService es = Executors.newFixedThreadPool(1);
        Future<Integer> future = es.submit(game);
        es.shutdown();
        // While the game has not finished
        // sleep
//        while(!future.isDone()) {
//            try {
//                Thread.sleep(100L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        //this code will execute once the user exits the game
        // (either to go to next level or to exit)
        try {
            Integer result = future.get();
            //quit to main screen
            if (result == 1) {
                System.err.println("QUITING TO MAIN SCREEN");
                if (MainOptions.TUTORIAL_MODE)
                    MainOptions.TUTORIAL_MODE = false;
                sceneHandler.popScene();
            } else if (result == 2) // load next level
            {
                sceneHandler.simplePopScene();
                if (MainOptions.TUTORIAL_MODE) {
                    //if the last game was in tutorial mode, load the first normal game
                    MainOptions.TUTORIAL_MODE = false;
                    startNormalGame(gameLevel);
                } else
                    loadNextLevel();

            } else if (result == 3) //play same level again
            {
                sceneHandler.simplePopScene();
                startNormalGame(gameLevel);
            }
            System.out.println(result);
        } catch (InterruptedException | ExecutionException e) {
            MemoriLogger.LOGGER.log(Level.SEVERE, "Game exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Gets the next level and starts a new game on this level.
     */
    private void loadNextLevel() {
        MainOptions.gameLevel++;
        MemoriGameLevel gameLevelNext = gameLevels.get(MainOptions.gameLevel);

        System.err.println("next level: " + gameLevelNext.getDimensions().getX() + ", " + gameLevelNext.getDimensions().getY());

        MainOptions.NUMBER_OF_ROWS = (int) gameLevelNext.getDimensions().getX();
        MainOptions.NUMBER_OF_COLUMNS = (int) gameLevelNext.getDimensions().getY();
        startNormalGame(gameLevelNext);
    }
}
