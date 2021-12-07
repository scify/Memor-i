package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.json.JSONObject;
import org.scify.memori.GameLevelService;
import org.scify.memori.MemoriGameLauncher;
import org.scify.memori.MemoriGameLevel;
import org.scify.memori.PlayerManager;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.AudioEngine;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.RequestManager;
import org.scify.memori.network.ServerOperationResponse;
import org.scify.memori.network.ServerResponse;
import org.scify.memori.tts.TTSFacade;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.SPACE;

public class LevelsScreenController extends MemoriScreenController implements Initializable {

    private Scene primaryScene;
    private final AudioEngine audioEngine = FXAudioEngine.getInstance();
    ;
    private int opponentId;
    private final GameRequestManager gameRequestManager = new GameRequestManager();
    private MemoriGameLauncher gameLauncher;
    private GameType gameType;
    private final String miscellaneousSoundsBasePath;
    private int currentGameRequestId = 0;
    private Thread threadSetPlayerOnline;
    private Thread threadGameRequestReply;
    @FXML
    Button messageText;
    @FXML
    Button infoText;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }

    public LevelsScreenController() {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        this.miscellaneousSoundsBasePath = configuration.getDataPackProperty("MISCELLANEOUS_SOUNDS");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene, GameType gameType) {
        super.setParameters(sceneHandler, levelsScreenScene);
        this.primaryScene = levelsScreenScene;
        this.gameType = gameType;
        VBox gameLevelsContainer = (VBox) this.primaryScene.lookup("#gameLevelsDiv");
        addGameLevelButtons(gameLevelsContainer);

        primaryScene.lookup("#infoText").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "levels_screen_welcome.mp3", false);
            }
        });
        if (gameType.equals(GameType.VS_PLAYER))
            setPlayerOnlineThread();
    }

    private void queryForGameRequestReplyThread(MemoriGameLevel gameLevel) {
        threadGameRequestReply = new Thread(() -> queryServerForGameRequestReply(gameLevel));
        threadGameRequestReply.start();
    }

    private void setPlayerOnlineThread() {
        threadSetPlayerOnline = new Thread(() -> setPlayerOnline());
        threadSetPlayerOnline.start();
    }

    private void setPlayerOnline() {
        PlayerManager playerManager = new PlayerManager();
        while (true) {
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
     *
     * @param buttonsContainer FXML container (div) for adding the buttons
     */
    private void addGameLevelButtons(VBox buttonsContainer) {
        GameLevelService gameLevelService = new GameLevelService();
        List<MemoriGameLevel> gameLevels;
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

    /**
     * Pauses all sounds and exits the application
     */
    @FXML
    protected void exitScreen() {
        if (currentGameRequestId != 0)
            cancelGameRequest();
        audioEngine.pauseCurrentlyPlayingAudios();
        if (gameType.equals(GameType.VS_PLAYER)) {
            threadSetPlayerOnline.interrupt();
            if (threadGameRequestReply != null)
                threadGameRequestReply.interrupt();
            super.exitScreen();
            new InvitePlayerScreen(sceneHandler);
        } else {
            super.exitScreen();
        }

    }

    private Button btnClicked;

    /**
     * When the user clicks on a game level button, a new Game should start
     *
     * @param gameLevelBtn the button clcked
     * @param gameLevel    the game level associated with this button
     */
    private void levelBtnHandler(Button gameLevelBtn, MemoriGameLevel gameLevel) {
        gameLevelBtn.setOnKeyPressed(event -> {
            if (event.getCode() == SPACE) {
                startGameForGameLevel(gameLevelBtn, gameLevel);
            }
        });
        gameLevelBtn.setOnTouchPressed(event -> {
            startGameForGameLevel(gameLevelBtn, gameLevel);
        });
        gameLevelBtn.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY)
                startGameForGameLevel(gameLevelBtn, gameLevel);
        });
    }

    protected void startGameForGameLevel(Button gameLevelBtn, MemoriGameLevel gameLevel) {
        Thread thread;
        gameLauncher = new MemoriGameLauncher(this.sceneHandler);
        TTSFacade.postGameStatus("started");
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

    private void waitForResponseUI() {
        setAllLevelButtonsAsDisabled();
        messageText.setText(resources.getString("wait"));
    }

    private void resetUI() {
        setAllLevelButtonsAsEnabled();
    }

    private void sendGameRequest(MemoriGameLevel gameLevel) {
        String serverResponse = gameRequestManager.sendGameRequestToPlayer(PlayerManager.getPlayerId(), opponentId, gameLevel.getLevelCode());
        if (serverResponse != null) {
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
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_sent.mp3", false);
                queryForGameRequestReplyThread(gameLevel);
                break;
            case ServerResponse.RESPONSE_ERROR:
            case ServerResponse.VALIDATION_ERROR:
                // Error in server validation rules
                // Error in creating game request
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
            timesCalled++;
            try {
                serverOperationResponse = gameRequestManager.askServerForGameRequestReply();
            } catch (Exception e) {
                shouldContinue = false;
                Thread.currentThread().interrupt();
            }
            if (serverOperationResponse != null) {
                // we got a reply
                if (serverOperationResponse.getMessage().equals("accepted")) {
                    // to press enter to start the game
                    Platform.runLater(() -> messageText.setText(resources.getString("opponent_accepted")));
                    promptToStartGame(gameLevel);
                } else if (serverOperationResponse.getMessage().equals("rejected")) {
                    cancelGameRequest();
                    Platform.runLater(() -> messageText.setText(resources.getString("opponent_rejected")));
                    Platform.runLater(() -> resetUI());
                    Thread voiceThread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_rejected.mp3", true));
                    voiceThread.start();
                    promptToPlayWithCPU();
                }
                shouldContinue = false;
                Thread.currentThread().interrupt();
            } else {
                try {
                    Thread.sleep(GameRequestManager.SHUFFLE_CARDS_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    shouldContinue = false;
                }
            }
            System.out.println("timesCalled " + timesCalled);
            if (timesCalled > RequestManager.MAX_REQUEST_TRIES) {
                System.err.println("Opponent not answering!");
                Thread voiceThread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_rejected.mp3", true));
                voiceThread.start();
                Platform.runLater(() -> messageText.setText(resources.getString("opponent_not_answering")));
                cancelGameRequest();
                shouldContinue = false;
                Thread.currentThread().interrupt();
            }
        }
        System.err.println("Out of the while loop");
    }

    private void cancelGameRequest() {
        Platform.runLater(() -> resetUI());
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
        for (Node node : gameLevelsContainer.getChildren()) {
            node.setDisable(true);
        }
    }

    private void setAllLevelButtonsAsEnabled() {
        VBox gameLevelsContainer = (VBox) primaryScene.lookup("#gameLevelsDiv");
        for (Node node : gameLevelsContainer.getChildren()) {
            node.setDisable(false);
        }
        if (btnClicked != null)
            btnClicked.requestFocus();
    }

    private void promptToStartGame(MemoriGameLevel gameLevel) {
        // Inform the player that the opponent accepted
        Thread voiceThread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "request_accepted.mp3", false));
        voiceThread.start();
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == ENTER) {
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
