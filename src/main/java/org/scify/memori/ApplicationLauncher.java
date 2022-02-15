package org.scify.memori;

import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.screens.MainScreen;

/**
 * This class invokes the main JavaFX application
 */
public class ApplicationLauncher {

    public static void main(String[]args) {
        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler.getInstance());
        MainScreen.launch(MainScreen.class);
    }
}
