
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

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.scify.memori.MemoriGameLauncher;
import org.scify.memori.PlayerManager;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXAudioEngine;
import org.scify.memori.fx.FXRenderingEngine;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.RequestManager;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.KeyCode.SPACE;

public class MainScreenController implements Initializable {

    public Button sponsors;
    public Button versus_player;
    public VBox btnContainer;
    public Button versus_computer;
    private final MemoriConfiguration configuration;
    private final String miscellaneousSoundsBasePath;
    private Stage primaryStage;
    private static Scene primaryScene;
    protected FXSceneHandler sceneHandler = new FXSceneHandler();
    private final FXAudioEngine audioEngine = new FXAudioEngine();

    public MainScreenController() {
        configuration = new MemoriConfiguration();
        this.miscellaneousSoundsBasePath = configuration.getProjectProperty("MISCELLANEOUS_SOUNDS");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // if the game is in english, we want to hide the "sponsors" button
        if (configuration.getProjectProperty("APP_LANG").toLowerCase().equals("en")) {
            sponsors.setVisible(false);
        }
        // if the game has vs_player option enabled, show the button
        // else hide it
        if (configuration.getProjectProperty("VS_PLAYER_ENABLED").toLowerCase().equals("true")) {
            versus_player.setVisible(true);
        } else {
            btnContainer.getChildren().remove(versus_player);
        }

        if (configuration.getProjectProperty("VS_CPU_ENABLED").toLowerCase().equals("true")) {
            versus_computer.setVisible(true);
        } else {
            btnContainer.getChildren().remove(versus_computer);
        }
    }


    public void setParameters(Stage primaryStage, Scene primaryScene) {
        this.primaryScene = primaryScene;
        this.primaryStage = primaryStage;
        addCloseHandlerOnStage();
        primaryScene.getStylesheets().add("css/style.css");
        primaryStage.show();
        primaryStage.requestFocus();
        primaryStage.sizeToScene();
        primaryStage.setFullScreen(true);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        FXRenderingEngine.setGamecoverIcon(primaryScene, "gameCoverImgContainer");

        setStageFavicon(primaryStage);
        sceneHandler.setMainWindow(primaryStage);
        sceneHandler.pushScene(primaryScene);

        attachButtonFocusHandlers();
        primaryStage.show();

    }

    private void setStageFavicon(Stage primaryStage) {
        ResourceLocator resourceLocator = new ResourceLocator();
        String gameCoverImgPath = resourceLocator.getCorrectPathForFile(configuration.getProjectProperty("IMAGES_BASE_PATH") + configuration.getProjectProperty("GAME_COVER_IMG_PATH"), "game_cover.png");
        //set the "favicon"
        Image faviconImage = new Image(gameCoverImgPath);
        primaryStage.getIcons().add(faviconImage);
    }

    public static void screenPoppedUI() {
        primaryScene.lookup("#welcome").requestFocus();
    }


    /**
     * Attaches focus handlers to fixed buttons (tutorial, exit, etc)
     */
    private void attachButtonFocusHandlers() {

        primaryScene.lookup("#welcome").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "welcome.mp3", false);
            }
        });

        primaryScene.lookup("#headphones_adjustment").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "headphones_adjustment.mp3", false);
            }
        });

        primaryScene.lookup("#tutorial").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "tutorial.mp3", false);
            }
        });

        primaryScene.lookup("#single_player").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "single_player.mp3", false);
            }
        });

        if (configuration.getProjectProperty("VS_CPU_ENABLED").toLowerCase().equals("true")) {
            primaryScene.lookup("#versus_computer").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_cpu.mp3", false);
                }
            });
        }

        if (configuration.getProjectProperty("VS_PLAYER_ENABLED").toLowerCase().equals("true")) {
            primaryScene.lookup("#versus_player").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "vs_player.mp3", false);
                }
            });
        }

        primaryScene.lookup("#my_scores").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "my_scores.mp3", false);
            }
        });

        primaryScene.lookup("#exit").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "exit.mp3", false);
            }
        });

        if (!configuration.getProjectProperty("APP_LANG").toLowerCase().equals("en")) {
            primaryScene.lookup("#sponsors").focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
                if (newPropertyValue) {
                    audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "sponsors.mp3", false);
                }
            });
        }
    }

    /**
     * Quits game
     *
     * @param evt the keyboard event
     */
    @FXML
    protected void exitGame(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE || keyEvt.getCode() == ESCAPE) {
                exitApplication();
            }
        } else if (evt.getClass() == TouchEvent.class) {
            exitApplication();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
                exitApplication();
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
        } else if (evt.getClass() == TouchEvent.class) {
            initializeTutorialGame();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
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
        } else if (evt.getClass() == TouchEvent.class) {
            initializeSinglePlayerGame();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
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
        } else if (evt.getClass() == TouchEvent.class) {
            initializePvCGame();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
                initializePvCGame();
        }
    }

    protected void initializePvCGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new LevelsScreen(sceneHandler, GameType.VS_CPU);
    }

    /**
     * When the user clicks on "tutorial" button, start a new tutorial game
     *
     * @param evt the click event
     */
    @FXML
    protected void initializePvPGameEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                initializePvPGame();
            }
        } else if (evt.getClass() == TouchEvent.class) {
            initializePvPGame();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
                initializePvPGame();
        }
    }

    protected void initializePvPGame() {
        audioEngine.pauseCurrentlyPlayingAudios();
        if (RequestManager.networkAvailable()) {
            new RegisterOrLoginScreen(sceneHandler);
        } else {
            audioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "no_network.mp3", false);
        }

    }

    @FXML
    protected void myScoresEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                myScores();
            }
        } else if (evt.getClass() == TouchEvent.class) {
            myScores();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
                myScores();
        }
    }


    protected void myScores() {
        audioEngine.pauseCurrentlyPlayingAudios();
        new FXHighScoresScreen(sceneHandler, sceneHandler.getMainWindow());
    }

    @FXML
    protected void goToSponsorsPageEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                goToSponsorsPage();
            }
        } else if (evt.getClass() == TouchEvent.class) {
            goToSponsorsPage();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
                goToSponsorsPage();
        }
    }

    protected void goToSponsorsPage() {
        new SponsorsScreen(sceneHandler, sceneHandler.getMainWindow());
    }

    @FXML
    protected void headphonesAdjustmentEventHandler(Event evt) {
        if (evt.getClass() == KeyEvent.class) {
            KeyEvent keyEvt = (KeyEvent) evt;
            if (keyEvt.getCode() == SPACE) {
                headphonesAdjustment();
            }
        } else if (evt.getClass() == TouchEvent.class) {
            headphonesAdjustment();
        } else if (evt.getClass() == MouseEvent.class) {
            if (((MouseEvent) evt).getButton() == MouseButton.PRIMARY)
                headphonesAdjustment();
        }
    }

    protected void headphonesAdjustment() {
        audioEngine.playBalancedSound(-1.0, this.miscellaneousSoundsBasePath + "left_headphone.mp3", true);
        audioEngine.playBalancedSound(1.0, this.miscellaneousSoundsBasePath + "right_headphone.mp3", true);
    }

    private void addCloseHandlerOnStage() {
        primaryStage.setOnCloseRequest(t -> exitApplication());
    }

    private void exitApplication() {
        System.out.println("Stage is closing");
        audioEngine.pauseCurrentlyPlayingAudios();
        // if player has logged in, perform call to set them as non-active
        Player player = PlayerManager.getLocalPlayer();
        PlayerManager playerManager = new PlayerManager();
        if (player != null) {
            System.err.println(player.getName());
            playerManager.setPlayerAsNotInGame();
            System.out.println("player set not in game. Closing...");
        } else {
            System.out.println("no local player. Closing...");
        }
        Platform.exit();
        System.exit(0);
    }
}
