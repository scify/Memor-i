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

public class RegisterScreen {

    protected FXSceneHandler sceneHandler;

    public RegisterScreen(FXSceneHandler shSceneHandler) {
        sceneHandler = shSceneHandler;
        MemoriConfiguration configuration = new MemoriConfiguration();
        Locale locale = new Locale(configuration.getProjectProperty("APP_LANG"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"),
                ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene gameLevelsScene = new Scene(root, mWidth, mHeight);
        RegisterScreenController controller = loader.getController();

        controller.setParameters(sceneHandler, gameLevelsScene);
    }
}
