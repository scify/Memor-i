package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXSceneHandler;

import static javafx.scene.input.KeyCode.ESCAPE;

public abstract class MemoriScreenController {
    protected FXSceneHandler sceneHandler;
    public abstract void setParameters(FXSceneHandler sceneHandler, Scene scoresScene);

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
