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
    private boolean shouldQueryForRequests = true;

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        gameRequestManager = new GameRequestManager();
        playerManager = new PlayerManager();
        sceneHandler.pushScene(userNameScreenScene);
        text2Speech = new Text2Speech();
        gameLauncher = new MemoriGameLauncher(sceneHandler);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        queryServerForGameRequests();
                    }
                },
                5000
        );
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        markPlayerActive();
                    }
                },
                5000
        );
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
    }

    @FXML
    protected void submitUsername(KeyEvent evt) {
        if (evt.getCode() == ENTER){
            String cleanString = Normalizer.normalize(username.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                evt.consume();
                Thread thread = new Thread(() -> getPlayerAvailability(cleanString));
                thread.start();
            } else {
                //TODO: play sound informing the player that only english characters are allowed
            }
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
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
        System.out.println("game request reply response code: " + code);
        switch (code) {
            case 1:
                String playerStatus = response.getMessage();
                if(playerStatus.equals("player_available")) {
                    // TODO inform that player is available and prompt to go to levels page
                    JsonObject parametersObject = (JsonObject) response.getParameters();
                    int playerId = parametersObject.get("player_id").getAsInt();
                    System.out.println("Player available");
                    invitationText.setText("Player available. Press space to continue.");
                    promptToGoToLevelsPage(playerId);
                    username.setDisable(true);
                } else if(playerStatus.equals("player_not_available")) {
                    // TODO inform that player is NOT available
                    System.out.println("Player not available");
                    // TODO prompt user to press space and play with CPU
                    Platform.runLater(() -> promptToPlayWithCPUUI());
                    Thread thread = new Thread(() -> text2Speech.speak("Player not available. Press space to play with a random player."));
                    thread.start();
                    promptToPlayWithCPU();
                }
            case 2:
                // error
            case 3:
                // server validation not passed
            case 4:
                // player not found
        }
    }

    private void promptToGoToLevelsPage(int opponentId) {
        Thread thread = new Thread(() -> text2Speech.speak("Player available. Press space."));
        thread.start();
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                LevelsScreen levelsScreen = new LevelsScreen(sceneHandler, GameType.VS_PLAYER);
                levelsScreen.setOpponentId(opponentId);
                shouldQueryForRequests = false;
            }
        });
    }

    private void promptToPlayWithCPUUI() {
        invitationText.setText("Player not available. Press space to play with a random player.");
    }

    private void promptToPlayWithCPU() {
        primaryScene.setOnKeyReleased(event -> {
            if (event.getCode() == SPACE) {
                new LevelsScreen(sceneHandler, GameType.VS_CPU);
                shouldQueryForRequests = false;
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
                    System.out.println("You have a new request from " +initiatorUserName + "!");
                    invitationText.setText("You have a new request from " +initiatorUserName + "! Press Enter to Accept.");
                    username.setDisable(true);
                    candidateOpponent = new Player(initiatorUserName, initiatorId);
                    Thread thread = new Thread(() -> text2Speech.speak("You have a new request from " +initiatorUserName + "!"));
                    thread.start();
                    Thread answerThread = new Thread(() -> answerToGameRequest());
                    answerThread.start();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void answerToGameRequest() {
        // TODO tell player that in order to accept the request they click enter
        // or click back space to reject it
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                // TODO: accept game request
                System.out.println("game request accepted");
                gameRequestManager.sendGameRequestAnswerToServer(true);
                queryForGameRequestShuffledCards();
            } else if(event.getCode() == BACK_SPACE) {
                // TODO: reject game request
                System.out.println("game request rejected");
                Platform.runLater(() -> resetUI());
                // re-check for requests
                queryServerForGameRequests();
                gameRequestManager.sendGameRequestAnswerToServer(false);
            }
        });
    }

    private void resetUI() {
        username.setDisable(false);
        invitationText.setText("");
    }

    private void markPlayerActive() {
        markPlayerActiveExecutorService.scheduleAtFixedRate(
                new PlayerManager("PLAYER_ACTIVE"), 5, 5, TimeUnit.MINUTES);
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
        // TODO send request to server to cancel the game request
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

        Thread thread = new Thread(() -> gameLauncher.startGameForLevel(gameLevel, GameType.VS_PLAYER, cardsWithPositions));
        PlayerManager.setOpponentPlayer(candidateOpponent);
        thread.start();
    }

}
