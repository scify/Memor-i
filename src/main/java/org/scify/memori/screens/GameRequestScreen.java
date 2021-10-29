package org.scify.memori.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.UTF8Control;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.scify.memori.MainOptions.mHeight;
import static org.scify.memori.MainOptions.mWidth;

public class GameRequestScreen {

    protected FXSceneHandler sceneHandler;

    public GameRequestScreen(FXSceneHandler shSceneHandler) {
        sceneHandler = shSceneHandler;
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_request.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene gameLevelsScene = new Scene(root, mWidth, mHeight);
        GameRequestScreenController controller = loader.getController();

        controller.setParameters(sceneHandler, gameLevelsScene);
    }
}
