package org.scify.memori.rules;

import org.scify.memori.*;
import org.scify.memori.interfaces.*;

import java.util.ArrayList;
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
            return super.getNextState(gsCurrent, uaAction);
        }

        if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "GAME_STARTED")) {
            super.handleGameStartingGameEvents(gsCurrentState);
            gsCurrentState.getEventQueue().add(new GameEvent("GAME_STARTED"));
        } else {
            if(isOpponentPlaying(gsCurrentState)) {
                handleOpponentNextMove(gsCurrentState);
            } else {
                opponentMoveFactory.updateFactoryComponents();
                return super.getNextState(gsCurrent, uaAction);
            }
        }

        return gsCurrentState;
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
                super.handleUserActionGameEvents(movement, gsCurrentState, currentStep * opponentMoveFactory.getMovementDelay());
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
        super.handleUserActionGameEvents(opponentMoveFactory.getUserFlip(), gsCurrentState);
    }
}
