package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import org.scify.memori.*;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.Text2Speech;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.RequestManager;
import org.scify.memori.network.ServerOperationResponse;
import org.scify.memori.network.ServerResponse;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.input.KeyCode.*;

public class LevelsScreenController {

    private List<MemoriGameLevel> gameLevels = new ArrayList<>();
    private Scene primaryScene;
    private FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private int opponentId;
    private GameRequestManager gameRequestManager = new GameRequestManager();
    private MemoriGameLauncher gameLauncher;
    private GameType gameType;
    private String miscellaneousSoundsBasePath;
    private MemoriConfiguration configuration;
    private int currentGameRequestId = 0;
    private Thread threadSetPlayerOnline;
    @FXML
    Button messageText;
    @FXML
    Button infoText;

    public LevelsScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene, GameType gameType) {
        this.primaryScene = levelsScreenScene;
        this.sceneHandler = sceneHandler;
        this.gameType = gameType;
        gameLauncher = new MemoriGameLauncher(sceneHandler);
        sceneHandler.pushScene(levelsScreenScene);
        FXRenderingEngine.setGamecoverIcon(this.primaryScene, "gameCoverImgContainer");
        VBox gameLevelsContainer = (VBox) this.primaryScene.lookup("#gameLevelsDiv");
        addGameLevelButtons(gameLevelsContainer);

        primaryScene.lookup("#infoText").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "levels_screen_welcome.mp3", false);
            }
        });
        setPlayerOnlineThread();
    }

    private void setPlayerOnlineThread() {
        threadSetPlayerOnline = new Thread(() -> setPlayerOnline());
        threadSetPlayerOnline.start();
    }

    private void setPlayerOnline() {
        PlayerManager playerManager = new PlayerManager();
        while(true) {
            playerManager.setPlayerOnline();
            try {
                Thread.sleep(PlayerManager.MARK_PLAYER_ACTIVE_CALL_INTERVAL);
            } catch (InterruptedException e) {
                System.err.println("setPlayerOnline thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
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
                if (newPropertyValue && !gameLevelBtn.isDisabled()) {
                    audioEngine.pauseAndPlaySound(currLevel.getIntroScreenSound(), false);
                }
            });
            buttonsContainer.getChildren().add(gameLevelBtn);
            levelBtnHandler(gameLevelBtn, currLevel);
        }
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    /**
     * Pauses all sounds and exits the application
     */
    private void exitScreen() {
        if(currentGameRequestId != 0)
            cancelGameRequest();
        audioEngine.pauseCurrentlyPlayingAudios();
        if(gameType.equals(GameType.VS_PLAYER)) {
            sceneHandler.simplePopScene();
            new InvitePlayerScreen(sceneHandler);
        } else {
            sceneHandler.popScene();
        }
    }

    private Button btnClicked;
    /**
     * When the user clicks on a game level button, a new Game should start
     * @param gameLevelBtn the button clcked
     * @param gameLevel the game level associated with this button
     */
    private void levelBtnHandler(Button gameLevelBtn, MemoriGameLevel gameLevel) {
        gameLevelBtn.setOnKeyPressed(event -> {
            Thread thread;
            if (event.getCode() == SPACE) {
                switch (gameType) {
                    case SINGLE_PLAYER:
                        audioEngine.pauseCurrentlyPlayingAudios();
                        thread = new Thread(() -> gameLauncher.startSinglePlayerGame(gameLevel));
                        thread.start();
                        break;
                    case VS_CPU:
                        audioEngine.pauseCurrentlyPlayingAudios();
                        thread = new Thread(() -> gameLauncher.startPVCPUGame(gameLevel));
                        thread.start();
                        break;
                    case VS_PLAYER:
                        Platform.runLater(() -> waitForResponseUI());
                        btnClicked = gameLevelBtn;
                        thread = new Thread(() -> sendGameRequest(gameLevel));
                        thread.start();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void waitForResponseUI() {
        setAllLevelButtonsAsDisabled();
        messageText.setText("Περίμενε...");
    }

    private void resetUI() {
        setAllLevelButtonsAsEnabled();
    }

    private void sendGameRequest(MemoriGameLevel gameLevel) {
        String serverResponse = gameRequestManager.sendGameRequestToPlayer(PlayerManager.getPlayerId(), opponentId, gameLevel.getLevelCode());
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
            case ServerResponse.RESPONSE_SUCCESSFUL:
                // Game Request sent
                JSONObject responseObj = new JSONObject(serverResponse);
                JSONObject paramsObj = responseObj.getJSONObject("parameters");
                int gameRequestId = paramsObj.getInt("game_request_id");
                GameRequestManager.setGameRequestId(gameRequestId);
                this.currentGameRequestId = gameRequestId;
                System.err.println("Success. Game request id: " + gameRequestId);
                Thread thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_sent.mp3", false));
                thread.start();
                Thread queryThread = new Thread(() -> queryServerForGameRequestReply(gameLevel));
                queryThread.start();
                break;
            case ServerResponse.RESPONSE_ERROR:
                // Error in creating game request
                responseParameters = (String) response.getParameters();
                System.err.println("ERROR: " + responseParameters);
                break;
            case ServerResponse.VALIDATION_ERROR:
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
        boolean shouldContinue = true;
        while (shouldContinue) {
            timesCalled ++;
            try {
                serverOperationResponse = gameRequestManager.askServerForGameRequestReply();
            } catch (Exception e) {
                shouldContinue = false;
                Thread.currentThread().interrupt();
            }
            if(serverOperationResponse != null) {
                // we got a reply
                if(serverOperationResponse.getMessage().equals("accepted")) {
                    // to press enter to start the game
                    Platform.runLater(() -> messageText.setText("Ο παίκτης δέχθηκε! Πάτησε ENTER"));
                    promptToStartGame(gameLevel);
                } else if(serverOperationResponse.getMessage().equals("rejected")) {
                    cancelGameRequest();
                    Platform.runLater(() -> resetUI());
                    Thread voiceThread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_rejected.mp3", true));
                    voiceThread.start();
                    promptToPlayWithCPU();
                    break;
                }
                shouldContinue = false;
                Thread.currentThread().interrupt();
            } else {
                try {
                    Thread.sleep(GameRequestManager.SHUFFLE_CARDS_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if(timesCalled > RequestManager.MAX_REQUEST_TRIES) {
                Thread voiceThread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_rejected.mp3", true));
                voiceThread.start();
                cancelGameRequest();
                shouldContinue = false;
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void cancelGameRequest() {
        Platform.runLater(() -> resetUI());
        Platform.runLater(() -> messageText.setText("Ο παίκτης δεν απαντάει"));
        Thread cancelThread = new Thread(() -> gameRequestManager.cancelGame());
        cancelThread.start();
    }

    private void promptToPlayWithCPU() {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                gameType = GameType.VS_CPU;
                new LevelsScreen(sceneHandler, gameType);
            }
        });
    }

    private void setAllLevelButtonsAsDisabled() {
        VBox gameLevelsContainer = (VBox) primaryScene.lookup("#gameLevelsDiv");
        for(Node node: gameLevelsContainer.getChildren()) {
            node.setDisable(true);
        }
    }

    private void setAllLevelButtonsAsEnabled() {
        VBox gameLevelsContainer = (VBox) primaryScene.lookup("#gameLevelsDiv");
        for(Node node: gameLevelsContainer.getChildren()) {
            node.setDisable(false);
        }
        if(btnClicked != null)
            btnClicked.requestFocus();
    }

    private void promptToStartGame(MemoriGameLevel gameLevel) {
        // Inform the player that the opponent accepted
        Thread voiceThread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_accepted.mp3", false));
        voiceThread.start();
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                PlayerManager.localPlayerIsInitiator = true;
                audioEngine.pauseCurrentlyPlayingAudios();
                threadSetPlayerOnline.interrupt();
                Thread thread = new Thread(() -> gameLauncher.startPvPGame(gameLevel));
                thread.start();
            }
        });
    }

    public void setOpponentId(int opponentId) {
        this.opponentId = opponentId;
        Player opponent = new Player(opponentId);
        PlayerManager.setOpponentPlayer(opponent);
    }
}
