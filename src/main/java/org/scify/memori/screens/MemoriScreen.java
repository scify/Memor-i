package org.scify.memori.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.UTF8Control;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.scify.memori.MainOptions.mHeight;
import static org.scify.memori.MainOptions.mWidth;

public class MemoriScreen {
    protected FXSceneHandler sceneHandler;
    protected Scene scene;

    public MemoriScreen(FXSceneHandler sceneHandler, Stage mainWindow, String fxmlFileName) {
        sceneHandler.setMainWindow(mainWindow);
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFileName + ".fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert root != null;
        MemoriScreenController controller = loader.getController();
        scene = new Scene(root, mWidth, mHeight);
        controller.setParameters(sceneHandler, scene);
        FXAudioEngine.getInstance().pauseCurrentlyPlayingAudios();
        this.sceneHandler = sceneHandler;
    }
}
