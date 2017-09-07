package org.scify.memori.rules;

import org.scify.memori.*;
import org.scify.memori.interfaces.*;

import java.awt.geom.Point2D;
import java.util.Date;

/**
 * Class that contains the rules that integrate in the tutorial game.
 */
public class TutorialRules extends MemoriRules {

    public TutorialRules(GameLevel gameLevel) {
        super(gameLevel);
    }

    public GameState getInitialState(GameType gameType) {
        return super.getInitialState(gameType);
    }

    public GameState getNextState(GameState gsCurrent, UserAction uaAction) {

        MemoriGameState gsCurrentState = (MemoriGameState) gsCurrent;
        if(eventQueueContainsBlockingEvent(gsCurrentState)) {
            return gsCurrentState;
        }
        //gsCurrentState = (MemoriGameState) super.getNextState(gsCurrent, uaAction);
        handleGameStartingGameEvents(gsCurrentState);
        // handle the tutorial game events
        if(uaAction != null) {
            handleUserActionTutorialGameEvents(uaAction, gsCurrentState);
            tutorialRulesSet(gsCurrentState, uaAction);
        }
        if(isLastRound(gsCurrentState)) {
            //if ready to finish event already in events queue
            this.handleTutorialFinishGameEvents(uaAction, gsCurrentState);
        }
        return gsCurrentState;
    }

    private void handleUserActionTutorialGameEvents(UserAction uaAction, MemoriGameState gsCurrentState) {
        if(movementValid(uaAction.getDirection(), gsCurrentState)) {
            handleValidActionTutorialEvent(uaAction, gsCurrentState);
        } else {
            gsCurrentState.getEventQueue().add(invalidMovementGameEvent(gsCurrentState));
        }
    }

    private void handleValidActionTutorialEvent(UserAction uaAction, MemoriGameState gsCurrentState) {
        updateGameStateIndexesAndUserActionCoords(uaAction, gsCurrentState);

        MemoriTerrain memoriTerrain = (MemoriTerrain) (gsCurrentState.getTerrain());
        // currTile is the tile that was moved on or acted upon
        Tile currTile = memoriTerrain.getTile(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex());
        if(uaAction.getActionType().equals("movement")) {
            movementUI(uaAction, gsCurrentState);

        } else if (uaAction.getActionType().equals("flip")) {
            if (eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_2"))
                performFlipTutorial(currTile, gsCurrentState, uaAction, memoriTerrain);
        } else if(uaAction.getActionType().equals("enter")) {

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
    protected void performFlipTutorial(Tile currTile, MemoriGameState gsCurrentState, UserAction uaAction, MemoriTerrain memoriTerrain) {
        // Rule 6: flip
        // If target card flipped
        if(isTileFlipped(currTile)) {
            //if card won
            if(isTileWon(currTile)) {
                //play empty sound
                emptySoundUI(gsCurrentState);
            } else {
                //else if card not won
                // play card sound
                cardSoundUI(uaAction, gsCurrentState);
            }
        } else {
            // else if not flipped
            // flip card
            flipTile(currTile);
            flipTileUI(uaAction, gsCurrentState);
            if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "FLIP_EXPLANATION")) {
                //add FLIP_EXPLANATION event to queue
                gsCurrentState.getEventQueue().add(new GameEvent("FLIP_EXPLANATION"));
                // add FLIP_EXPLANATION_UI event to queue
                gsCurrentState.getEventQueue().add(new GameEvent("FLIP_EXPLANATION_UI", null, new Date().getTime() + 4000, true));

            }
            if(tileIsLastOfTuple(memoriTerrain, currTile)) {
                // If last of n-tuple flipped (i.e. if we have enough cards flipped to form a tuple)
                successUI(uaAction, gsCurrentState);
                //gsCurrentState.getEventQueue().add(new GameEvent("CARD_DESCRIPTION", uaAction.getCoords(), new Date().getTime() + 4500, true));
                cardDescriptionSoundUI(gsCurrentState);
                //if in tutorial mode, push explaining events
                if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_CORRECT_PAIR")) {
                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_CORRECT_PAIR"));
                    gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_CORRECT_PAIR_UI", null, new Date().getTime() + 5000, true));
                }
                updateGameStateAndNextTurn(currTile, gsCurrentState, memoriTerrain);
            } else {
                // else not last card in tuple
                if(atLeastOneOtherTileIsDifferent(memoriTerrain, currTile)) {
                    // Flip card back
                    flipBackTileAndAddToOpenCards(currTile, gsCurrentState, memoriTerrain);
                    if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_WRONG_PAIR")) {
                        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_WRONG_PAIR"));
                        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_WRONG_PAIR_UI", null, new Date().getTime() + 4600, true));
                    }
                    doorsShuttingUI(gsCurrentState);
                    if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_DOORS_CLOSED")) {
                        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_DOORS_CLOSED"));
                        gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_DOORS_CLOSED_UI", null, new Date().getTime() + 7000, true));
                    }


                    nextTurn(gsCurrentState);
                } else {
                    memoriTerrain.addTileToOpenTiles(currTile);
                }
            }

        }
    }

    protected void handleGameStartingGameEvents(MemoriGameState gsCurrentState) {
        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_INTRO")) {
            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INTRO"));
            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INTRO_UI", null, 0, true));
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
                    } else {
                        //the invalid movement tutorial event should be emitted only if the tutorial has reached a certain point (step 2 which is go right second time)

                        if(eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_1_STEP_2")) {
                            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INVALID_MOVEMENT_UI", new Point2D.Double(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex()), new Date().getTime() + 500, true));
                            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_INVALID_MOVEMENT"));
                        }
                    }
                }

            }
        }
    }

    private void handleTutorialFinishGameEvents(UserAction userAction, MemoriGameState gsCurrentState) {
        super.handleLevelFinishGameEvents(userAction, gsCurrentState);
        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "TUTORIAL_END_GAME")) {
            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_END_GAME_UI", null, new Date().getTime() + 6500, false));
            gsCurrentState.getEventQueue().add(new GameEvent("TUTORIAL_END_GAME"));
        }
    }
}
