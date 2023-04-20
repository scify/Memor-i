
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
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.UTF8Control;
import org.scify.memori.interfaces.HighScoresScreen;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.scify.memori.MainOptions.mHeight;
import static org.scify.memori.MainOptions.mWidth;

/**
 * JavaFX Screen constructor page
 */
public class FXHighScoresScreen implements HighScoresScreen {

    protected FXSceneHandler sceneHandler;

    public FXHighScoresScreen(FXSceneHandler shSceneHandler, Stage mainWindow) {
        this.sceneHandler = shSceneHandler;
        sceneHandler.setMainWindow(mainWindow);
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scores.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }

        assert root != null;
        Scene scoresScene = new Scene(root, mWidth, mHeight);

        FXHighScoresScreenController controller = loader.getController();
        controller.setParameters(sceneHandler, scoresScene);
    }

    @Override
    public void initialize() {

    }
}
