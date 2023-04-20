package org.scify.memori.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.UTF8Control;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.scify.memori.MainOptions.mHeight;
import static org.scify.memori.MainOptions.mWidth;

public class InvitePlayerScreen {

    protected FXSceneHandler sceneHandler;
    public static Scene scene;

    public InvitePlayerScreen(FXSceneHandler shSceneHandler) {
        sceneHandler = shSceneHandler;
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/invite_player.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }

        Scene gameLevelsScene = new Scene(root, mWidth, mHeight);
        InvitePlayerScreenController controller = loader.getController();
        scene = gameLevelsScene;
        controller.setParameters(sceneHandler, gameLevelsScene);
    }
}
