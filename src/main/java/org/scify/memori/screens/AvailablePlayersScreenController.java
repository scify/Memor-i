package org.scify.memori.screens;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.scify.memori.*;
import org.scify.memori.card.Card;
import org.scify.memori.card.CategorizedCard;
import org.scify.memori.card.MemoriCardService;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.Text2Speech;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.ServerOperationResponse;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static javafx.scene.input.KeyCode.*;

public class AvailablePlayersScreenController {

    private Scene primaryScene;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();
    private ArrayList<Player> availablePlayers = new ArrayList<>();
    private Text2Speech text2Speech;
    private GameRequestManager gameRequestManager;
    MemoriGameLauncher gameLauncher;

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        gameRequestManager = new GameRequestManager();
        sceneHandler.pushScene(userNameScreenScene);
        text2Speech = new Text2Speech();
        getOnlinePlayersFromServer();
        gameLauncher = new MemoriGameLauncher(sceneHandler);
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            sceneHandler.popScene();
        }
    }

    private void getOnlinePlayersFromServer() {
        PlayerManager playerManager = new PlayerManager();
        String serverResponse = playerManager.getOnlinePlayersFromServer();
        if(serverResponse != null) {
            parseServerResponse(serverResponse);
        }
    }

    private void parseServerResponse(String serverResponse) {
        JSONArray jsonarray = new JSONArray(serverResponse);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            String name = jsonobject.getString("user_name");
            int id = jsonobject.getInt("id");
            Player player = new Player(name, id);
            availablePlayers.add(player);
        }
        VBox gameLevelsContainer = (VBox) this.primaryScene.lookup("#playersDiv");
        if(availablePlayers.isEmpty()) {
            // TODO play appropriate sound
            //addPlayerVsCPUBtn(gameLevelsContainer);
        } else {
            // TODO play sound to inform the user that they can
            // either wait for a game request or select a player to send them a request
            addPlayersButtons(gameLevelsContainer);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            queryServerForGameRequests();
                        }
                    },
                    5000
            );
        }

    }

//    private void addPlayerVsCPUBtn(VBox buttonsContainer) {
//        Button verusCPUBtn = new Button();
//        verusCPUBtn.setText("Versus Computer");
//        verusCPUBtn.getStyleClass().add("optionButton");
//        buttonsContainer.getChildren().add(verusCPUBtn);
//        playVersusCPUBtnHandler(verusCPUBtn);
//        verusCPUBtn.requestFocus();
//    }

    private void addPlayersButtons(VBox buttonsContainer) {
        for (Player currPlayer : availablePlayers) {
            Button playerBtn = new Button();
            playerBtn.setText(currPlayer.getName());
            playerBtn.getStyleClass().add("optionButton");
            playerBtn.setId(String.valueOf(currPlayer.getId()));
            playerBtn.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    Thread thread = new Thread(() -> text2Speech.speak(currPlayer.getName()));
                    thread.start();
                }
            });
            buttonsContainer.getChildren().add(playerBtn);
            playerBtnHandler(playerBtn, currPlayer);
        }
    }

//    private void playVersusCPUBtnHandler(Button button) {
//        button.setOnKeyPressed(event -> {
//            if (event.getCode() == SPACE) {
//                MainOptions.GAME_TYPE = 2;
//                new LevelsScreen(this.sceneHandler);
//            } else if (event.getCode() == ESCAPE) {
//                sceneHandler.popScene();
//            }
//        });
//    }

    private void playerBtnHandler(Button playerBtn, Player currPlayer) {
        playerBtn.setOnKeyPressed(event -> {
            if (event.getCode() == SPACE) {
                System.out.println("opponent id: " + playerBtn.getId());
                LevelsScreen levelsScreen = new LevelsScreen(this.sceneHandler);
                levelsScreen.setOpponentId(currPlayer.getId());
            } else if (event.getCode() == ESCAPE) {
                sceneHandler.popScene();
            }
        });
    }

    private void queryServerForGameRequests() {
        ServerOperationResponse serverResponse = null;
        int timesCalled = 0;
        while (serverResponse == null) {
            timesCalled ++;
            ScheduledExecutorService scheduler = Executors
                    .newScheduledThreadPool(1);
            ScheduledFuture<ServerOperationResponse> future = scheduler.schedule(
                    new GameRequestManager("GET_REQUESTS"), 5, TimeUnit.SECONDS);
            try {
                serverResponse = future.get();
                if(serverResponse != null) {
                    JsonObject parametersObject = (JsonObject) serverResponse.getParameters();
                    int gameRequestId = parametersObject.get("game_request_id").getAsInt();
                    GameRequestManager.setGameRequestId(gameRequestId);
                    String initiatorUserName = parametersObject.get("initiator_user_name").getAsString();
                    System.out.println("You have a new request from " +initiatorUserName + "!");
                    Thread thread = new Thread(() -> text2Speech.speak("You have a new request from " +initiatorUserName + "!"));
                    thread.start();
                    answerToGameRequest(initiatorUserName);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void answerToGameRequest(String initiatorUserName) {
        // TODO tell player that in order to accept the request they click enter
        // or click back space to reject it
        primaryScene.setOnKeyReleased(event -> {
            if(event.getCode() == ENTER) {
                // TODO: accept game request
                System.out.println("game request accepted");
                gameRequestManager.sendGameRequestAnswerToServer(true);
                queryForGameRequestShuffledCards(initiatorUserName);
            } else if(event.getCode() == BACK_SPACE) {
                // TODO: reject game request
                System.out.println("game request rejected");
                gameRequestManager.sendGameRequestAnswerToServer(false);
            }
        });
    }

    private void queryForGameRequestShuffledCards(String initiatorUserName) {
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
                    parseShuffledCardsFromServerAndStartGame(jsonCardsArray, initiatorUserName);
                    // TODO inform player that the cards are ready and should press enter
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseShuffledCardsFromServerAndStartGame(ArrayList<LinkedTreeMap> jsonCardsArray, String initiatorUserName) {
        Map<CategorizedCard, Point2D> cardsWithPositions = new HashMap<>();
        MemoriCardService memoriCardService = new MemoriCardService();
        for(LinkedTreeMap cardJsonObj: jsonCardsArray) {
            CategorizedCard nextCard = memoriCardService.getCardFromLabelAndType(cardJsonObj.get("label").toString(), cardJsonObj.get("category").toString());
            cardsWithPositions.put(nextCard, new Point2D.Double(Double.parseDouble(cardJsonObj.get("xPos").toString()), Double.parseDouble(cardJsonObj.get("yPos").toString())));
        }
        System.out.println(cardsWithPositions.size());
        List<MemoriGameLevel> gameLevels = new ArrayList<>();
        GameLevelService gameLevelService = new GameLevelService();
        gameLevels = gameLevelService.createGameLevels();
        // TODO change
        MemoriGameLevel gameLevel = gameLevels.get(0);
        MainOptions.GAME_LEVEL_CURRENT = gameLevel.getLevelCode();
        MainOptions.NUMBER_OF_ROWS = (int) gameLevel.getDimensions().getX();
        MainOptions.NUMBER_OF_COLUMNS = (int) gameLevel.getDimensions().getY();
        Thread thread = new Thread(() -> gameLauncher.startNormalGameWithCards(gameLevel, cardsWithPositions));
        Player opponent = new Player(initiatorUserName);
        PlayerManager.setOpponentPlayer(opponent);
        thread.start();
    }

}
