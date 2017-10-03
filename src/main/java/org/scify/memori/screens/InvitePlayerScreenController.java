package org.scify.memori.screens;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import com.google.gson.JsonObject;
import org.scify.memori.*;
import org.scify.memori.card.CategorizedCard;
import org.scify.memori.card.MemoriCardService;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.RequestManager;
import org.scify.memori.network.ServerOperationResponse;
import org.scify.memori.network.ServerResponse;

import java.awt.geom.Point2D;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;

import static javafx.scene.input.KeyCode.*;

public class InvitePlayerScreenController  implements Initializable {

    private Scene primaryScene;
    @FXML
    TextField username;
    @FXML
    Label invitationText;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private GameRequestManager gameRequestManager;
    private PlayerManager playerManager;
    private MemoriGameLauncher gameLauncher;
    private Player candidateOpponent;
    private int gameLevelId;
    private String miscellaneousSoundsBasePath;
    private Thread threadSetPlayerOnline;
    private Thread threadQueryForGameRequests;
    private Thread threadQueryForShuffleCards;

    public InvitePlayerScreenController() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        gameRequestManager = new GameRequestManager();
        playerManager = new PlayerManager();
        sceneHandler.pushScene(userNameScreenScene);
        gameLauncher = new MemoriGameLauncher(sceneHandler);
        queryServerForGameRequestsThread();
        setPlayerOnlineThread();
        setPlayerAsNotInGameThread();
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "invite_player_screen_welcome.mp3", false);
        resetUI();
    }

    private void setPlayerOnlineThread() {
        threadSetPlayerOnline = new Thread(() -> setPlayerOnline());
        threadSetPlayerOnline.start();
    }

    private void setPlayerAsNotInGameThread() {
        Thread thread = new Thread(() -> playerManager.setPlayerAsNotInGame());
        thread.start();
    }

    private void queryServerForGameRequestsThread() {
        threadQueryForGameRequests = new Thread(() -> queryServerForGameRequests());
        threadQueryForGameRequests.start();
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
        threadSetPlayerOnline.interrupt();
        threadQueryForGameRequests.interrupt();
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
                        resetUI();
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
        Platform.runLater(() -> username.setText(""));
    }

    private void searchForRandomPlayer() {
        waitForResponseUI();
        String serverResponse = playerManager.searchForRandomPlayer();
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        if(!response.getParameters().equals(""))
            response.setParameters(g.toJsonTree(response.getParameters()).getAsJsonObject());
        int code = response.getCode();
        resetUI();
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
        waitForResponseUI();
        String serverResponse = playerManager.getPlayerAvailability(playerUserName);
        parsePlayerAvailabilityResponse(serverResponse);
    }

    private void parsePlayerAvailabilityResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        if(!response.getParameters().equals(""))
            response.setParameters(g.toJsonTree(response.getParameters()).getAsJsonObject());
        int code = response.getCode();
        resetUI();
        switch (code) {
            case ServerResponse.RESPONSE_SUCCESSFUL:
                String playerStatus = response.getMessage();
                if(playerStatus.equals("player_available")) {
                    JsonObject parametersObject = (JsonObject) response.getParameters();
                    int playerId = parametersObject.get("player_id").getAsInt();
                    System.out.println("Player available");
                    Platform.runLater(() -> {
                        invitationText.setText("Ο παίκτης είναι διαθέσιμος. Πάτησε SPACE για να συνεχίσεις.");
                        username.setDisable(true);
                    });
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_available.mp3", false);
                    promptToGoToLevelsPage(playerId);
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
        Platform.runLater(() ->invitationText.setText("Αυτός ο παίκτης σου έχει ήδη στείλει πρόσκληση. Περίμενε για να την ακούσεις."));
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_already_invited_by_opponent.mp3", false);
    }

    private void openRequestExistsFromRandomPlayer() {
        Platform.runLater(() ->invitationText.setText("Ένας παίκτης σου έχει ήδη στείλει πρόσκληση. Περίμενε για να την ακούσεις."));
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_already_invited.mp3", false);
    }

    private void playerNotFound() {
        Platform.runLater(() -> {
            invitationText.setText("Αυτός ο παίκτης δεν βρέθηκε. Πάτησε SPACE για να παίξεις με τον υπολογιστή.");
            username.setText("");
        });
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_not_found_prompt_cpu.mp3", false);
        promptToPlayWithCPU();
    }

    private void playerNotAvailable() {
        Platform.runLater(() ->invitationText.setText("Αυτός ο παίκτης δεν είναι διαθέσιμος. Πάτησε SPACE για να παίξεις με τυχαίο παίκτη."));
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_not_available.mp3", false);
        promptToPlayWithCPU();
    }

    private void noAvailablePlayerFound() {
        Platform.runLater(() ->invitationText.setText("Δεν βρέθηκε διαθέσιμος παίκτης. Πάτησε SPACE για να παίξεις με τον υπολογιστή."));
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "no_player_available.mp3", false);
        promptToPlayWithCPU();
    }

    private void promptToGoToLevelsPage(int opponentId) {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                audioEngine.pauseCurrentlyPlayingAudios();
                LevelsScreen levelsScreen = new LevelsScreen(sceneHandler, GameType.VS_PLAYER);
                levelsScreen.setOpponentId(opponentId);
                threadQueryForGameRequests.interrupt();
                threadSetPlayerOnline.interrupt();
                resetUI();
            }
        });
    }

    private void promptToPlayWithCPU() {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                threadQueryForGameRequests.interrupt();
                threadSetPlayerOnline.interrupt();
                audioEngine.pauseCurrentlyPlayingAudios();
                new LevelsScreen(sceneHandler, GameType.VS_CPU);
                resetUI();
            }
        });
    }

    private void queryServerForGameRequests() {
        ServerOperationResponse serverResponse;
        GameRequestManager gameRequestManager = new GameRequestManager();
        boolean shouldContinue = true;
        while (shouldContinue) {
            serverResponse = gameRequestManager.askServerForGameRequests();
            if(serverResponse != null) {
                shouldContinue = false;
                parseGameRequestJSONResponse(serverResponse);
                Thread answerThread = new Thread(() -> answerToGameRequest());
                answerThread.start();
            } else {
                try {
                    Thread.sleep(GameRequestManager.GAME_REQUESTS_CALL_INTERVAL);
                } catch (InterruptedException e) {
                    System.err.println("queryServerForGameRequests thread was interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void parseGameRequestJSONResponse(ServerOperationResponse serverResponse) {
        JsonObject parametersObject = (JsonObject) serverResponse.getParameters();
        int gameRequestId = parametersObject.get("game_request_id").getAsInt();
        int initiatorId = parametersObject.get("initiator_id").getAsInt();
        String initiatorUserName = parametersObject.get("initiator_user_name").getAsString();
        gameLevelId = parametersObject.get("game_level_id").getAsInt();
        candidateOpponent = new Player(initiatorUserName, initiatorId);
        PlayerManager.setOpponentPlayer(candidateOpponent);
        GameRequestManager.setGameRequestId(gameRequestId);
        Platform.runLater(() -> {
            username.setDisable(true);
            invitationText.setText("Έχεις πρόσκληση από τον παίκτη " + initiatorUserName + "! Πάτησε ENTER για να τη δεχθείς.");
        });
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "new_request.mp3", false);
    }

    private void answerToGameRequest() {
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                gameRequestManager.sendGameRequestAnswerToServer(true);
                threadQueryForShuffleCards = new Thread(() ->  queryForGameRequestShuffledCards());
                threadQueryForShuffleCards.start();
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "game_getting_ready.mp3", false);
                waitForResponseUI();
            } else if(event.getCode() == BACK_SPACE) {
                resetUI();
                Thread answerThread = new Thread(() ->  gameRequestManager.sendGameRequestAnswerToServer(false));
                answerThread.start();
                // re-check for requests
                queryServerForGameRequestsThread();
            }
        });
    }

    private void resetUI() {
        Platform.runLater(() -> {
            username.setDisable(false);
            invitationText.setText("");
            username.requestFocus();
        });
    }

    private void waitForResponseUI() {
        Platform.runLater(() -> {
            username.setDisable(true);
            invitationText.setText("Περίμενε...");
        });
    }

    private void setPlayerOnline() {
        PlayerManager playerManager = new PlayerManager();
        while(true) {
            System.out.println("setPlayerOnline from invite screen");
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

    private void queryForGameRequestShuffledCards() {
        ServerOperationResponse serverResponse;
        int timesCalled = 0;
        boolean shouldContinue = true;
        while (shouldContinue) {
            System.out.println("queryForGameRequestShuffledCards loop");
            timesCalled ++;
            try {
                serverResponse = gameRequestManager.askServerForGameRequestShuffledCards();
                if(serverResponse != null) {
                    // if we got response from the server, stop loop
                    shouldContinue = false;
                    ArrayList<LinkedTreeMap> jsonCardsArray = (ArrayList<LinkedTreeMap>) serverResponse.getParameters();
                    System.out.println("Got cards!");
                    parseShuffledCardsFromServerAndStartGame(jsonCardsArray);
                    break;
                } else {
                    try {
                        Thread.sleep(GameRequestManager.SHUFFLE_CARDS_INTERVAL);
                    } catch (InterruptedException e) {
                        System.err.println("queryForGameRequestShuffledCards was interrupted");
                        Thread.currentThread().interrupt();
                        shouldContinue = false;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                shouldContinue = false;
                opponentCanceledGame();
            }
            if(timesCalled > RequestManager.MAX_REQUEST_TRIES) {
                shouldContinue = false;
                opponentCanceledGame();
                break;
            }
        }
    }

    private void opponentCanceledGame() {
        audioEngine.playSound(this.miscellaneousSoundsBasePath + "player_cancelled_game.mp3", false);
        cancelGameRequest();
        threadQueryForShuffleCards.interrupt();
    }

    private void cancelGameRequest() {
        resetUI();
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
        resetUI();
        threadSetPlayerOnline.interrupt();
        Thread thread = new Thread(() -> gameLauncher.startGameForLevel(gameLevel, GameType.VS_PLAYER, cardsWithPositions));
        PlayerManager.setOpponentPlayer(candidateOpponent);
        thread.start();
    }

}
