package org.scify.memori.screens;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;

import static javafx.scene.input.KeyCode.SPACE;

public class InputMethodSelectionScreenController extends MemoriScreenController {

    public ImageView logoImage;

    @Override
    public void setParameters(FXSceneHandler sceneHandler, Scene scene) {
        super.setParameters(sceneHandler, scene);
        attachButtonFocusHandlers();
    }

    /**
     * Attaches focus handlers to buttons
     */
    protected void attachButtonFocusHandlers() {
        primaryScene.lookup("#logoImage").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "input_method_screen_welcome.mp3", false);
            }
        });
        primaryScene.lookup("#keyboardBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "keyboard.mp3", false);
            }
        });
        primaryScene.lookup("#mouseBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "mouse.mp3", false);
            }
        });
        primaryScene.lookup("#goBackBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "go_back.mp3", false);
            }
        });

        Platform.runLater(() -> logoImage.requestFocus());
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
        new MainMenuScreen(sceneHandler, sceneHandler.getMainWindow());
    }
}
