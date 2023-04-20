package org.scify.memori.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.UTF8Control;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.scify.memori.MainOptions.mHeight;
import static org.scify.memori.MainOptions.mWidth;

public class LevelsScreen {

    protected FXSceneHandler sceneHandler;
    LevelsScreenController controller;
    public static Scene scene;

    public LevelsScreen(FXSceneHandler shSceneHandler, GameType gameType) {
        sceneHandler = shSceneHandler;
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/levels_screen.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }

        scene = new Scene(root, mWidth, mHeight);
        controller = loader.getController();

        controller.setParameters(sceneHandler, scene, gameType);
    }

    public void setOpponentId(int opponentId) {
        this.controller.setOpponentId(opponentId);
    }
}
