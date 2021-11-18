package org.scify.memori.screens;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;

import static javafx.scene.input.KeyCode.SPACE;

public class InputMethodSelectionScreenController extends MemoriScreenController {

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene) {
        this.sceneHandler = sceneHandler;
        sceneHandler.pushScene(levelsScreenScene);
        FXRenderingEngine.setGamecoverIcon(levelsScreenScene, "gameCoverImgContainer");
    }

    @FXML
    protected void onSetInputMethod(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                setInputMethod(evt);
            }
        } else {
            setInputMethod(evt);
        }
    }

    private void setInputMethod(Event evt) {
        Node node = (Node) evt.getSource();
        String inputMethod = (String) node.getUserData();
        MemoriConfiguration.getInstance().setProperty("INPUT_METHOD", inputMethod);
        MemoriConfiguration.getInstance().setProperty("TTS_ENABLED", String.valueOf(inputMethod.equals("mouse_touch")));
        new MainMenuScreen(sceneHandler, sceneHandler.getMainWindow());
    }
}
