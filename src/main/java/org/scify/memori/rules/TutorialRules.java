package org.scify.memori.rules;

import javafx.scene.input.KeyCode;
import org.scify.memori.MainOptions;
import org.scify.memori.MemoriGameState;
import org.scify.memori.interfaces.GameEvent;
import org.scify.memori.interfaces.GameState;
import org.scify.memori.interfaces.UserAction;

import java.awt.geom.Point2D;
import java.util.Date;
import java.util.Objects;

/**
 * Class that contains the rules that integrate in the tutorial game.
 */
public class TutorialRules extends MemoriRules {

    public TutorialRules() {
        super();
    }

    public GameState getInitialState() {
        return super.getInitialState();
    }

    public GameState getNextState(GameState gsCurrent, UserAction uaAction) {

        MemoriGameState gsCurrentState;

        gsCurrentState = (MemoriGameState) super.getNextState(gsCurrent, uaAction);
        handleGameStartingGameEvents(gsCurrentState);
        // handle the tutorial game events
        if(uaAction != null)
            tutorialRulesSet((MemoriGameState) gsCurrent, uaAction);
        if(isLastRound(gsCurrent)) {
            //if ready to finish event already in events queue
            handleLevelFinishGameEvents(gsCurrentState);
        }
        return gsCurrent;
    }

    protected void handleGameStartingGameEvents(MemoriGameState gsCurrentState) {
        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_INTRO")) {
            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INTRO"));
            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INTRO_UI", null, 0, true));
        }
    }

    protected void handleUserActionGameEvents(UserAction uaAction, MemoriGameState gsCurrentState, int delay) {
        if(!movementValid(uaAction.getDirection(), gsCurrentState)) {
            if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_INVALID_MOVEMENT")) {
                //the invalid movement tutorial event should be emitted only if the tutorial has reached a certain point (step 2 which is go right second time)
                if(eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_1_STEP_2")) {
                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INVALID_MOVEMENT_UI", new Point2D.Double(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex()), new Date().getTime() + 500, true));
                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INVALID_MOVEMENT"));
                }
            }
        }
    }

    /**
     * When in tutorial mode, handles the tutorial game events
     * @param gsCurrentState the current game state
     * @param uaAction the user action object
     */
    protected void tutorialRulesSet(MemoriGameState gsCurrentState, UserAction uaAction) {

        // if tutorial_0 event does not exist
        //If user clicked space
        // add tutorial_0 event to queue
        // add tutorial_0 UI event to queue
        // else if tutorial_0 event exists
        //if tutorial_1 event does not exist
        //if user clicked RIGHT
        //add tutorial_1 event to queue
        //add tutorial_1 UI event to queue
        //else  if user did not click RIGHT
        //add UI event indicating that the user should click RIGHT
        //else if tutorial_1 event exists
        //if tutorial_2 event does not exist
        //if user clicked LEFT
        //add tutorial_2 event to queue
        //add tutorial_2 UI event to queue
        //else if user did not click LEFT
        //add UI event indicating that the user should click RIGHT
        //else if tutorial_2 event exists
        //if tutorial_3 event does not exist
        //if user clicked FLIP
        //add tutorial_3 event to queue
        //add tutorial_3 UI event to queue
        //if tutorial_3 event exists
        // if tutorial_0 event does not exist


        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_0")) {
            //If user clicked space
            if (uaAction.getActionType().equals("flip")) {
                // add tutorial_0 event to queue
                gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_0"));
                // add tutorial_0 UI event to queue
                gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_0_UI", null, 0, true));
            }
            // else if tutorial_0 event exists
        } else {
            //if tutorial_1 event does not exist
            if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_1_STEP_1")) {
                //if user clicked RIGHT
                if (uaAction.getDirection().equals("RIGHT")) {
                    //add tutorial_1 event to queue
                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_1_STEP_1"));
                    //add tutorial_1 UI event to queue
                    gsCurrentState.getEventQueue().add(new GameEvent("GO_RIGHT_AGAIN", null, new Date().getTime() + 400, true));
                } //else  if user did not click RIGHT
                else {
                    gsCurrentState.getEventQueue().add(new GameEvent("NOT_RIGHT_UI", null, new Date().getTime() + 200, false));
                }
                //else if tutorial_1 event exists
            } else {
                if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_1_STEP_2")) {
                    //if user clicked RIGHT
                    if (uaAction.getDirection().equals("RIGHT")) {
                        //add tutorial_1 event to queue
                        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_1_STEP_2"));
                    } //else  if user did not click RIGHT
                    else {
                        gsCurrentState.getEventQueue().add(new GameEvent("NOT_RIGHT_UI", null, new Date().getTime() + 200, false));
                    }
                    //else if tutorial_1 event exists
                } else {
                    //if tutorial_2 event does not exist
                    if(eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_INVALID_MOVEMENT")) {
                        // if the invalid movement event was handled by the rendering engine
                        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_INVALID_MOVEMENT_UI")) {
                            if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_2")) {
                                //if user clicked LEFT
                                if (uaAction.getDirection().equals("LEFT")) {
                                    // add tutorial_2 UI event to queue
                                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_2_UI", null, new Date().getTime() + 500, true));
                                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_2"));

                                } else {
                                    //add UI event indicating that the user should click LEFT
                                    gsCurrentState.getEventQueue().add(new GameEvent("NOT_LEFT_UI", null, new Date().getTime() + 200, true));
                                }
                            } else {
                                //if tutorial_3 event does not exist
                                if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "DOORS_EXPLANATION")) {
                                    //if user clicked ENTER
                                    if (uaAction.getActionType().equals("enter")) {
                                        //add tutorial_3 event to queue
                                        gsCurrentState.getEventQueue().add(new GameEvent("DOORS_EXPLANATION"));
                                        // add tutorial_3 UI event to queue
                                        gsCurrentState.getEventQueue().add(new GameEvent("DOORS_EXPLANATION_UI", null, new Date().getTime() + 200, true));
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private void handleLevelFinishGameEvents(MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_END_GAME_UI", null, new Date().getTime() + 6500, false));
        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_END_GAME"));
    }
}
