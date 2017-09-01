package org.scify.memori;

import org.scify.memori.card.CategorizedCard;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXMemoriGame;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriLogger;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class MemoriGameLauncher {

    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    private FXSceneHandler sceneHandler;
    private FXAudioEngine audioEngine = new FXAudioEngine();

    public MemoriGameLauncher(FXSceneHandler sceneHandler) {
        GameLevelService gameLevelService = new GameLevelService();
        gameLevels = new ArrayList<>();
        gameLevels = gameLevelService.createGameLevels();
        this.sceneHandler = sceneHandler;
    }

    public void startNormalGame(MemoriGameLevel gameLevel) {
        MemoriLogger.LOGGER.log(Level.INFO, "Starting a new game on level: " + gameLevel.getLevelName());
        audioEngine.pauseCurrentlyPlayingAudios();
        FXMemoriGame game = new FXMemoriGame(sceneHandler, gameLevel);
        game.initialize();
        startGameThread(game, gameLevel);
    }

    public void startNormalGameWithCards(MemoriGameLevel gameLevel, Map<CategorizedCard, Point2D> cards) {
        MemoriLogger.LOGGER.log(Level.INFO, "Starting a new game with given cards on level: " + gameLevel.getLevelName());
        audioEngine.pauseCurrentlyPlayingAudios();
        FXMemoriGame game = new FXMemoriGame(sceneHandler, gameLevel);
        game.initialize(cards);
        startGameThread(game, gameLevel);
    }

    private void startGameThread(FXMemoriGame game, MemoriGameLevel gameLevel) {
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
