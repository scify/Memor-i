package org.scify.memori.screens;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import com.google.gson.JsonObject;
import javafx.scene.text.Text;
import org.scify.memori.*;
import org.scify.memori.card.CategorizedCard;
import org.scify.memori.card.MemoriCardService;
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

import java.awt.geom.Point2D;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static javafx.scene.input.KeyCode.*;

public class InvitePlayerScreenController {

    private Scene primaryScene;
    @FXML
    TextField username;

    @FXML
    Text invitationText;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private GameRequestManager gameRequestManager;
    private PlayerManager playerManager;
    private MemoriGameLauncher gameLauncher;
    private Player candidateOpponent;
    private ScheduledExecutorService gameRequestsExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService setPlayerOnlineExecutorService = Executors.newScheduledThreadPool(1);
    private int gameLevelId;
    private static boolean shouldQueryForRequests = true;
    private static boolean shouldQueryForMarkingPlayerActive = true;
    private String miscellaneousSoundsBasePath;
    private Thread threadUI;

    public InvitePlayerScreenController() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        gameRequestManager = new GameRequestManager();
        playerManager = new PlayerManager();
        sceneHandler.pushScene(userNameScreenScene);
        gameLauncher = new MemoriGameLauncher(sceneHandler);
        shouldQueryForRequests = true;
        shouldQueryForMarkingPlayerActive = true;
        queryServerForGameRequestsThread();
        setPlayerOnlineThread();
        setPlayerAsNotInGameThread();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "invite_player_screen_welcome.mp3", false);
    }

    private void setPlayerOnlineThread() {
        Thread thread = new Thread(() -> setPlayerOnline());
        thread.start();
    }

    private void setPlayerAsNotInGameThread() {
        Thread thread = new Thread(() -> setPlayerNotInGame());
        thread.start();
    }

    private void queryServerForGameRequestsThread() {
        Thread thread = new Thread(() -> queryServerForGameRequests());
        thread.start();
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    private void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
        shouldQueryForMarkingPlayerActive = false;
        shouldQueryForRequests = false;
    }

    @FXML
    protected void submitUsername(KeyEvent evt) {
        String opponentUsername = username.getText().trim();
        if (evt.getCode() == ENTER){
            evt.consume();
            Thread thread;
            String cleanString = Normalizer.normalize(opponentUsername, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            cleanString = cleanString.trim();
            if(cleanString.length() == 0) {
                thread = new Thread(() -> searchForRandomPlayer());
                thread.start();
            } else {
                boolean valid = cleanString.matches("\\w+");
                if (valid) {
                    String finalCleanString = cleanString;
                    // if username is the same as the logged in player username, cancel
                    if(cleanString.equals(PlayerManager.getLocalPlayer().getName())) {
                        System.out.println("Same username");
                        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "cannot_invite_self.mp3", false);
                        resetUIThread();
                        setUserNameEmptyThread();
                    } else {
                        thread = new Thread(() -> getPlayerAvailability(finalCleanString));
                        thread.start();
                    }
                } else {
                    thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_input.mp3", false));
                    thread.start();
                    setUserNameEmptyThread();
                }
            }
        }

    }

    private void setUserNameEmptyThread() {
        threadUI = new Thread(() -> username.setText(""));
        threadUI.start();
    }

    private void searchForRandomPlayer() {
        String serverResponse = playerManager.searchForRandomPlayer();
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        if(!response.getParameters().equals(""))
            response.setParameters(g.toJsonTree(response.getParameters()).getAsJsonObject());
        int code = response.getCode();
        switch (code) {
            case ServerResponse.RESPONSE_SUCCESSFUL:
                // found a player
                JsonObject parametersObject = (JsonObject) response.getParameters();
                int playerId = parametersObject.get("player_id").getAsInt();
                Platform.runLater(() -> invitationText.setText("Βρέθηκε διαθέσιμος παίκτης! Πάτησε SPACE για να συνεχίσεις."));
                Thread thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "random_player_found.mp3", false));
                thread.start();
                promptToGoToLevelsPage(playerId);
                break;
            case ServerResponse.RESPONSE_ERROR:
                // error
                break;
            case ServerResponse.VALIDATION_ERROR:
                // server validation not passed
                break;
            case ServerResponse.RESPONSE_EMPTY:
                // player not found
                noAvailablePlayerFound();
                break;
            case ServerResponse.OPPONENT_HAS_ALREADY_SENT_REQUEST:
                // an opponent player has already sent a request
                openRequestExistsFromRandomPlayer();
                break;
            default:
                break;
        }
    }

    private void getPlayerAvailability(String playerUserName) {
        String serverResponse = playerManager.getPlayerAvailability(playerUserName);
        parsePlayerAvailabilityResponse(serverResponse);
    }

    private void parsePlayerAvailabilityResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        if(!response.getParameters().equals(""))
            response.setParameters(g.toJsonTree(response.getParameters()).getAsJsonObject());
        int code = response.getCode();
        switch (code) {
            case ServerResponse.RESPONSE_SUCCESSFUL:
                String playerStatus = response.getMessage();
                if(playerStatus.equals("player_available")) {
                    JsonObject parametersObject = (JsonObject) response.getParameters();
                    int playerId = parametersObject.get("player_id").getAsInt();
                    System.out.println("Player available");
                    Platform.runLater(() -> invitationText.setText("Ο παίκτης είναι διαθέσιμος. Πάτησε SPACE για να συνεχίσεις."));
                    Thread thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_available.mp3", false));
                    thread.start();
                    promptToGoToLevelsPage(playerId);
                    username.setDisable(true);
                } else if(playerStatus.equals("player_not_available")) {
                    System.out.println("Player not available");
                    playerNotAvailable();
                } else if(playerStatus.equals("player_in_game")) {
                    // player is online and playing this game, but plays another game
                    System.out.println("Player available but in other game");
                    Platform.runLater(() -> invitationText.setText("Ο παίκτης είναι σε παιχνίδι. Δοκίμασε αργότερα"));
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_already_in_game.mp3", false);
                }
                break;
            case ServerResponse.RESPONSE_ERROR:
                // error
                break;
            case ServerResponse.VALIDATION_ERROR:
                // server validation not passed
                break;
            case ServerResponse.RESPONSE_EMPTY:
                // player not found
                playerNotFound();
                break;
            case ServerResponse.OPPONENT_HAS_ALREADY_SENT_REQUEST:
                // opponent player has already sent a request
                openRequestExistsFromOpponent();
                break;
        }
    }

    private void openRequestExistsFromOpponent() {
        threadUI = new Thread(() -> invitationText.setText("Αυτός ο παίκτης σου έχει ήδη στείλει πρόσκληση. Περίμενε για να την ακούσεις."));
        threadUI.start();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_already_invited_by_opponent.mp3", false);
    }

    private void openRequestExistsFromRandomPlayer() {
        threadUI = new Thread(() -> invitationText.setText("Ένας παίκτης σου έχει ήδη στείλει πρόσκληση. Περίμενε για να την ακούσεις."));
        threadUI.start();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_already_invited.mp3", false);
    }

    private void playerNotFound() {
        threadUI = new Thread(() -> {
            invitationText.setText("Αυτός ο παίκτης δεν βρέθηκε. Πάτησε SPACE για να παίξεις με τον υπολογιστή.");
            username.setText("");
        });
        threadUI.start();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_not_found_prompt_cpu.mp3", true);
        promptToPlayWithCPU();
    }

    private void playerNotAvailable() {
        threadUI = new Thread(() -> invitationText.setText("Αυτός ο παίκτης δεν είναι διαθέσιμος. Πάτησε SPACE για να παίξεις με τυχαίο παίκτη."));
        threadUI.start();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_not_available.mp3", false);
        promptToPlayWithCPU();
    }

    private void noAvailablePlayerFound() {
        threadUI = new Thread(() -> invitationText.setText("Δεν βρέθηκε διαθέσιμος παίκτης. Πάτησε SPACE για να παίξεις με τον υπολογιστή."));
        threadUI.start();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "no_player_available.mp3", false);
        promptToPlayWithCPU();
    }

    private void promptToGoToLevelsPage(int opponentId) {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                audioEngine.pauseCurrentlyPlayingAudios();
                LevelsScreen levelsScreen = new LevelsScreen(sceneHandler, GameType.VS_PLAYER);
                levelsScreen.setOpponentId(opponentId);
                shouldQueryForRequests = false;
                resetUIThread();
            }
        });
    }

    private void promptToPlayWithCPU() {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                shouldQueryForRequests = false;
                shouldQueryForMarkingPlayerActive = false;
                audioEngine.pauseCurrentlyPlayingAudios();
                new LevelsScreen(sceneHandler, GameType.VS_CPU);
                resetUIThread();
            }
        });
    }

    private void queryServerForGameRequests() {
        ServerOperationResponse serverResponse = null;
        while (serverResponse == null && shouldQueryForRequests) {
            ScheduledFuture<ServerOperationResponse> gameRequestsFuture = gameRequestsExecutorService.schedule(
                    new GameRequestManager("GET_REQUESTS"), GameRequestManager.GAME_REQUESTS_CALL_INTERVAL, TimeUnit.SECONDS);
            try {
                serverResponse = gameRequestsFuture.get();
                if(serverResponse != null) {
                    JsonObject parametersObject = (JsonObject) serverResponse.getParameters();
                    int gameRequestId = parametersObject.get("game_request_id").getAsInt();
                    GameRequestManager.setGameRequestId(gameRequestId);
                    String initiatorUserName = parametersObject.get("initiator_user_name").getAsString();
                    int initiatorId = parametersObject.get("initiator_id").getAsInt();
                    gameLevelId = parametersObject.get("game_level_id").getAsInt();
                    invitationText.setText("Έχεις πρόσκληση από τον παίκτη " +initiatorUserName + "! Πάτησε ENTER για να τη δεχθείς.");
                    threadUI = new Thread(() -> username.setDisable(true));
                    threadUI.start();
                    candidateOpponent = new Player(initiatorUserName, initiatorId);
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "new_request.mp3", false);

                    Thread answerThread = new Thread(() -> answerToGameRequest());
                    answerThread.start();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void answerToGameRequest() {
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                gameRequestManager.sendGameRequestAnswerToServer(true);
                Thread queryThread = new Thread(() ->  queryForGameRequestShuffledCards());
                queryThread.start();
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "game_getting_ready.mp3", false);
                waitForResponseUIThread();
            } else if(event.getCode() == BACK_SPACE) {
                resetUIThread();
                Thread answerThread = new Thread(() ->  gameRequestManager.sendGameRequestAnswerToServer(false));
                answerThread.start();
                // re-check for requests
                queryServerForGameRequestsThread();
            }
        });
    }

    private void resetUIThread() {
        threadUI = new Thread(() -> resetUI());
        threadUI.start();
    }

    private void waitForResponseUIThread() {
        threadUI = new Thread(() -> waitForResponseUI());
        threadUI.start();
    }

    private void resetUI() {
        Platform.runLater(() -> {
            username.setDisable(false);
            invitationText.setText("");
        });
    }

    private void waitForResponseUI() {
        Platform.runLater(() -> {
            username.setDisable(true);
            invitationText.setText("Περίμενε...");
        });
    }

    private void setPlayerOnline() {
        while(shouldQueryForMarkingPlayerActive) {
            ScheduledFuture<String> playerActiveFuture = setPlayerOnlineExecutorService.schedule(
                    new PlayerManager("PLAYER_ACTIVE"), PlayerManager.MARK_PLAYER_ACTIVE_CALL_INTERVAL, TimeUnit.SECONDS);
            try {
                String response = playerActiveFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    private void setPlayerNotInGame() {
        String response = playerManager.setPlayerAsNotInGame();
    }

    private void queryForGameRequestShuffledCards() {
        ServerOperationResponse serverResponse = null;
        int timesCalled = 0;
        while (serverResponse == null) {
            timesCalled ++;

            ScheduledExecutorService scheduler = Executors
                    .newScheduledThreadPool(1);
            ScheduledFuture<ServerOperationResponse> future = scheduler.schedule(
                    new GameRequestManager("GET_SHUFFLED_CARDS"), 0, TimeUnit.SECONDS);
            try {
                serverResponse = future.get();
                if(serverResponse != null) {
                    ArrayList<LinkedTreeMap> jsonCardsArray = (ArrayList<LinkedTreeMap>) serverResponse.getParameters();
                    System.out.println("Got cards!");
                    parseShuffledCardsFromServerAndStartGame(jsonCardsArray);
                }
                if(timesCalled > RequestManager.MAX_REQUEST_TRIES) {
                    audioEngine.playSound(this.miscellaneousSoundsBasePath + "player_cancelled_game.mp3", false);
                    cancelGameRequest();
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelGameRequest() {
        resetUIThread();
        Thread cancelThread = new Thread(() -> gameRequestManager.cancelGame());
        cancelThread.start();
    }

    private void parseShuffledCardsFromServerAndStartGame(ArrayList<LinkedTreeMap> jsonCardsArray) {
        Map<CategorizedCard, Point2D> cardsWithPositions = new HashMap<>();
        List<MemoriGameLevel> gameLevels;
        GameLevelService gameLevelService = new GameLevelService();
        gameLevels = gameLevelService.createGameLevels();
        MemoriGameLevel gameLevel = gameLevels.get(gameLevelId -1);

        MemoriCardService memoriCardService = new MemoriCardService();
        for(LinkedTreeMap cardJsonObj: jsonCardsArray) {
            CategorizedCard nextCard = memoriCardService.getCardFromLabelAndType(cardJsonObj.get("label").toString(), cardJsonObj.get("category").toString());
            cardsWithPositions.put(nextCard, new Point2D.Double(Double.parseDouble(cardJsonObj.get("xPos").toString()), Double.parseDouble(cardJsonObj.get("yPos").toString())));
        }
        resetUIThread();
        Thread thread = new Thread(() -> gameLauncher.startGameForLevel(gameLevel, GameType.VS_PLAYER, cardsWithPositions));
        PlayerManager.setOpponentPlayer(candidateOpponent);
        thread.start();
    }

}
