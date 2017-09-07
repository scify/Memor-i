package org.scify.memori.rules;

import org.scify.memori.*;
import org.scify.memori.helper.TimeWatch;
import org.scify.memori.interfaces.*;

import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SinglePlayerRules extends MemoriRules {

    /**
     * a {@link TimeWatch} instance to track the elapsed time of the game
     */
    private TimeWatch watch;
    /**
     * When the user makes the first move, start the watch
     * This variable is used to identify if the watch has been already started or not
     */
    private boolean watchStarted = false;
    /**
     * a {@link HighScoresHandlerImpl} instance to handle the high score as soon as the game has finished
     */
    private HighScoresHandlerImpl highScore;

    public SinglePlayerRules(GameLevel gameLevel) {
        super(gameLevel);
        highScore = new HighScoresHandlerImpl();
    }

    public GameState getNextState(GameState gsCurrent, UserAction uaAction) {

        MemoriGameState gsCurrentState = (MemoriGameState) gsCurrent;
        if(eventQueueContainsBlockingEvent(gsCurrentState)) {
            return gsCurrentState;
        }
        handleGameStartingGameEvents(gsCurrentState);

        if(uaAction != null) {
            handleUserActionSinglePlayerGameEvents(uaAction, gsCurrentState);
            //After the first user action, start the stopwatch
            if (!watchStarted) {
                watch = TimeWatch.start();
                watchStarted = true;
            }
        }
        if(isLastRound(gsCurrent)) {
            //if ready to finish event already in events queue
            this.handleSinglePlayerFinishGameEvents(uaAction, gsCurrentState);
        }
        return gsCurrent;
    }

    private void handleUserActionSinglePlayerGameEvents(UserAction uaAction, MemoriGameState gsCurrentState) {
        if(movementValid(uaAction.getDirection(), gsCurrentState)) {
            handleValidActionSinglePlayerEvent(uaAction, gsCurrentState);
        } else {
            gsCurrentState.getEventQueue().add(invalidMovementGameEvent(gsCurrentState));
        }
    }

    private void handleValidActionSinglePlayerEvent(UserAction uaAction, MemoriGameState gsCurrentState) {
        updateGameStateIndexesAndUserActionCoords(uaAction, gsCurrentState);

        MemoriTerrain memoriTerrain = (MemoriTerrain) (gsCurrentState.getTerrain());
        // currTile is the tile that was moved on or acted upon
        Tile currTile = memoriTerrain.getTile(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex());
        if(uaAction.getActionType().equals("movement")) {
            movementUI(uaAction, gsCurrentState);

        } else if (uaAction.getActionType().equals("flip")) {
            performFlipSinglePlayer(currTile, gsCurrentState, uaAction, memoriTerrain);
        } else if(uaAction.getActionType().equals("enter")) {
            createHelpGameEvent(uaAction, gsCurrentState);
        } else if(uaAction.getActionType().equals("escape")) {
            //exit current game
            gsCurrentState.setGameFinished(true);
        }
    }

    /**
     * Applies the rules and creates the game events relevant to flipping a card
     * @param currTile the tile that the flip performed on
     * @param gsCurrentState the current game state
     * @param uaAction the user action object
     * @param memoriTerrain the terrain holding all the tiles
     */
    protected void performFlipSinglePlayer(Tile currTile, MemoriGameState gsCurrentState, UserAction uaAction, MemoriTerrain memoriTerrain) {
        // Rule 6: flip
        // If target card flipped
        if(isTileFlipped(currTile)) {
            //if card won
            if(isTileWon(currTile)) {
                //play empty sound
                gsCurrentState.getEventQueue().add(new GameEvent("EMPTY"));
            } else {
                //else if card not won
                // play card sound
                gsCurrentState.getEventQueue().add(new GameEvent("CARD_SOUND_UI", uaAction.getCoords(), 0, true));
            }
        } else {
            // else if not flipped
            // flip card
            flipTile(currTile);
            flipTileUI(uaAction, gsCurrentState);

            if(tileIsLastOfTuple(memoriTerrain, currTile)) {
                // If last of n-tuple flipped (i.e. if we have enough cards flipped to form a tuple)
                successUI(uaAction, gsCurrentState);
                cardDescriptionSoundUI(gsCurrentState);
                updateGameStateAndNextTurn(currTile, gsCurrentState, memoriTerrain);
            } else {
                // else not last card in tuple
                if(atLeastOneOtherTileIsDifferent(memoriTerrain, currTile)) {
                    // Flip card back
                    flipBackTileAndAddToOpenCards(currTile, gsCurrentState, memoriTerrain);
                    doorsShuttingUI(gsCurrentState);
                    nextTurn(gsCurrentState);
                } else {
                    memoriTerrain.addTileToOpenTiles(currTile);
                }
            }

        }
    }

    protected void handleGameStartingGameEvents(MemoriGameState gsCurrentState) {
        if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "STORYLINE_AUDIO")) {
            gsCurrentState.getEventQueue().add(new GameEvent("STORYLINE_AUDIO"));
            gsCurrentState.getEventQueue().add(new GameEvent("STORYLINE_AUDIO_UI", null, 0, true));
            if(!(MainOptions.GAME_LEVEL_CURRENT == 4)) {
                if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "FUN_FACTOR")) {
                    gsCurrentState.getEventQueue().add(new GameEvent("FUN_FACTOR"));
                    if(MainOptions.storyLineLevel % 2 == 1) {
                        gsCurrentState.getEventQueue().add(new GameEvent("FUN_FACTOR_UI", null, 0, true));
                    }
                }
            }
            if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "LEVEL_INTRO_AUDIO")) {
                gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_INTRO_AUDIO"));
                gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_INTRO_AUDIO_UI", null, 1000, true));
            }
        }

        if(MainOptions.GAME_LEVEL_CURRENT == 4) {
            if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "HELP_INSTRUCTIONS")) {
                gsCurrentState.getEventQueue().add(new GameEvent("HELP_INSTRUCTIONS"));
                gsCurrentState.getEventQueue().add(new GameEvent("HELP_INSTRUCTIONS_UI", null, 0, false));
            }
        }
    }

    private void handleSinglePlayerFinishGameEvents(UserAction userAction, MemoriGameState gsCurrentState) {
        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "READY_TO_FINISH_GAME")) {
            //add appropriate event
            gsCurrentState.getEventQueue().add(new GameEvent("READY_TO_FINISH_GAME"));
            //add UI events
            gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_SUCCESS_STEP_1", null, new Date().getTime() + 5500, true));
            addTimeGameEvent(watch, gsCurrentState);

            gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_SUCCESS_STEP_2", null, new Date().getTime() + 7500, true));

            if(MainOptions.GAME_LEVEL_CURRENT < MainOptions.MAX_NUM_OF_LEVELS) {
                gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_END_UNIVERSAL", null, new Date().getTime() + 8600, true));
            } else {
                gsCurrentState.getEventQueue().add(new GameEvent("GAME_END", null, new Date().getTime() + 8600, true));
                gsCurrentState.getEventQueue().add(new GameEvent("PRESS_EXIT", null, new Date().getTime() + 8600, true));
            }
            //update high score
            highScore.updateHighScore(watch);
        } else {
            super.handleLevelFinishGameEvents(userAction, gsCurrentState);
        }
    }

    /**
     * Prepares the UI events that will play the sound clips for the high score
     * @param watch The watch object that contains the timer
     * @param gsCurrentState the current game state
     */
    private void addTimeGameEvent(TimeWatch watch, MemoriGameState gsCurrentState) {
        long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
        String timestampStr = String.valueOf(ConvertSecondToHHMMSSString((int) passedTimeInSeconds));
        String[] tokens = timestampStr.split(":");
        int minutes = Integer.parseInt(tokens[1]);
        int seconds = Integer.parseInt(tokens[2]);
        System.err.println("minutes: " + minutes);
        System.err.println("seconds: " + seconds);
        if(minutes != 0) {
            gsCurrentState.getEventQueue().add(new GameEvent("NUMERIC", minutes, new Date().getTime() + 5200, true));
            if(minutes > 1)
                gsCurrentState.getEventQueue().add(new GameEvent("MINUTES", minutes, new Date().getTime() + 5300, true));
            else
                gsCurrentState.getEventQueue().add(new GameEvent("MINUTE", minutes, new Date().getTime() + 5500, true));
        }
        if(minutes != 0 && seconds != 0)
            gsCurrentState.getEventQueue().add(new GameEvent("AND", minutes, new Date().getTime() + 5700, true));
        if(seconds != 0) {
            gsCurrentState.getEventQueue().add(new GameEvent("NUMERIC", seconds, new Date().getTime() + 6000, true));
            if(seconds > 1)
                gsCurrentState.getEventQueue().add(new GameEvent("SECONDS", minutes, new Date().getTime() + 5300, true));
            else
                gsCurrentState.getEventQueue().add(new GameEvent("SECOND", minutes, new Date().getTime() + 5500, true));
        }

    }

    private String ConvertSecondToHHMMSSString(int nSecondTime) {
        return LocalTime.MIN.plusSeconds(nSecondTime).toString();
    }
}
