package org.scify.memori.rules;

import org.scify.memori.*;
import org.scify.memori.enums.GameType;
import org.scify.memori.interfaces.*;
import org.scify.memori.network.GameRequestManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MultiPlayerRules extends MemoriRules {

    private MoveFactory opponentMoveFactory;
    private GameType gameType;
    private boolean gameInterrupted = false;

    public MultiPlayerRules(GameLevel gameLevel, GameType gameType) {
        super(gameLevel);
        this.gameType = gameType;
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
        try {
            handleMultiPlayerGameStartingGameEvents(gsCurrentState);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(isOpponentPlaying(gsCurrentState) && !isLastRound(gsCurrent) && !gameInterrupted) {
            handleOpponentNextMove(gsCurrentState);
            if(uaAction != null)
                handleUserActionDuringOpponentTurn(uaAction, gsCurrentState);
        } else {
            opponentMoveFactory.updateFactoryComponents();
            if(uaAction != null) {
                handleUserActionMultiPlayerGameEvents(uaAction, gsCurrentState, 0);
            }
        }
        if(isLastRound(gsCurrent)) {
            //if ready to finish event already in events queue
            this.handleMultiPlayerFinishGameEvents(uaAction, gsCurrentState);
        }
        return gsCurrentState;
    }

    private void handleUserActionDuringOpponentTurn(UserAction uaAction, MemoriGameState gsCurrentState) {
        if(uaAction.getActionType().equals("escape")) {
            exitGame(gsCurrentState);
        } else {
            gsCurrentState.getEventQueue().add(new GameEvent("NOT_YOUR_TURN", null, 0, false));
        }
    }


    private void exitGame(MemoriGameState gsCurrentState) {
        //exit current game
        if(!gsCurrentState.isGameFinished()) {
            gsCurrentState.gameInterrupted = true;
        }
        gsCurrentState.setGameFinished(true);
    }

    protected void handleMultiPlayerGameStartingGameEvents(MemoriGameState gsCurrentState) throws Exception {
        switch (gameType){
            case VS_CPU:
                playerVSCPUGameStartingGameEvents(gsCurrentState);
                break;
            case VS_PLAYER:
                playerVSPlayerGameStartingGameEvents(gsCurrentState);
                break;
            default:
                throw new Exception("Unsupported Game type: " + gameType.toString());
        }

    }

    protected void playerVSCPUGameStartingGameEvents(MemoriGameState gsCurrentState) {
        if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "GAME_VS_CPU_STARTED")) {
            gsCurrentState.getEventQueue().add(new GameEvent("GAME_VS_CPU_STARTED"));
            gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_DESCRIPTION", null, new Date().getTime(), true));
            gsCurrentState.getEventQueue().add(new GameEvent("CPU_INTRO_MESSAGE", null, new Date().getTime() + 1000, true));
            gsCurrentState.getEventQueue().add(new GameEvent("YOUR_TURN", null, new Date().getTime() + 2000, false));
        }
    }

    protected void playerVSPlayerGameStartingGameEvents(MemoriGameState gsCurrentState) {
        if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "GAME_VS_PLAYER_STARTED")) {
            gsCurrentState.getEventQueue().add(new GameEvent("GAME_VS_PLAYER_STARTED"));
            gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_DESCRIPTION", null, new Date().getTime(), true));
            if(PlayerManager.getLocalPlayer().equals(gsCurrentState.getCurrentPlayer()))
                gsCurrentState.getEventQueue().add(new GameEvent("LOCAL_PLAYER_IS_INITIATOR", null, new Date().getTime() + 1000, true));
            else
                gsCurrentState.getEventQueue().add(new GameEvent("OPPONENT_IS_INITIATOR", null, new Date().getTime() + 1000, true));
        }
    }

    private void handleUserActionMultiPlayerGameEvents(UserAction uaAction, MemoriGameState gsCurrentState, int delay) {
        if(movementValid(uaAction.getDirection(), gsCurrentState)) {
            handleValidActionMultiPlayerEvent(uaAction, gsCurrentState, delay);
        } else {
            gsCurrentState.getEventQueue().add(invalidMovementGameEvent(gsCurrentState));
            if(!isOpponentPlaying(gsCurrentState))
                notifyObserversForPlayerMovement(uaAction);
        }
    }

    private void handleValidActionMultiPlayerEvent(UserAction uaAction, MemoriGameState gsCurrentState, int delay) {
        updateGameStateIndexesAndUserActionCoords(uaAction, gsCurrentState);

        MemoriTerrain memoriTerrain = (MemoriTerrain) (gsCurrentState.getTerrain());
        // currTile is the tile that was moved on or acted upon
        Tile currTile = memoriTerrain.getTile(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex());
        if(uaAction.getActionType().equals("movement")) {
            if(gsCurrentState.getCurrentPlayer().equals(PlayerManager.getLocalPlayer()))
                notifyObserversForPlayerMovement(uaAction);
            movementUI(uaAction, gsCurrentState);
        } else if (uaAction.getActionType().equals("flip")) {
            if(!isOpponentPlaying(gsCurrentState))
                notifyObserversForPlayerMovement(uaAction);
            performFlipMultiPlayer(currTile, gsCurrentState, uaAction, memoriTerrain);
        } else if(uaAction.getActionType().equals("enter")) {
            createHelpGameEvent(uaAction, gsCurrentState);
        } else if(uaAction.getActionType().equals("opponent_movement")) {
            if(gsCurrentState.getCurrentPlayer().equals(PlayerManager.getOpponentPlayer()))
                gsCurrentState.getEventQueue().add(new GameEvent("movement", uaAction.getCoords(), new Date().getTime() + delay, true));
        } else if(uaAction.getActionType().equals("escape")) {
            exitGame(gsCurrentState);
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
                if(gameType.equals(GameType.VS_CPU))
                    roundWonGameEvents(gsCurrentState);
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

    private void roundWonGameEvents(MemoriGameState gsCurrentState) {
        Random rand = new Random();
        if (rand.nextDouble() < 0.65) {
            if (PlayerManager.getLocalPlayer().equals(gsCurrentState.getCurrentPlayer()))
                gsCurrentState.getEventQueue().add(new GameEvent("PLAYER_WON_ROUND_MESSAGE", null, new Date().getTime() + 7000, true));
            else
                gsCurrentState.getEventQueue().add(new GameEvent("CPU_WON_ROUND_MESSAGE", null, new Date().getTime() + 7000, true));
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
        try {
            movements = opponentMoveFactory.getNextUserMovement(gsCurrentState);
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
        catch (Exception e) {
            e.printStackTrace();
            gameInterrupted = true;
            GameRequestManager gameRequestManager = new GameRequestManager();
            Thread thread = new Thread(() -> gameRequestManager.cancelGame());
            thread.start();
            gsCurrentState.gameInterrupted = true;
            gsCurrentState.getEventQueue().add(new GameEvent("PLAYER_ABANDONED", null, new Date().getTime() + 1000, true));
            System.err.println("You won!");
            gsCurrentState.getEventQueue().add(new GameEvent("GAME_WON_VS_PLAYER", null, new Date().getTime() + 2000, true));
            opponentMoveFactory.terminateFactoryComponents();
        }
        return false;
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

    private void handleMultiPlayerFinishGameEvents(UserAction userAction, MemoriGameState gsCurrentState) {
        if(!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "READY_TO_FINISH_GAME")) {
            //add appropriate event
            gsCurrentState.getEventQueue().add(new GameEvent("READY_TO_FINISH_GAME"));
            Player winnerPlayer = getWinnerPlayer(gsCurrentState);
            if(gameWasTie(gsCurrentState, winnerPlayer)) {
                gameWasTieGameEvents(gsCurrentState);
            } else {
                winnerPlayerGameEvents(gsCurrentState, winnerPlayer);
            }
            GameRequestManager gameRequestManager = new GameRequestManager();
            gameRequestManager.endGame();
            multiPlayerGameEndUserActions(gsCurrentState);
            opponentMoveFactory.terminateFactoryComponents();
        } else {
            if(gameType.equals(GameType.VS_CPU))
                super.handleLevelFinishGameEvents(userAction, gsCurrentState);
        }
    }

    private void gameWasTieGameEvents(MemoriGameState gsCurrentState) {
        System.err.println("Game was a tie!");
        gsCurrentState.getEventQueue().add(new GameEvent("GAME_TIE", null, new Date().getTime() + 7500, true));
    }

    private void winnerPlayerGameEvents(MemoriGameState gsCurrentState, Player winnerPlayer) {
        System.err.println("WINNER: " + winnerPlayer.getName());
        boolean localPlayerWon = true;
        if(PlayerManager.getOpponentPlayer().equals(winnerPlayer))
            localPlayerWon = false;
        if(gameType.equals(GameType.VS_CPU)) {
            if(localPlayerWon) {
                System.err.println("You won!");
                gsCurrentState.getEventQueue().add(new GameEvent("GAME_WON_VS_CPU", null, new Date().getTime() + 7500, true));
            } else {
                System.err.println("CPU won!");
                gsCurrentState.getEventQueue().add(new GameEvent("GAME_LOST_VS_CPU", null, new Date().getTime() + 7500, true));
            }
        } else {
            if(localPlayerWon) {
                System.err.println("You won!");
                gsCurrentState.getEventQueue().add(new GameEvent("GAME_WON_VS_PLAYER", null, new Date().getTime() + 7500, true));
            } else {
                gsCurrentState.getEventQueue().add(new GameEvent("GAME_LOST_VS_PLAYER", null, new Date().getTime() + 7500, true));
            }
        }
    }

    private void multiPlayerGameEndUserActions(MemoriGameState gsCurrentState) {
        if(gameType.equals(GameType.VS_CPU)) {
            levelEndUserActions(gsCurrentState);
        } else {
            gsCurrentState.getEventQueue().add(new GameEvent("PRESS_ESCAPE_TO_QUIT", null, new Date().getTime() + 8600, true));
        }
    }
}
