package org.scify.memori.screens;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.tts.TTSFacade;

import static javafx.scene.input.KeyCode.SPACE;

public class LanguageSelectionScreenController extends MemoriScreenController {

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene) {
        this.sceneHandler = sceneHandler;
        sceneHandler.pushScene(levelsScreenScene);
        FXRenderingEngine.setGamecoverIcon(levelsScreenScene, "gameCoverImgContainer");
        TTSFacade.speak("This is a test call to the text to speech service in english.");
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
        MemoriConfiguration.getInstance().setProperty("APP_LANG", langCode);
        MemoriConfiguration.getInstance().setProperty("DATA_PACKAGE_DEFAULT", "generic_pack_" + langCode);
        new InputMethodSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
    }
}
