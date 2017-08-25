package org.scify.memori.screens;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.Text2Speech;
import org.scify.memori.interfaces.Player;

import java.util.ArrayList;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class AvailablePlayersController {

    protected Scene primaryScene;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();
    private ArrayList<Player> availablePlayers = new ArrayList<>();
    Text2Speech text2Speech;

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");

        sceneHandler.pushScene(userNameScreenScene);
        text2Speech = new Text2Speech();

        availablePlayers.add(new Player("player_1"));
        availablePlayers.add(new Player("fighterrr&2"));
        availablePlayers.add(new Player("bAtman3"));
        availablePlayers.add(new Player("pao13"));
        availablePlayers.add(new Player("SciFY"));

        VBox gameLevelsContainer = (VBox) userNameScreenScene.lookup("#playersDiv");
        addPlayersButtons(gameLevelsContainer);
    }

    @FXML
    private void exitIfEsc(KeyEvent evt) {
        if(evt.getCode() == ESCAPE) {
            sceneHandler.popScene();
        }
    }

    private void addPlayersButtons(VBox buttonsContainer) {
        for (Player currPlayer : availablePlayers) {
            Button playerBtn = new Button();
            playerBtn.setText(currPlayer.getName());
            playerBtn.getStyleClass().add("optionButton");
            playerBtn.setId(String.valueOf(currPlayer.getName()));
            playerBtn.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    Thread thread = new Thread(() -> text2Speech.speak(currPlayer.getName()));
                    thread.start();
                }
            });
            buttonsContainer.getChildren().add(playerBtn);
            playerBtnHandler(playerBtn, currPlayer);
        }
    }

    protected void playerBtnHandler(Button playerBtn, Player currPlayer) {
        playerBtn.setOnKeyPressed(event -> {
            if (event.getCode() == SPACE) {
                System.out.println(playerBtn.getId());
            } else if (event.getCode() == ESCAPE) {
                sceneHandler.popScene();
            }
        });
    }
}
