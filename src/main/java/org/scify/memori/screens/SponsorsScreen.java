
/**
 * Copyright 2016 SciFY.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scify.memori.screens;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXSceneHandler;

/**
 * JavaFX Screen constructor page
 */
public class SponsorsScreen extends MemoriScreen {

    public SponsorsScreen(FXSceneHandler sceneHandler, Stage mainWindow) {
        super(sceneHandler, mainWindow, "sponsors");

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                exitScreen();
            }
        });
    }

    private void exitScreen() {
        FXAudioEngine.getInstance().pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
