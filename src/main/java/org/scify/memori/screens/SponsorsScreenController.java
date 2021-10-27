package org.scify.memori.screens;

import javafx.scene.Scene;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;

public class SponsorsScreenController extends MemoriScreenController {

    public void setParameters(FXSceneHandler sceneHandler, Scene levelsScreenScene) {
        this.sceneHandler = sceneHandler;
        sceneHandler.pushScene(levelsScreenScene);
        FXRenderingEngine.setGamecoverIcon(levelsScreenScene, "gameCoverImgContainer");
    }
}
