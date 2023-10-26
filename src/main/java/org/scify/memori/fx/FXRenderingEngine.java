
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

package org.scify.memori.fx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.scify.memori.MainOptions;
import org.scify.memori.MemoriGameLevel;
import org.scify.memori.MemoriGameState;
import org.scify.memori.MemoriTerrain;
import org.scify.memori.card.Card;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.helper.UTF8Control;
import org.scify.memori.interfaces.*;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;

import static javafx.scene.input.KeyCode.*;

/**
 * The Rendering Engine is responsible for handling Game Events (drawing, playing audios etc) as well as implementing the UI events listener
 * (keyboard events in this case)
 */
public class FXRenderingEngine implements RenderingEngine<MemoriGameState>, UI, EventHandler<Event> {

    /**
     * The rendering engine processes the game events, one at a time.
     * The currently processed {@link GameEvent} may block any UI input.
     */
    GameEvent currentGameEvent;
    /**
     * A {@link AudioEngine} object, able to play sounds
     */
    private AudioEngine fxAudioEngine;

    /**
     * The {@link GridPane} holds all cards
     */
    private GridPane gridPane;
    /**
     * first draw defines whether the rendering engine will initialize or update the UI components
     */
    private boolean firstDraw = true;
    /**
     * current game scene
     */
    Scene gameScene;
    /**
     * JavFX component to bind the scene with the .fxml and .css file
     */
    protected Parent root;
    protected VBox vBox;
    protected Button backBtn;

    /**
     * Every time we play a game we follow the story line
     */
    private final ArrayList<String> storyLineSounds = new ArrayList<>();

    /**
     * Fun factor sounds occur every 3 levels
     */
    protected List<String> funFactorSounds = new ArrayList<>();
    protected List<String> cpuIntroMessages = new ArrayList<>();
    protected List<String> playerWonRoundMessages = new ArrayList<>();
    protected List<String> playerLostRoundMessages = new ArrayList<>();
    protected String packageName;
    protected String storyLineSoundsBasePath;


    protected String gameInstructionSoundsBasePath;

    protected String miscellaneousSoundsBasePath;

    protected String funFactorSoundsBasePath;

    protected String endLevelStartingSoundsBasePath;

    protected String endLevelEndingSoundsBasePath;

    protected String multiPlayerSoundsBasePath;
    /**
     * Every time a level ends, we should construct the end level Sound which consists of:
     * 1) starting sound 2) the time in which the player finished the level 3) an ending sound
     */
    private final ArrayList<String> endLevelStartingSounds = new ArrayList<>();
    private final ArrayList<String> endLevelEndingSounds = new ArrayList<>();

    private final MemoriGameLevel gameLevel;

    public FXRenderingEngine(MemoriGameLevel gameLevel) {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        this.gameLevel = gameLevel;
        this.packageName = configuration.getDataPackProperty("DATA_PACKAGE");
        this.storyLineSoundsBasePath = configuration.getDataPackProperty("STORYLINE_SOUNDS");
        this.gameInstructionSoundsBasePath = configuration.getDataPackProperty("GAME_INSTRUCTION_SOUNDS");
        this.miscellaneousSoundsBasePath = configuration.getDataPackProperty("MISCELLANEOUS_SOUNDS");
        this.funFactorSoundsBasePath = configuration.getDataPackProperty("FUN_FACTOR_SOUNDS");
        this.endLevelStartingSoundsBasePath = configuration.getDataPackProperty("END_LEVEL_STARTING_SOUNDS");
        this.endLevelEndingSoundsBasePath = configuration.getDataPackProperty("END_LEVEL_ENDING_SOUNDS");
        this.multiPlayerSoundsBasePath = configuration.getDataPackProperty("MULTIPLAYER_SOUNDS_BASE_PATH");

        try {
            Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"),
                    ResourceBundle.getBundle("languages.strings", locale, new UTF8Control()));
            root = loader.load();
        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            return;
        }
        this.initialiseGameSoundLists();

        fxAudioEngine = FXAudioEngine.getInstance();

        double mWidth = Screen.getPrimary().getBounds().getWidth();
        double mHeight = Screen.getPrimary().getBounds().getHeight();
        gameScene = new Scene(root, mWidth, mHeight);
    }

    protected void initialiseGameSoundLists() {

        for (int i = 1; i < 5; i++) {
            //TODO: should poll from sounds that exist in the additional pack only?
            endLevelStartingSounds.add("sound" + i + ".mp3");
        }
        for (int i = 1; i < 5; i++) {
            //TODO: should poll from sounds that exist in the additional pack only?
            endLevelEndingSounds.add("sound" + i + ".mp3");
        }
        for (int i = 1; i < 10; i++) {
            storyLineSounds.add("storyLine" + i + ".mp3");
        }
        for (int i = 1; i < 11; i++) {
            //TODO: should poll from fun factor sounds that exist in the additional pack only?
            funFactorSounds.add(i + ".mp3");
        }

        for (int i = 1; i < 3; i++) {
            //TODO: should poll from fun factor sounds that exist in the additional pack only?
            cpuIntroMessages.add("vs_cpu_intro_" + i + ".mp3");
        }
        for (int i = 1; i < 3; i++) {
            //TODO: should poll from fun factor sounds that exist in the additional pack only?
            playerLostRoundMessages.add("vs_cpu_lost_" + i + ".mp3");
        }
        for (int i = 1; i < 5; i++) {
            //TODO: should poll from fun factor sounds that exist in the additional pack only?
            playerWonRoundMessages.add("vs_cpu_won_" + i + ".mp3");
        }
    }


    /**
     * List of actions captured by the user interaction. User in the Player-derived methods.
     */
    List<UserAction> pendingUserActions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void drawGameState(MemoriGameState currentState) {
        if (firstDraw) {
            //initialize UI components=
            try {
                setUpFXComponents();
                initFXComponents(currentState);
            } catch (IOException e) {
                DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            }
            firstDraw = false;
        } else {
            //update UI components
            updateFXComponents(currentState);
        }
    }

    public void setUpFXComponents() throws IOException {
        vBox = (VBox) root.lookup("#vBox");
        gridPane = ((GridPane) vBox.lookup("#gameGrid"));
        backBtn = (Button) vBox.lookup("#backBtn");
        if (MemoriConfiguration.inputMethodIsMouseOrTouch()) {
            backBtn.setOnTouchPressed(this::exitGame);
            backBtn.setOnMouseClicked(this::exitGame);
        } else {
            Platform.runLater(() -> vBox.getChildren().remove(backBtn));
        }
        gameScene.getStylesheets().add("css/style.css");
    }

    protected void exitGame(Event event) {
        UserAction userAction = new UserAction("escape", "SPACE");
        event.consume();
        addUserAction(userAction);
    }

    protected void initFXComponents(MemoriGameState currentState) {
        MemoriTerrain terrain = (MemoriTerrain) currentState.getTerrain();
        //Load the tiles list from the Terrain
        Map<Point2D, Tile> initialTiles = terrain.getTiles();
        //Iterate through the tiles list to add them to the Layout object
        for (Map.Entry<Point2D, Tile> point2DTileEntry : initialTiles.entrySet()) {
            Point2D point = (Point2D) ((Map.Entry) point2DTileEntry).getKey();

            Card card = (Card) ((Map.Entry) point2DTileEntry).getValue();
            //add the card to layout when the Thread deems appropriate
            Platform.runLater(() -> {
                try {
                    gridPane.add(card.getButton(), (int) point.getX(), (int) point.getY());
                } catch (Exception e) {
                    System.out.println("FAILED x: " + point.getX());
                    System.out.println("FAILED y: " + point.getY());
                    System.out.println("FAILED: " + card.getButton().getId());
                    DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
                }
            });
            //Set up the event handler for the current card
            card.getButton().setOnKeyPressed(this);
            card.getButton().setOnTouchPressed(this);
            card.getButton().setOnMouseClicked(this);
        }

        Platform.runLater(() -> { //set first card as focused
            gridPane.getChildren().get(0).getStyleClass().addAll("focusedCard");
        });
    }

    @Override
    public UserAction getNextUserAction(Player pCurrentPlayer) {
        UserAction toReturn = null;
        if (!pendingUserActions.isEmpty()) {
            toReturn = pendingUserActions.get(0);
            pendingUserActions.remove(0);
        }
        return toReturn;
    }

    /**
     * Pauses every rendering function
     */
    @Override
    public void cancelCurrentRendering() {
        fxAudioEngine.pauseCurrentlyPlayingAudios();
    }

    private long lLastUpdate = -1L;

    protected void updateFXComponents(MemoriGameState currentState) {
        long lNewTime = new Date().getTime();
        if (lNewTime - lLastUpdate < 100L) {// If no less than 1/10 sec has passed
            Thread.yield();
            return; // Do nothing
        } else {
            lLastUpdate = lNewTime;
            List<GameEvent> eventsList = Collections.synchronizedList(currentState.getEventQueue());
            ListIterator<GameEvent> listIterator = eventsList.listIterator();
            while (listIterator.hasNext()) {
                currentGameEvent = listIterator.next();
                String eventType = currentGameEvent.type;
                Point2D coords;
                Card currCard;
                switch (eventType) {
                    case "movement":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            coords = (Point2D) currentGameEvent.parameters;
                            movementSound((int) coords.getX(), (int) coords.getY());
                            Platform.runLater(() -> {
                                focusOnTile((int) coords.getX(), (int) coords.getY());
                                //System.out.println("now at: " + coords.getX() + "," + coords.getY());
                            });
                            listIterator.remove();
                        }
                        break;
                    case "INVALID_MOVEMENT_UI":
                        coords = (Point2D) currentGameEvent.parameters;
                        invalidMovementSound((int) coords.getX(), (int) coords.getY(), currentGameEvent.blocking);
                        listIterator.remove();

                        break;
                    case "EMPTY":
                        /**
                         * Plays an appropriate sound associated with an "empty" Game Event (if the user clicks on an already won Card)
                         */
                        fxAudioEngine.playSound("miscellaneous/visited_card.wav");
                        listIterator.remove();
                        break;
                    case "CARD_SOUND_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {

                            coords = (Point2D) currentGameEvent.parameters;
                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());

                            fxAudioEngine.playSound(currCard.getRandomSound(), currentGameEvent.blocking);
                            listIterator.remove();

                        }
                        break;
                    case "CARD_DESCRIPTION":
//                        if (new Date().getTime() > currentGameEvent.delay) {
//
//                            coords = (Point2D) currentGameEvent.parameters;
//                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());
//
//                            fxAudioEngine.pauseAndPlaySound(currCard.getDescriptionSound(), currentGameEvent.blocking);
//                            listIterator.remove();
//
//                        }
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound((String) currentGameEvent.parameters, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "FOCUS":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            coords = (Point2D) currentGameEvent.parameters;
                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());
                            Platform.runLater(() -> {
                                focusOnTile((int) coords.getX(), (int) coords.getY());
                            });
                            listIterator.remove();
                        }
                        break;
                    case "FLIP_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            coords = (Point2D) currentGameEvent.parameters;
                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());
                            Platform.runLater(() -> {
                                currCard.flipUI(0);
                            });
                            listIterator.remove();
                        }
                        break;
                    case "TURN_ANIMATION":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            coords = (Point2D) currentGameEvent.parameters;
                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());
                            Platform.runLater(() -> {
                                currCard.turnCard();
                            });
                            listIterator.remove();
                        }
                        break;
                    case "FLIP_SECOND_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            coords = (Point2D) currentGameEvent.parameters;
                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());
                            Platform.runLater(() -> {
                                currCard.flipUI(1);
                            });
                            listIterator.remove();
                        }
                        break;
                    case "FLIP_BACK_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            coords = (Point2D) currentGameEvent.parameters;
                            currCard = (Card) currentState.getTerrain().getTile((int) coords.getX(), (int) coords.getY());
                            Platform.runLater(() -> {
                                currCard.flipBackUI();
                            });
                            listIterator.remove();
                        }
                        break;
                    case "SUCCESS_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSuccessSound();
                            listIterator.remove();
                        }
                        break;
                    case "NUMERIC":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int number = (Integer) currentGameEvent.parameters;
                            fxAudioEngine.playNumSound(number);
                            listIterator.remove();
                        }
                        break;
                    case "LETTER":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int number = (Integer) currentGameEvent.parameters;
                            fxAudioEngine.playLetterSound(number);
                            listIterator.remove();
                        }
                        break;
                    case "HELP_INSTRUCTIONS_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "help_instructions.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "HELP_EXPLANATION_ROW":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "help_explanation_row.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "HELP_EXPLANATION_COLUMN":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "help_explanation_column.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "LEVEL_SUCCESS_STEP_1":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int idx = new Random().nextInt(endLevelStartingSounds.size());
                            String randomSound = (endLevelStartingSounds.get(idx));
                            System.out.println(randomSound);
                            fxAudioEngine.pauseAndPlaySound(this.endLevelStartingSoundsBasePath + randomSound, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "LEVEL_SUCCESS_STEP_2":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int idx = new Random().nextInt(endLevelStartingSounds.size());
                            String randomSound = (endLevelEndingSounds.get(idx));
                            fxAudioEngine.pauseAndPlaySound(this.endLevelEndingSoundsBasePath + randomSound, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "FUN_FACTOR_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int randInt = new Random().nextInt(funFactorSounds.size());
                            String randomSound = (funFactorSounds.get(randInt));
                            fxAudioEngine.pauseAndPlaySound(this.funFactorSoundsBasePath + randomSound, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "GAME_END":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.endLevelEndingSoundsBasePath + "game_end_sound.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "PRESS_EXIT":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "replay_or_exit.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_INTRO_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.gameInstructionSoundsBasePath + "tutorial_intro_step_1.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "STORYLINE_AUDIO_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.storyLineSoundsBasePath + storyLineSounds.get(MainOptions.STORY_LINE_LEVEL - 1), currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "LEVEL_INTRO_AUDIO_UI":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(gameLevel.getIntroSound(), currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "LEVEL_END_UNIVERSAL":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "level_ending_universal.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "MINUTE":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "minute.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "MINUTES":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "minutes.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "AND":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "and.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "SECOND":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "second.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "SECONDS":
                        //check if the event should happen after some time
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "seconds.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_0_UI":
                        fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "tutorial_intro_step_2.mp3", currentGameEvent.blocking);
                        listIterator.remove();
                        break;
                    case "GO_RIGHT_AGAIN":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.gameInstructionSoundsBasePath + "press_right_until_end.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_2_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "please_press_down.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "DOORS_EXPLANATION_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "doors_explanation.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "FLIP_EXPLANATION_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "flip_explanation.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_INVALID_MOVEMENT_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "tutorial_invalid_movement.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "NOT_RIGHT_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.gameInstructionSoundsBasePath + "press_right.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "NOT_LEFT_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.gameInstructionSoundsBasePath + "press_left.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_WRONG_PAIR_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "wrong_pair.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_DOORS_CLOSED_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "doors_closing_explanation.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_CORRECT_PAIR_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.gameInstructionSoundsBasePath + "correct_pair_explanation.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "DOORS_SHUTTING":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(this.miscellaneousSoundsBasePath + "doors_shutting.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "TUTORIAL_END_GAME_UI":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.gameInstructionSoundsBasePath + "tutorial_ending.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "DOOR_OPEN":
                        // TODO (1): We need the door sound to be position-variable. For example, when opening the window from the left,
                        // the open door sound should be emitted from from the left headphone.
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.miscellaneousSoundsBasePath + "open_door.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "STOP_AUDIOS":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseCurrentlyPlayingAudios();
                            listIterator.remove();
                        }
                        break;
                    case "LEVEL_DESCRIPTION":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.pauseAndPlaySound(gameLevel.getDescriptionSound(), currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "CPU_INTRO_MESSAGE":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int idx = new Random().nextInt(cpuIntroMessages.size());
                            String randomSound = (cpuIntroMessages.get(idx));
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "game_starting_sounds/" + randomSound, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "PLAYER_WON_ROUND_MESSAGE":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int idx = new Random().nextInt(playerWonRoundMessages.size());
                            String randomSound = (playerWonRoundMessages.get(idx));
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "player_won_round/" + randomSound, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "CPU_WON_ROUND_MESSAGE":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            int idx = new Random().nextInt(playerLostRoundMessages.size());
                            String randomSound = (playerLostRoundMessages.get(idx));
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "cpu_won_round/" + randomSound, currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "GAME_TIE":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "game_tie.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "GAME_WON_VS_CPU":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "vs_cpu_game_won.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "GAME_LOST_VS_CPU":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "vs_cpu_game_lost.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "LOCAL_PLAYER_IS_INITIATOR":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "vs_player_game_started_initiator.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "OPPONENT_IS_INITIATOR":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "vs_player_game_started_guest.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "GAME_WON_VS_PLAYER":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "vs_player_game_won.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "GAME_LOST_VS_PLAYER":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "vs_player_game_lost.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "PRESS_ESCAPE_TO_QUIT":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.miscellaneousSoundsBasePath + "press_escape.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "PLAYER_ABANDONED":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "player_abandoned.mp3", currentGameEvent.blocking);
                            listIterator.remove();
                        }
                        break;
                    case "NOT_YOUR_TURN":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            fxAudioEngine.playSound(this.multiPlayerSoundsBasePath + "not_your_turn1.mp3");
                            listIterator.remove();
                        }
                        break;
                    case "YOUR_TURN":
                        if (new Date().getTime() > currentGameEvent.delay) {
                            listIterator.remove();
                        }
                        break;
                    default:
                        break;
                }
                currentGameEvent = null;
            }
        }
    }

    /**
     * When a level ends, play a success sound
     */
    @Override
    public void playGameOver() {
        System.err.println("play game over");
        Platform.runLater(() -> {
            fxAudioEngine.playSuccessSound();
        });
    }


    /**
     * Given the coordinates, marks a Node as visited (green background) by applying a CSS class
     *
     * @param rowIndex    the Node x position
     * @param columnIndex the Node y position
     */
    private void focusOnTile(int rowIndex, int columnIndex) {
        //get Node (in our case it's a button)
        Node node = getNodeByRowColumnIndex(rowIndex, columnIndex);
        //remove the focused class from every other Node
        ObservableList<Node> nodes = gridPane.getChildren();
        for (Node nd : nodes) {
            nd.getStyleClass().remove("focusedCard");
        }
        //apply the CSS class
        node.getStyleClass().addAll("focusedCard");
        Button btn = (Button) node;
        //DEBUG print button id
        // System.out.println(btn.getId());
    }

    /**
     * Computes the sound balance (left-right panning) and rate and plays the movement sound
     *
     * @param rowIndex    the Node x position
     * @param columnIndex the Node y position
     */
    private void movementSound(int rowIndex, int columnIndex) {
        double soundBalance = map(columnIndex, 0.0, gameLevel.getDimensions().getY(), -1.0, 2.0);
        double rate = map(rowIndex, 0.0, gameLevel.getDimensions().getX(), 1.5, 1.0);
        fxAudioEngine.playMovementSound(soundBalance, rate);
    }

    private Node getNodeByRowColumnIndex(final int row, final int column) {
        Node result = null;
        ObservableList<Node> children = gridPane.getChildren();
        for (Node node : children) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    /**
     * Handles the UI events (button clicks) and populates the {@link UserAction} list
     *
     * @param event the event emitted from Ui
     */
    @Override
    public void handle(Event event) {
        if (event.getClass() == KeyEvent.class) {
            handleKeyEvent((KeyEvent) event);
        } else {
            handleTouchOrMouseEvent(event);
        }
    }

    protected void handleKeyEvent(KeyEvent event) {
        UserAction userAction = null;
        //Handle different kinds of UI (keyboard) events
        if (event.getCode() == SPACE) {
            userAction = new UserAction("flip", getMovementDirection(event));
        } else if (isMovementAction(event)) {
            userAction = new UserAction("movement", getMovementDirection(event));
        } else if (event.getCode() == ENTER) {
            //userAction = new UserAction("help", event);
            userAction = new UserAction("enter", getMovementDirection(event));
        } else if (event.getCode() == F1) {
            userAction = new UserAction("f1", getMovementDirection(event));
        } else if (event.getCode() == ESCAPE) {
            userAction = new UserAction("escape", getMovementDirection(event));
            event.consume();
        }
        if (userAction != null) {
            userAction.setIsKeyboardEvent(true);
            addUserAction(userAction);
        }
    }

    protected void handleTouchOrMouseEvent(Event event) {
        int x = GridPane.getRowIndex((Node) event.getSource());
        int y = GridPane.getColumnIndex((Node) event.getSource());
        UserAction userAction = new UserAction("flip", "AT_" + x + "_" + y);
        event.consume();
        addUserAction(userAction);
    }

    protected void addUserAction(UserAction userAction) {
        //if there is a game event currently being processed
        if (currentGameEvent != null) {
            //if the currently processed event is blocking, the UI engine does not accept any user actions
            //if the currently processed event is not blocking, accept user actions
            if (!currentGameEvent.blocking)
                pendingUserActions.add(0, userAction);
        } else {
            //if there is no processed event, accept the user action
            pendingUserActions.add(0, userAction);
        }
    }

    /**
     * Determines whether the user action was a movement {@link GameEvent}.
     *
     * @param evt the action event
     * @return true if the evt was a movement action
     */
    private boolean isMovementAction(KeyEvent evt) {
        return evt.getCode() == UP || evt.getCode() == DOWN || evt.getCode() == LEFT || evt.getCode() == RIGHT;
    }

    private String getMovementDirection(KeyEvent evt) {
        return evt.getCode().toString();
    }

    /**
     * Computes the sound balance (left-right panning) and rate and plays the movement sound
     *
     * @param rowIndex    the Node x position
     * @param columnIndex the Node y position
     * @param isBlocking  if the event should block the ui thread
     */
    private void invalidMovementSound(int rowIndex, int columnIndex, boolean isBlocking) {
        double soundBalance = map(columnIndex, 0.0, gameLevel.getDimensions().getY(), -1.0, 2.0);
        double rate = map(rowIndex, 0.0, gameLevel.getDimensions().getX(), 1.5, 1.0);
        fxAudioEngine.playInvalidMovementSound(soundBalance, isBlocking);
    }

    //maps a value to a new set
    private double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static void setGamecoverIcon(Scene scene, String imgContainer) {
        ImageView gameCoverImgContainer = (ImageView) scene.lookup("#" + imgContainer);
        if (gameCoverImgContainer == null || !gameCoverImgContainer.isVisible())
            return;
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        ResourceLocator resourceLocator = ResourceLocator.getInstance();

        String gameCoverImgPath = resourceLocator.getCorrectPathForFile(configuration.getDataPackProperty("IMAGES_BASE_PATH") + configuration.getDataPackProperty("GAME_COVER_IMG_PATH"), "game_cover.png");
        gameCoverImgContainer.setImage(new Image(gameCoverImgPath));
        gameCoverImgContainer.setFitHeight(250);
        gameCoverImgContainer.setFitWidth(250);
        gameCoverImgContainer.setPreserveRatio(true);
    }
}
