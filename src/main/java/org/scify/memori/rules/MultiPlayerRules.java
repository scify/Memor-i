package org.scify.memori.rules;

import org.scify.memori.*;
import org.scify.memori.helper.TimeWatch;
import org.scify.memori.interfaces.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class MultiPlayerRules extends MemoriRules {

    private MoveFactory opponentMoveFactory;

    public MultiPlayerRules(GameLevel gameLevel, GameType gameType) {
        super(gameLevel);
        if (gameType.equals(GameType.VS_CPU)) {
            opponentMoveFactory = new CPUMoveFactory();
        } else {
            opponentMoveFactory = new OnlineMoveFactory();
        }
        this.addObserver((Observer) opponentMoveFactory);
    }

    public GameState getNextState(GameState gsCurrent, UserAction uaAction) {

        MemoriGameState gsCurrentState = (MemoriGameState)gsCurrent;
        //if there is a blocking game event currently being handled by the Rendering engine, return.
        if(eventQueueContainsBlockingEvent(gsCurrentState)) {
            return gsCurrentState;
        }

        if(isOpponentPlaying(gsCurrentState)) {
            handleOpponentNextMove(gsCurrentState);
        } else {
            opponentMoveFactory.updateFactoryComponents();
            if(uaAction != null) {
                handleUserActionMultiPlayerGameEvents(uaAction, gsCurrentState, 0);
            }
        }

        return gsCurrentState;
    }

    private void handleUserActionMultiPlayerGameEvents(UserAction uaAction, MemoriGameState gsCurrentState, int delay) {
        if(movementValid(uaAction.getDirection(), gsCurrentState)) {
            handleValidActionMultiPlayerEvent(uaAction, gsCurrentState, delay);
        } else {
            gsCurrentState.getEventQueue().add(invalidMovementGameEvent(gsCurrentState));
            notifyObserversForPlayerMovement(uaAction);
        }
    }

    private void handleValidActionMultiPlayerEvent(UserAction uaAction, MemoriGameState gsCurrentState, int delay) {
        updateGameStateIndexesAndUserActionCoords(uaAction, gsCurrentState);

        MemoriTerrain memoriTerrain = (MemoriTerrain) (gsCurrentState.getTerrain());
        // currTile is the tile that was moved on or acted upon
        Tile currTile = memoriTerrain.getTile(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex());
        if(uaAction.getActionType().equals("movement")) {
            movementUI(uaAction, gsCurrentState);
            notifyObserversForPlayerMovement(uaAction);
        } else if (uaAction.getActionType().equals("flip")) {
            performFlipMultiPlayer(currTile, gsCurrentState, uaAction, memoriTerrain);
            notifyObserversForPlayerMovement(uaAction);
        } else if(uaAction.getActionType().equals("enter")) {
            createHelpGameEvent(uaAction, gsCurrentState);
        } else if(uaAction.getActionType().equals("escape")) {
            //exit current game
            gsCurrentState.setGameFinished(true);
        } else if(uaAction.getActionType().equals("opponent_movement")) {
            gsCurrentState.getEventQueue().add(new GameEvent("movement", uaAction.getCoords(), new Date().getTime() + delay, true));
        }
    }

    /**
     * Applies the rules and creates the game events relevant to flipping a card
     * @param currTile the tile that the flip performed on
     * @param gsCurrentState the current game state
     * @param uaAction the user action object
     * @param memoriTerrain the terrain holding all the tiles
     */
    protected void performFlipMultiPlayer(Tile currTile, MemoriGameState gsCurrentState, UserAction uaAction, MemoriTerrain memoriTerrain) {
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
            notifyObserversForTileFlip(uaAction, currTile);
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

    private boolean isOpponentPlaying(MemoriGameState memoriGameState) {
        Player currentPlayer = memoriGameState.getCurrentPlayer();
        Player opponentPlayer = PlayerManager.getOpponentPlayer();
        return currentPlayer.equals(opponentPlayer);
    }

    private void opponentCPUNextMove(MemoriGameState gsCurrentState) {
        if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "OPPONENT_MOVEMENT_1")) {
            if(generateOpponentMove(gsCurrentState)) {
                gsCurrentState.getEventQueue().add(new GameEvent("OPPONENT_MOVEMENT_1"));
            }
        } else {
            if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "OPPONENT_FLIP_1")) {
                gsCurrentState.getEventQueue().add(new GameEvent("OPPONENT_FLIP_1"));
                generateOpponentFlip(gsCurrentState);
            } else {
                if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "OPPONENT_MOVEMENT_2")) {
                    if(generateOpponentMove(gsCurrentState)) {
                        gsCurrentState.getEventQueue().add(new GameEvent("OPPONENT_MOVEMENT_2"));
                    }
                } else {
                    if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "OPPONENT_FLIP_2")) {
                        gsCurrentState.getEventQueue().add(new GameEvent("OPPONENT_FLIP_2"));
                        generateOpponentFlip(gsCurrentState);
                        super.removeEventFromQueue(gsCurrentState, "OPPONENT_MOVEMENT_1" );
                        super.removeEventFromQueue(gsCurrentState, "OPPONENT_MOVEMENT_2" );
                        super.removeEventFromQueue(gsCurrentState, "OPPONENT_FLIP_1" );
                        super.removeEventFromQueue(gsCurrentState, "OPPONENT_FLIP_2" );
                    }
                }
            }
        }
    }

    private void opponentOnlineNextMove(MemoriGameState gsCurrentState) {
        generateOpponentMove(gsCurrentState);
    }

    private void handleOpponentNextMove(MemoriGameState gsCurrentState) {
        if(opponentMoveFactory instanceof CPUMoveFactory) {
            opponentCPUNextMove(gsCurrentState);
        } else {
            opponentOnlineNextMove(gsCurrentState);
        }
    }

    private boolean generateOpponentMove(MemoriGameState gsCurrentState) {

        ArrayList<UserAction> movements;
        movements = opponentMoveFactory.getNextUserMovements(gsCurrentState);

        int currentStep = 1;
        if(!movements.isEmpty()) {
            for (UserAction movement : movements) {
                currentStep++;
                handleUserActionMultiPlayerGameEvents(movement, gsCurrentState, currentStep * opponentMoveFactory.getMovementDelay());
            }
            return true;
        } else {
            return false;
        }
    }

    private void generateOpponentFlip(MemoriGameState gsCurrentState) {
        // delay before turning the tile
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handleUserActionMultiPlayerGameEvents(opponentMoveFactory.getUserFlip(), gsCurrentState, 1000);
    }
}
