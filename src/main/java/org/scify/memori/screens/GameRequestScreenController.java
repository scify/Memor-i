package org.scify.memori.screens;

import javafx.scene.Scene;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.interfaces.Player;

import java.util.ArrayList;

public class GameRequestScreenController {

    private Scene primaryScene;

    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    protected FXAudioEngine audioEngine = new FXAudioEngine();
    private ArrayList<Player> availablePlayers = new ArrayList<>();

    public void setParameters(FXSceneHandler sceneHandler, Scene userNameScreenScene) {
        this.primaryScene = userNameScreenScene;
        this.sceneHandler = sceneHandler;
        FXRenderingEngine.setGamecoverIcon(userNameScreenScene, "gameCoverImgContainer");

        sceneHandler.pushScene(userNameScreenScene);
    }

}
