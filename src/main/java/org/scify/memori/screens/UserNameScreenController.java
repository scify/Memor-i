package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.scify.memori.PlayerManager;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;

import java.text.Normalizer;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;

import org.scify.memori.network.ServerOperationResponse;

public class UserNameScreenController {

    protected Scene primaryScene;
    @FXML
    TextField username;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();
    private String userName;
    PlayerManager playerManager;

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        playerManager = new PlayerManager();
        sceneHandler.pushScene(userNameScreenScene);
    }


    @FXML
    protected void submitUserName(KeyEvent evt) {
        if (evt.getCode() == ENTER) {
            String cleanString = Normalizer.normalize(username.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            System.out.println(cleanString);
            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                //proceed
                userName = cleanString;
                String userId = playerManager.getPlayerIdFromUserName(userName);
                this.sendUserNameToServer(userName, userId);

            } else {
                //TODO: play sound informing the player that only english characters are allowed
            }
        } else if (evt.getCode() == ESCAPE) {
            sceneHandler.popScene();
        }
    }

    private void sendUserNameToServer(String userName, String userId) {
        String serverResponse = playerManager.sendUserNametoServer(userName, userId);
        if(serverResponse != null) {
            parseServerResponse(serverResponse);
        }
    }

    private void parseServerResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        switch (code) {
            case 1:
                // New player created
                // store player id with username in file
                Double newPlayerIdDouble = (Double) response.getParameters();
                int newPlayerId = newPlayerIdDouble.intValue();
                playerManager.storeNewPlayer(userName, newPlayerId);
                AvailablePlayers availablePlayersScreen = new AvailablePlayers(sceneHandler);
                break;
            case 2:
                // Player with username exists
                // TODO play appropriate sound
                System.out.println("Player with username " + userName + " exists");
                break;
        }

    }
}
