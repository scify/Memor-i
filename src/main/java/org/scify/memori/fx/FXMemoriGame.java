
/**
 * Copyright 2016 SciFY.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scify.memori.fx;


import org.scify.memori.MemoriGame;
import org.scify.memori.MemoriGameLevel;
import org.scify.memori.card.CategorizedCard;
import org.scify.memori.interfaces.GameLevel;

import java.awt.geom.Point2D;
import java.util.Map;

public class FXMemoriGame extends MemoriGame {
    protected FXSceneHandler sceneHandler;
    protected MemoriGameLevel gameLevel;

    public FXMemoriGame(FXSceneHandler shSceneHandler, MemoriGameLevel gameLevel) {
        this.sceneHandler = shSceneHandler;
        this.gameLevel = gameLevel;
    }

    @Override
    public void initialize(GameLevel gameLevel) {
        super.initialize(gameLevel);
        setUpRenderingEngine();
    }

    public void initialize(Map<CategorizedCard, Point2D> cards, GameLevel gameLevel) {
        super.initialize(cards, gameLevel);
        setUpRenderingEngine();
    }

    private void setUpRenderingEngine() {
        FXRenderingEngine fUI = new FXRenderingEngine(gameLevel);
        uInterface = fUI;
        reRenderer = fUI;
        // Plus update current scene
        sceneHandler.pushScene(fUI.gameScene);
    }


}
