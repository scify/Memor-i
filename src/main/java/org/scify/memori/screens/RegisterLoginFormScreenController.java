package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.json.JSONObject;
import org.scify.memori.PlayerManager;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.network.ServerOperationResponse;

import java.text.Normalizer;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;

public class RegisterLoginFormScreenController {

    private Scene primaryScene;
    @FXML
    TextField username;
    @FXML
    TextField password;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private PlayerManager playerManager;
    private String userNameStr;
    private String passwordStr;
    private boolean isRegister;

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene, boolean isRegister) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        this.playerManager = new PlayerManager();
        this.isRegister = isRegister;

        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        sceneHandler.pushScene(userNameScreenScene);
        password.setDisable(true);
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            sceneHandler.popScene();
        }
    }

    @FXML
    protected void submitUserName(KeyEvent evt) {
        if (evt.getCode() == ENTER) {
            String cleanString = Normalizer.normalize(username.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            System.out.println(cleanString);
            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                password.setDisable(false);
                password.requestFocus();
                username.setDisable(true);
                userNameStr = cleanString;
            } else {
                //TODO: play sound informing the player that only english characters are allowed
            }
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    @FXML
    protected void submitPassword(KeyEvent evt) {
        if (evt.getCode() == ENTER){
            String cleanString = Normalizer.normalize(password.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            passwordStr = cleanString;
            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                sendRequestToServer();
            } else {
                //TODO: play sound informing the player that only english characters are allowed
            }
        } else if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    private void sendRequestToServer() {
        String serverResponse;
        if(isRegister)
            serverResponse = playerManager.register(userNameStr, passwordStr);
        else
            serverResponse = playerManager.login(userNameStr, passwordStr);
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
                JSONObject responseObj = new JSONObject(serverResponse);
                JSONObject paramsObj = responseObj.getJSONObject("parameters");
                int newPlayerId = paramsObj.getInt("player_id");
                PlayerManager.setPlayerId(newPlayerId);
                new AvailablePlayersScreen(sceneHandler);
                break;
            case 2:
                // Player with username exists
                // TODO play appropriate sound
                System.out.println("Player with username " + userNameStr + " exists");
                break;
            case 3:
                // Validation error
                // TODO play appropriate sound
                System.out.println("Validation error: " + response.getParameters());
                break;
        }

    }

    private void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
