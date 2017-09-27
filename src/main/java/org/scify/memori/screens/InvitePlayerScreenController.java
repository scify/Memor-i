package org.scify.memori.screens;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.application.Platform;
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
    private Text2Speech text2Speech;
    private GameRequestManager gameRequestManager;
    private PlayerManager playerManager;
    private MemoriGameLauncher gameLauncher;
    private Player candidateOpponent;
    private ScheduledExecutorService gameRequestsExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService markPlayerActiveExecutorService = Executors.newScheduledThreadPool(1);
    private int gameLevelId;
    private ScheduledFuture<ServerOperationResponse> gameRequestsFuture;
    private ScheduledFuture<String> playerActiveFuture;
    private static boolean shouldQueryForRequests = true;
    private static boolean shouldQueryForMarkingPlayerActive = true;
    private String miscellaneousSoundsBasePath;
    private String multiPlayerSoundsBasePath;
    private MemoriConfiguration configuration;

    public InvitePlayerScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
        this.multiPlayerSoundsBasePath = configuration.getProjectProperty("MULTIPLAYER_SOUNDS_BASE_PATH");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        gameRequestManager = new GameRequestManager();
        playerManager = new PlayerManager();
        sceneHandler.pushScene(userNameScreenScene);
        text2Speech = new Text2Speech();
        gameLauncher = new MemoriGameLauncher(sceneHandler);
        shouldQueryForRequests = true;
        shouldQueryForMarkingPlayerActive = true;
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        queryServerForGameRequests();
                    }
                },
                GameRequestManager.GAME_REQUESTS_CALL_INTERVAL
        );
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        markPlayerActive();
                    }
                },
                PlayerManager.MARK_PLAYER_ACTIVE_CALL_INTERVAL
        );

        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "invite_player_screen_welcome.mp3", false);
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
                        // TODO play appropriate sound
                        Platform.runLater(() -> resetUI());
                        Platform.runLater(() -> username.setText(""));
                    } else {
                        thread = new Thread(() -> getPlayerAvailability(finalCleanString));
                        thread.start();
                    }
                } else {
                    thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_input.mp3", false));
                    thread.start();
                    Platform.runLater(() -> username.setText(""));
                }
            }
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }

    }

    private void searchForRandomPlayer() {
        String serverResponse = playerManager.searchForRandomPlayer();
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        if(!response.getParameters().equals(""))
            response.setParameters(g.toJsonTree(response.getParameters()).getAsJsonObject());
        int code = response.getCode();
        switch (code) {
            case 1:
                // found a player
                JsonObject parametersObject = (JsonObject) response.getParameters();
                int playerId = parametersObject.get("player_id").getAsInt();
                Thread thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "random_player_found.mp3", false));
                thread.start();
                promptToGoToLevelsPage(playerId);
                break;
            case 2:
                // error
                break;
            case 3:
                // server validation not passed
                break;
            case 4:
                // player not found
                noAvailablePlayerFound();
                break;
            case 5:
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
            case 1:
                String playerStatus = response.getMessage();
                if(playerStatus.equals("player_available")) {
                    JsonObject parametersObject = (JsonObject) response.getParameters();
                    int playerId = parametersObject.get("player_id").getAsInt();
                    System.out.println("Player available");
                    invitationText.setText("Ο παίκτης είναι διαθέσιμος. Πάτησε SPACE για να συνεχίσεις.");
                    Thread thread = new Thread(() -> audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_available.mp3", false));
                    thread.start();
                    promptToGoToLevelsPage(playerId);
                    username.setDisable(true);
                } else if(playerStatus.equals("player_not_available")) {
                    System.out.println("Player not available");
                    playerNotAvailable();
                }
                break;
            case 2:
                // error
            case 3:
                // server validation not passed
            case 4:
                // player not found
                playerNotFound();
                break;
            case 5:
                // opponent player has already sent a request
                openRequestExistsFromOpponent();
                break;
        }
    }

    private void openRequestExistsFromOpponent() {
        Platform.runLater(() -> {
            invitationText.setText("Αυτός ο παίκτης σου έχει ήδη στείλει πρόσκληση. Περίμενε για να την ακούσεις.");
        });
        // TODO add appropriate sound
    }

    private void openRequestExistsFromRandomPlayer() {
        Platform.runLater(() -> {
            invitationText.setText("Ένας παίκτης σου έχει ήδη στείλει πρόσκληση. Περίμενε για να την ακούσεις.");
        });
        // TODO add appropriate sound
    }

    private void playerNotFound() {
        Platform.runLater(() -> {
            invitationText.setText("Αυτός ο παίκτης δεν βρέθηκε. Πάτησε SPACE για να παίξεις με τον υπολογιστή.");
            username.setText("");
        });
        Thread thread = new Thread(() -> {
            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_not_found.mp3", true);
            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "press_space_to_play_with_cpu.mp3", true);
        });
        thread.start();
        promptToPlayWithCPU();
    }

    private void playerNotAvailable() {
        Platform.runLater(() -> invitationText.setText("Αυτός ο παίκτης δεν είναι διαθέσιμος. Πάτησε SPACE για να παίξεις με τυχαίο παίκτη."));
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "player_not_available.mp3", false);
        promptToPlayWithCPU();
    }

    private void noAvailablePlayerFound() {
        Platform.runLater(() -> invitationText.setText("Δεν βρέθηκε διαθέσιμος παίκτης. Πάτησε SPACE για να παίξεις με τον υπολογιστή."));
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
                shouldQueryForMarkingPlayerActive = false;
                Platform.runLater(() -> resetUI());
            }
        });
    }

    private void promptToPlayWithCPU() {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                shouldQueryForRequests = false;
                shouldQueryForMarkingPlayerActive = false;
                new LevelsScreen(sceneHandler, GameType.VS_CPU);
            }
        });
    }

    private void queryServerForGameRequests() {
        ServerOperationResponse serverResponse = null;
        int timesCalled = 0;
        while (serverResponse == null && shouldQueryForRequests) {
            timesCalled ++;

            gameRequestsFuture = gameRequestsExecutorService.schedule(
                    new GameRequestManager("GET_REQUESTS"), 5, TimeUnit.SECONDS);
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
                    username.setDisable(true);
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
                Platform.runLater(() -> waitForResponseUI());
            } else if(event.getCode() == BACK_SPACE) {
                Platform.runLater(() -> resetUI());
                Thread answerThread = new Thread(() ->  gameRequestManager.sendGameRequestAnswerToServer(false));
                answerThread.start();
                // re-check for requests after 10 seconds
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                queryServerForGameRequests();
                            }
                        },
                        10000
                );
            }
        });
    }

    public static void screenPoppedUI() {
        // TODO play sound to inform player that they are back to this screen
        FXAudioEngine audioEngine = new FXAudioEngine();
        audioEngine.pauseCurrentlyPlayingAudios();
    }

    private void resetUI() {
        username.setDisable(false);
        invitationText.setText("");
    }

    private void waitForResponseUI() {
        username.setDisable(true);
        invitationText.setText("Περίμενε...");
    }

    private void markPlayerActive() {
        while(shouldQueryForMarkingPlayerActive) {
            playerActiveFuture = markPlayerActiveExecutorService.schedule(
                    new PlayerManager("PLAYER_ACTIVE"), 10, TimeUnit.SECONDS);
            try {
                String response = playerActiveFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

    }

    private void queryForGameRequestShuffledCards() {
        ServerOperationResponse serverResponse = null;
        int timesCalled = 0;
        while (serverResponse == null) {
            timesCalled ++;

            ScheduledExecutorService scheduler = Executors
                    .newScheduledThreadPool(1);
            ScheduledFuture<ServerOperationResponse> future = scheduler.schedule(
                    new GameRequestManager("GET_SHUFFLED_CARDS"), 3, TimeUnit.SECONDS);
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
        // TODO inform player that something went wrong
        Platform.runLater(() -> resetUI());
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
        System.out.println(cardsWithPositions.size());
        Platform.runLater(() -> resetUI());
        Thread thread = new Thread(() -> gameLauncher.startGameForLevel(gameLevel, GameType.VS_PLAYER, cardsWithPositions));
        PlayerManager.setOpponentPlayer(candidateOpponent);
        thread.start();
    }

}
