package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.json.JSONObject;
import org.scify.memori.PlayerManager;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
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
    private String miscellaneousSoundsBasePath;
    private MemoriConfiguration configuration;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private PlayerManager playerManager;
    private String userNameStr;
    private String passwordStr;
    private boolean isRegister;

    public RegisterLoginFormScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene, boolean isRegister) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        this.playerManager = new PlayerManager();
        this.isRegister = isRegister;

        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        sceneHandler.pushScene(userNameScreenScene);
        password.setDisable(true);
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "username.mp3", false);
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
            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                password.setDisable(false);
                password.requestFocus();
                username.setDisable(true);
                userNameStr = cleanString;
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "password.mp3", false);
            } else {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_input.mp3", false);
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
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_input.mp3", false);
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
                audioEngine.pauseCurrentlyPlayingAudios();
                new InvitePlayerScreen(sceneHandler);
                break;
            case 2:
                // Player with username exists
                // TODO play sound
                System.out.println("Player with username " + userNameStr + " exists");
                Platform.runLater(() -> resetUI());
                break;
            case 3:
                // Validation error
                System.out.println("Validation error: " + response.getParameters());
                Platform.runLater(() -> resetUI());
                break;
            case 4:
                // Wrong login credentials
                Platform.runLater(() -> resetUI());
                Thread thread = new Thread(() -> {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_credentials.mp3", true);
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "username.mp3", true);
                });
                thread.start();
                break;
        }

    }

    private void resetUI() {
        username.setText("");
        password.setText("");
        password.setDisable(true);
        username.setDisable(false);
        username.requestFocus();
    }

    private void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
