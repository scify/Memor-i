
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

package org.scify.memori.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.HighScoresScreen;

import java.io.IOException;

import static org.scify.memori.MainOptions.mHeight;
import static org.scify.memori.MainOptions.mWidth;

/**
 * JavaFX Screen constructor page
 */
public class SponsorsScreen implements HighScoresScreen {

    protected FXSceneHandler sceneHandler;
    private FXAudioEngine audioEngine;
    private MemoriConfiguration configuration;
    protected String miscellaneousSoundsBasePath;
    
    public SponsorsScreen(FXSceneHandler shSceneHandler, Stage mainWindow) {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
        this.sceneHandler = shSceneHandler;
        audioEngine = new FXAudioEngine();
        sceneHandler.setMainWindow(mainWindow);
        MemoriConfiguration configuration = new MemoriConfiguration();
        String appLang = configuration.getProjectProperty("APP_LANG");
        FXMLLoader fxmlLoader = null;
        if(appLang.equals("el")) {
            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sponsors_el.fxml"));
        } else if (appLang.equals("no")) {
            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sponsors_no.fxml"));
        } else if (appLang.equals("en")) {
            fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sponsors_en.fxml"));
        }

        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "sponsors_message.mp3", false);
        Scene sponsorsScene = new Scene(root, mWidth, mHeight);
        sceneHandler.pushScene(sponsorsScene);

        sponsorsScene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    exitScreen();
                    break;
            }
        });
    }

    @Override
    public void initialize() {

    }

    protected void exitScreen() {
        audioEngine.pauseCurrentlyPlayingAudios();
        sceneHandler.popScene();
    }
}
