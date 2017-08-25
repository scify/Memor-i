package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;

import java.text.Normalizer;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class UserNameScreenController {

    protected Scene primaryScene;
    @FXML
    TextField username;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");

        sceneHandler.pushScene(userNameScreenScene);
    }


    @FXML
    protected void submitUserName(KeyEvent evt) {
        if (evt.getCode() == ENTER) {
            String cleanString = Normalizer.normalize(username.getText(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            System.out.println(cleanString);
            boolean valid = cleanString.matches("\\w+");
            if(valid) {
                //proceed
                //go to available players screen
                AvailablePlayers availablePlayersScreen = new AvailablePlayers(sceneHandler);
            } else {
                //TODO: play sound informing the player that only english characters are allowed
            }
        } else if(evt.getCode() == ESCAPE) {
            sceneHandler.popScene();
        }
    }
}
