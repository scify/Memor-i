package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scify.memori.MainOptions;
import org.scify.memori.PlayerManager;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.Text2Speech;
import org.scify.memori.interfaces.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;
import static org.apache.http.protocol.HTTP.USER_AGENT;

public class AvailablePlayersController {

    protected Scene primaryScene;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();
    private ArrayList<Player> availablePlayers = new ArrayList<>();
    Text2Speech text2Speech;

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");

        sceneHandler.pushScene(userNameScreenScene);
        text2Speech = new Text2Speech();
        getOnlinePlayersFromServer();
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
            addPlayerVsCPUBtn(gameLevelsContainer);
        } else {
            addPlayersButtons(gameLevelsContainer);
        }

    }

    private void addPlayerVsCPUBtn(VBox buttonsContainer) {
        Button verusCPUBtn = new Button();
        verusCPUBtn.setText("Versus Computer");
        verusCPUBtn.getStyleClass().add("optionButton");
        buttonsContainer.getChildren().add(verusCPUBtn);
        playVersusCPUBtnHandler(verusCPUBtn);
        verusCPUBtn.requestFocus();
    }

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

    private void playVersusCPUBtnHandler(Button button) {
        button.setOnKeyPressed(event -> {
            if (event.getCode() == SPACE) {
                MainOptions.GAME_TYPE = 2;
                new LevelsScreen(this.sceneHandler);
            } else if (event.getCode() == ESCAPE) {
                sceneHandler.popScene();
            }
        });
    }

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
}
