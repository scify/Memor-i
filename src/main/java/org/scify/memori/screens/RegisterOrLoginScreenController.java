package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class RegisterOrLoginScreenController {

    private Scene primaryScene;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");

        sceneHandler.pushScene(userNameScreenScene);
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            sceneHandler.popScene();
        }
    }

    @FXML
    protected void goToRegisterScreen(KeyEvent evt) {
        if (evt.getCode() == ESCAPE) {
            exitScreen();
        } else if(evt.getCode() == SPACE) {
            new RegisterLoginFormScreen(sceneHandler, true);
        }
    }

    @FXML
    protected void goToLoginScreen(KeyEvent evt) {
        if (evt.getCode() == ESCAPE) {
            exitScreen();
        } else if(evt.getCode() == SPACE) {
            new RegisterLoginFormScreen(sceneHandler, false);
        }
    }

    private void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
