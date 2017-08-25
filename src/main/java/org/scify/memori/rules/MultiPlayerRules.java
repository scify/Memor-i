package org.scify.memori.rules;

import org.scify.memori.MainOptions;
import org.scify.memori.MemoriGameState;
import org.scify.memori.CPUMoveFactory;
import org.scify.memori.OnlineMoveFactory;
import org.scify.memori.interfaces.GameEvent;
import org.scify.memori.interfaces.GameState;
import org.scify.memori.interfaces.MoveFactory;
import org.scify.memori.interfaces.UserAction;
import java.util.ArrayList;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class MultiPlayerRules extends MemoriRules {

    private MoveFactory opponentMoveFactory;

    public MultiPlayerRules() {
        if (MainOptions.GAME_TYPE == 2) {
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

    protected boolean isOpponentPlaying(MemoriGameState memoriGameState) {
        return memoriGameState.getCurrentPlayer().getName().equals("player2");
    }

    private void handleOpponentNextMove(MemoriGameState gsCurrentState) {

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

    private boolean generateOpponentMove(MemoriGameState gsCurrentState) {

        ArrayList<UserAction> movements;
        movements = opponentMoveFactory.getNextUserMovements(gsCurrentState);

        int currentStep = 1;
        if(!movements.isEmpty()) {
            for (UserAction movement : movements) {
                currentStep++;
                super.handleUserActionGameEvents(movement, gsCurrentState, currentStep * 1000);
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
