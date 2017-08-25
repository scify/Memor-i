package org.scify.memori.screens;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.scify.memori.GameLevelService;
import org.scify.memori.MainOptions;
import org.scify.memori.MemoriGameLevel;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXMemoriGame;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.helper.Text2Speech;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class LevelsScreenController {

    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    protected Stage primaryStage;
    protected Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();

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

                MainOptions.GAME_LEVEL_CURRENT = gameLevel.getLevelCode();
                MainOptions.NUMBER_OF_ROWS = (int) gameLevel.getDimensions().getX();
                MainOptions.NUMBER_OF_COLUMNS = (int) gameLevel.getDimensions().getY();
                MainOptions.GAME_TYPE = 1;
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
        sceneHandler.popScene();
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene) {
        this.primaryScene = levelsScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(levelsScreenScene, "gameCoverImgContainer");

        sceneHandler.pushScene(levelsScreenScene);
        VBox gameLevelsContainer = (VBox) levelsScreenScene.lookup("#gameLevelsDiv");
        addGameLevelButtons(gameLevelsContainer);
    }


    public void startGame(MemoriGameLevel gameLevel, FXSceneHandler sceneHandler) {
        this.sceneHandler = sceneHandler;
        startNormalGame(gameLevel);
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

        MemoriGameLevel gameLevelNext = gameLevels.get(MainOptions.GAME_LEVEL_CURRENT);
        MainOptions.GAME_LEVEL_CURRENT++;
        System.err.println("next level: " + gameLevelNext.getDimensions().getX() + ", " + gameLevelNext.getDimensions().getY());

        MainOptions.NUMBER_OF_ROWS = (int) gameLevelNext.getDimensions().getX();
        MainOptions.NUMBER_OF_COLUMNS = (int) gameLevelNext.getDimensions().getY();
        startNormalGame(gameLevelNext);
    }
}
