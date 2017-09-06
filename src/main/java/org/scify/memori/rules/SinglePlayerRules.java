package org.scify.memori.rules;

import org.scify.memori.HighScoresHandlerImpl;
import org.scify.memori.MainOptions;
import org.scify.memori.MemoriGameState;
import org.scify.memori.helper.TimeWatch;
import org.scify.memori.interfaces.GameEvent;
import org.scify.memori.interfaces.GameLevel;
import org.scify.memori.interfaces.GameState;
import org.scify.memori.interfaces.UserAction;

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

        MemoriGameState gsCurrentState;

        gsCurrentState = (MemoriGameState) super.getNextState(gsCurrent, uaAction);
        singlePlayerRulesSet((MemoriGameState) gsCurrent);
        if(uaAction != null) {
            handleUserActionGameEvents(uaAction);
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

    private void singlePlayerRulesSet(MemoriGameState gsCurrentState) {
        handleGameStartingGameEvents(gsCurrentState);
    }

    private void handleUserActionGameEvents(UserAction userAction) {

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
