package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import org.scify.memori.*;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXMemoriGame;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.ServerOperationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

import static javafx.scene.input.KeyCode.*;

public class LevelsScreenController {

    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    private Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private int opponentId;
    private GameRequestManager gameRequestManager = new GameRequestManager();
    MemoriGameLauncher gameLauncher;

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
    private void levelBtnHandler(Button gameLevelBtn, MemoriGameLevel gameLevel) {

        gameLevelBtn.setOnKeyPressed(event -> {
            if (event.getCode() == SPACE) {
                MainOptions.GAME_LEVEL_CURRENT = gameLevel.getLevelCode();
                MainOptions.NUMBER_OF_ROWS = (int) gameLevel.getDimensions().getX();
                MainOptions.NUMBER_OF_COLUMNS = (int) gameLevel.getDimensions().getY();
                if(MainOptions.GAME_TYPE != 3) {
                    Thread thread = new Thread(() -> gameLauncher.startNormalGame(gameLevel));
                    thread.start();
                } else {
                    sendGameRequest(gameLevel);
                }
            } else if (event.getCode() == ESCAPE) {
                exitScreen();
            }
        });
    }

    private void sendGameRequest(MemoriGameLevel gameLevel) {
        String serverResponse = gameRequestManager.sendGameRequestToPlayer(PlayerManager.getPlayerId(), opponentId, MainOptions.GAME_LEVEL_CURRENT);
        if(serverResponse != null) {
            parseGameRequestServerResponse(serverResponse, gameLevel);
        }
    }

    private void parseGameRequestServerResponse(String serverResponse, MemoriGameLevel gameLevel) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        String responseParameters;
        switch (code) {
            case 1:
                // Game Request sent
                JSONObject responseObj = new JSONObject(serverResponse);
                JSONObject paramsObj = responseObj.getJSONObject("parameters");
                int gameRequestId = paramsObj.getInt("game_request_id");
                GameRequestManager.setGameRequestId(gameRequestId);
                System.err.println("Success. Game request id: " + gameRequestId);
                queryServerForGameRequestReply(gameLevel);
                break;
            case 2:
                // Error in creating game request
                responseParameters = (String) response.getParameters();
                System.err.println("ERROR: " + responseParameters);
                break;
            case 3:
                // Error in server validation rules
                responseParameters = (String) response.getParameters();
                System.err.println("ERROR: " + responseParameters);
                break;
            default:
                break;
        }
    }

    private void queryServerForGameRequestReply(MemoriGameLevel gameLevel) {
        ServerOperationResponse serverOperationResponse = null;
        int timesCalled = 0;
        while (serverOperationResponse == null) {
            timesCalled ++;
            ScheduledExecutorService scheduler = Executors
                    .newScheduledThreadPool(1);
            ScheduledFuture<ServerOperationResponse> future = scheduler.schedule(
                    new GameRequestManager("GET_GAME_REQUEST_REPLY"), 5, TimeUnit.SECONDS);
            try {
                serverOperationResponse = future.get();
                System.out.println(serverOperationResponse);
                System.out.println("times called: " + timesCalled);
                if(serverOperationResponse != null) {
                    // we got a reply

                    if(serverOperationResponse.getMessage().equals("accepted")) {
                        // TODO inform user that the request was accepted and prompt
                        // to press enter to start the game
                        setAllLevelButtonsAsDisabled();
                        promptForEnterAndStartGame(gameLevel);
                    } else if(serverOperationResponse.getMessage().equals("rejected")) {
                        // TODO inform user that the request was rejected and prompt
                        // either to select another level
                        // or to press escape and select another opponent
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAllLevelButtonsAsDisabled() {
        VBox gameLevelsContainer = (VBox) primaryScene.lookup("#gameLevelsDiv");
        for(Node node: gameLevelsContainer.getChildren()) {
            node.setDisable(true);
        }
    }

    private void promptForEnterAndStartGame(MemoriGameLevel gameLevel) {
        // Inform the player that the opponent accepted
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                System.out.println("game is about to start");
                Thread thread = new Thread(() -> {PlayerManager.localPlayerIsInitiator = true; gameLauncher.startNormalGame(gameLevel);});
                thread.start();
            } else if(event.getCode() == ESCAPE) {
                // TODO send request to server to mark GameRequest as "canceled"
                System.out.println("game rejected");
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
        gameLauncher = new MemoriGameLauncher(sceneHandler);
        sceneHandler.pushScene(levelsScreenScene);
        VBox gameLevelsContainer = (VBox) levelsScreenScene.lookup("#gameLevelsDiv");
        addGameLevelButtons(gameLevelsContainer);
    }


    public void startGame(MemoriGameLevel gameLevel, FXSceneHandler sceneHandler) {
        this.sceneHandler = sceneHandler;
        gameLauncher.startNormalGame(gameLevel);
    }

    public void setOpponentId(int opponentId) {
        this.opponentId = opponentId;
        Player opponent = new Player(opponentId);
        PlayerManager.setOpponentPlayer(opponent);
    }
}
