package org.scify.memori.screens;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.json.JSONObject;
import org.scify.memori.PlayerManager;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.ServerOperationResponse;
import org.scify.memori.network.ServerResponse;

import java.text.Normalizer;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;

public class RegisterLoginFormScreenController {

    @FXML
    TextField username;
    @FXML
    TextField password;
    @FXML
    Label infoText;
    private String miscellaneousSoundsBasePath;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private FXAudioEngine audioEngine = new FXAudioEngine();
    private PlayerManager playerManager;
    private String userNameStr;
    private String passwordStr;
    private boolean isRegister;

    public RegisterLoginFormScreenController() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene, boolean isRegister) {
        this.sceneHandler = sceneHandler;
        this.playerManager = new PlayerManager();
        this.isRegister = isRegister;

        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");
        sceneHandler.pushScene(userNameScreenScene);

        resetUIThread();
        if(isRegister)
            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "register_username.mp3", false);
        else
            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "login_username.mp3", false);
    }

    private void resetUIThread() {
        Platform.runLater(() -> resetUI());
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            exitScreen();
            evt.consume();
        }
    }

    @FXML
    protected void submitUserName(KeyEvent evt) {
        if (evt.getCode() == ENTER) {
            String cleanString = Normalizer.normalize(username.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                promptForPasswordUI();
                userNameStr = cleanString;
                if(isRegister)
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "register_password.mp3", false);
                else
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "login_password.mp3", false);
            } else {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_input.mp3", false);
                Platform.runLater(() -> username.setText(""));
            }
        }
    }

    private void promptForPasswordUI() {
        Platform.runLater(() -> {
            password.setDisable(false);
            password.requestFocus();
            username.setDisable(true);
        });
    }

    @FXML
    protected void submitPassword(KeyEvent evt) {
        if (evt.getCode() == ENTER){
            String cleanString = Normalizer.normalize(password.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            passwordStr = cleanString;
            boolean valid = cleanString.matches("\\w+");
            if (valid) {
                Thread thread = new Thread(() -> sendRequestToServer());
                thread.start();
            } else {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_input.mp3", false);
                Platform.runLater(() -> password.setText(""));
            }
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
            case ServerResponse.RESPONSE_SUCCESSFUL:
                // New player created or player is logged in
                JSONObject responseObj = new JSONObject(serverResponse);
                JSONObject paramsObj = responseObj.getJSONObject("parameters");
                int newPlayerId = paramsObj.getInt("player_id");
                PlayerManager.setPlayerId(newPlayerId);
                PlayerManager.setLocalPlayer(new Player(userNameStr, newPlayerId));
                audioEngine.pauseCurrentlyPlayingAudios();
                InvitePlayerScreen invitePlayerScreen = new InvitePlayerScreen(sceneHandler);
                break;
            case ServerResponse.RESPONSE_ERROR:
                // Player with username exists
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "username_taken.mp3", false);
                System.out.println("Player with username " + userNameStr + " exists");
                resetUIThread();
                break;
            case ServerResponse.VALIDATION_ERROR:
                // Validation error
                System.out.println("Validation error: " + response.getParameters());
                resetUIThread();
                break;
            case ServerResponse.RESPONSE_EMPTY:
                // Wrong login credentials
                resetUIThread();
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "wrong_credentials_prompt_username.mp3", false);
                break;
            default:
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
