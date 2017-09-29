package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class RegisterOrLoginScreenController {

    private Scene primaryScene;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();
    private String miscellaneousSoundsBasePath;
    private MemoriConfiguration configuration;

    public RegisterOrLoginScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene scene) {
        this.primaryScene = scene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(scene, "gameCoverImgContainer");

        sceneHandler.pushScene(scene);

        attachButtonFocusHandlers();
    }

    /**
     * Attaches focus handlers to fixed buttons (tutorial, exit, etc)
     */
    private void attachButtonFocusHandlers() {

        primaryScene.lookup("#welcome").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "register_or_login.mp3", false);
            }
        });

        primaryScene.lookup("#register").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "register.mp3", false);
            }
        });

        primaryScene.lookup("#login").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "login.mp3", false);
            }
        });
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            exitScreen();
            evt.consume();
        }
    }

    @FXML
    protected void goToRegisterScreen(KeyEvent evt) {
        if(evt.getCode() == SPACE) {
            audioEngine.pauseCurrentlyPlayingAudios();
            new RegisterLoginFormScreen(sceneHandler, true);
        }
    }

    @FXML
    protected void goToLoginScreen(KeyEvent evt) {
        if(evt.getCode() == SPACE) {
            audioEngine.pauseCurrentlyPlayingAudios();
            new RegisterLoginFormScreen(sceneHandler, false);
        }
    }

    private void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
