package org.scify.memori.screens;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.DefaultExceptionHandler;

import java.util.Arrays;

import static javafx.scene.input.KeyCode.SPACE;

public class LanguageSelectionScreenController extends MemoriScreenController {

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
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "welcome_language_selection.mp3", false);
            }
        });
        primaryScene.lookup("#enLangBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "english.mp3", false);
            }
        });
        primaryScene.lookup("#elLangBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "greek.mp3", false);
            }
        });
        primaryScene.lookup("#esLangBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "spanish.mp3", false);
            }
        });
        primaryScene.lookup("#itLangBtn").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue && !memoriConfiguration.ttsEnabled()) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "italian.mp3", false);
            }
        });
        Platform.runLater(() -> logoImage.requestFocus());
    }

    @FXML
    protected void onSetLang(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                setLang(evt);
            }
        } else {
            setLang(evt);
        }
    }

    private void setLang(Event evt) {
        Node node = (Node) evt.getSource();
        String langCode = (String) node.getUserData();
        try {
            memoriConfiguration.setLang(langCode);
            if (Arrays.asList(new String[]{"keyboard", "mouse_touch"}).contains(System.getProperty("inputMethod")))
                new MainMenuScreen(sceneHandler, sceneHandler.getMainWindow());
            else
                new InputMethodSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
        } catch (Exception e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }
    }
}
