
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

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.scify.memori.MemoriGameLauncher;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.Utils;
import org.scify.memori.interfaces.AudioEngine;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.SPACE;

public class GameTypeSelectionScreenController extends MemoriScreenController implements Initializable {

    public VBox btnContainer;
    public Button versus_computer;
    protected final MemoriConfiguration configuration;
    protected final String miscellaneousSoundsBasePath;
    protected Scene primaryScene;
    protected final AudioEngine audioEngine = FXAudioEngine.getInstance();
    @FXML
    Button tutorialBtn;

    public GameTypeSelectionScreenController() {
        configuration = MemoriConfiguration.getInstance();
        this.miscellaneousSoundsBasePath = configuration.getDataPackProperty("MISCELLANEOUS_SOUNDS");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // if the game has vs_player option enabled, show the button
        // else hide it

        if (configuration.getDataPackProperty("VS_CPU_ENABLED").equalsIgnoreCase("true")) {
            versus_computer.setVisible(true);
        } else {
            btnContainer.getChildren().remove(versus_computer);
        }
        if (MemoriConfiguration.inputMethodIsMouseOrTouch())
            tutorialBtn.setVisible(false);
    }

    public void setParameters(FXSceneHandler sceneHandler, Scene scene) {
        this.primaryScene = scene;
        super.setParameters(sceneHandler, scene);
        FXRenderingEngine.setGamecoverIcon(this.primaryScene, "gameCoverImgContainer");
        attachButtonFocusHandlers();
    }

    /**
     * Attaches focus handlers to fixed buttons (tutorial, exit, etc.)
     */
    protected void attachButtonFocusHandlers() {

        primaryScene.lookup("#single_player").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "single_player.mp3", false);
            }
        });

        if (configuration.getDataPackProperty("VS_CPU_ENABLED").equalsIgnoreCase("true")) {
            primaryScene.lookup("#versus_computer").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_cpu.mp3", false);
                }
            });
        }
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializeTutorialGameEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializeTutorialGame();
            }
        } else {
            initializeTutorialGame();
        }
    }

    protected void initializeTutorialGame() {
        MemoriGameLauncher memoriGameLauncher = new MemoriGameLauncher(sceneHandler);
        Thread thread = new Thread(() -> memoriGameLauncher.startTutorialGame());
        thread.start();
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializeSinglePlayerGameEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializeSinglePlayerGame();
            }
        } else {
            initializeSinglePlayerGame();
        }
    }

    protected void initializeSinglePlayerGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new LevelsScreen(sceneHandler, GameType.SINGLE_PLAYER);
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializePvCGameEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializePvCGame();
            }
        } else {
            initializePvCGame();
        }
    }

    protected void initializePvCGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new LevelsScreen(sceneHandler, GameType.VS_CPU);
    }

    @FXML
    protected void goToSponsorsPageEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                openSponsorsPage();
            }
        } else {
            openSponsorsPage();
        }
    }

    protected void openSponsorsPage() {
        String url = "https://memoristudio.scify.org/about#memori-game";
        if (Utils.isWindows()) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e) {
                    DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
                }
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            }
        }
    }
}
