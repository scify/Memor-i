package org.scify.memori.screens;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import org.scify.memori.helper.MemoriConfiguration;

import java.util.Arrays;

import static javafx.scene.input.KeyCode.SPACE;

public class LanguageSelectionScreenController extends MemoriScreenController {

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
            MemoriConfiguration.getInstance().setLang(langCode);
            if (Arrays.asList(new String[]{"keyboard", "mouse_touch"}).contains(System.getProperty("inputMethod")))
                new MainMenuScreen(sceneHandler, sceneHandler.getMainWindow());
            else
                new InputMethodSelectionScreen(sceneHandler, sceneHandler.getMainWindow());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
