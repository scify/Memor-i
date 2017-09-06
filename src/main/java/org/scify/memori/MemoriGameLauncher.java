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
    private GameType gameType;

    public MemoriGameLauncher(FXSceneHandler sceneHandler) {
        GameLevelService gameLevelService = new GameLevelService();
        gameLevels = new ArrayList<>();
        gameLevels = gameLevelService.createGameLevels();
        this.sceneHandler = sceneHandler;
    }

    public void startTutorialGame() {
        GameLevelService gameLevelService = new GameLevelService();
        gameLevels = new ArrayList<>();
        gameLevels = gameLevelService.createGameLevels();
        MemoriGameLevel gameLevel = gameLevels.get(0);
        MainOptions.TUTORIAL_MODE = true;
        startGameForLevel(gameLevel, GameType.TUTORIAL);
    }

    public void startSinglePlayerGame(MemoriGameLevel gameLevel) {
        MainOptions.TUTORIAL_MODE = false;
        startGameForLevel(gameLevel, GameType.SINGLE_PLAYER);
    }

    public void startPVCPUGame(MemoriGameLevel gameLevel) {
        MainOptions.TUTORIAL_MODE = false;
        startGameForLevel(gameLevel, GameType.VS_CPU);
    }

    public void startPvPGame(MemoriGameLevel gameLevel) {
        MainOptions.TUTORIAL_MODE = false;
        startGameForLevel(gameLevel, GameType.VS_PLAYER);
    }

    private void startGameForLevel(MemoriGameLevel gameLevel, GameType gameType) {
        FXMemoriGame game = createNewGame(gameLevel, gameType);
        game.setGameType(gameType);
        game.initialize(gameLevel);
        startGameThread(game, gameLevel);
    }

    public void startGameForLevel(MemoriGameLevel gameLevel, GameType gameType, Map<CategorizedCard, Point2D> cards) {
        FXMemoriGame game = createNewGame(gameLevel, gameType);
        game.setGameType(gameType);
        game.initialize(cards, gameLevel);
        startGameThread(game, gameLevel);
    }

    private FXMemoriGame createNewGame(MemoriGameLevel gameLevel, GameType gameType) {
        this.gameType = gameType;
        audioEngine.pauseCurrentlyPlayingAudios();
        MainOptions.GAME_LEVEL_CURRENT = gameLevel.getLevelCode();
        return new FXMemoriGame(sceneHandler, gameLevel);
    }

    private void startGameThread(FXMemoriGame game, MemoriGameLevel gameLevel) {
        // Run game in separate thread
        ExecutorService es = Executors.newFixedThreadPool(1);
        Future<GameEndState> future = es.submit(game);
        es.shutdown();

        //this code will execute once the user exits the game
        // (either to go to next level or to exit)
        try {
            GameEndState result = future.get();
            //quit to main screen
            switch (result) {
                case SAME_LEVEL:
                    sceneHandler.simplePopScene();
                    startGameForLevel(gameLevel, gameType);
                    break;
                case GAME_FINISHED:
                    quitToMainScreen();
                    break;
                case NEXT_LEVEL:
                    playNextLevel(gameLevel);
                    break;
                default:
                    break;
            }
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
        startGameForLevel(gameLevelNext, gameType);
    }

    private void quitToMainScreen() {
        System.err.println("QUITING TO MAIN SCREEN");
        if (MainOptions.TUTORIAL_MODE)
            MainOptions.TUTORIAL_MODE = false;
        sceneHandler.popScene();
    }

    private void playNextLevel(MemoriGameLevel gameLevel) {
        sceneHandler.simplePopScene();
        switch (gameType) {
            case TUTORIAL:
                startGameForLevel(gameLevel, GameType.SINGLE_PLAYER);
                break;
            case VS_CPU:
                loadNextLevel();
                break;
            case SINGLE_PLAYER:
                loadNextLevel();
                break;
            default:
                break;
        }
    }
}
