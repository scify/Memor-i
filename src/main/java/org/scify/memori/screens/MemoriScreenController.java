package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.AudioEngine;

import static javafx.scene.input.KeyCode.ESCAPE;

public abstract class MemoriScreenController {
    protected FXSceneHandler sceneHandler;
    protected MemoriConfiguration memoriConfiguration;
    protected Scene primaryScene;
    protected AudioEngine audioEngine;
    protected String miscellaneousSoundsBasePath;

    public void setParameters(FXSceneHandler sceneHandler, Scene scene) {
        this.sceneHandler = sceneHandler;
        this.memoriConfiguration = MemoriConfiguration.getInstance();
        sceneHandler.pushScene(scene);
        FXRenderingEngine.setGamecoverIcon(scene, "gameCoverImgContainer");
        this.primaryScene = scene;
        this.audioEngine = FXAudioEngine.getInstance();
        this.miscellaneousSoundsBasePath = memoriConfiguration.getDataPackProperty("MISCELLANEOUS_SOUNDS");
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if (evt.getCode() == ESCAPE) {
            exitScreen();
        }
    }

    /**
     * Pauses all sounds and exits the application
     */
    @FXML
    protected void exitScreen() {
        FXAudioEngine.getInstance().pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
