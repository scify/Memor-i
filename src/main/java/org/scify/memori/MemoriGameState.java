
/**
 * Copyright 2016 SciFY.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scify.memori;

import org.scify.memori.card.CategorizedCard;
import org.scify.memori.enums.GameType;
import org.scify.memori.interfaces.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MemoriGameState implements GameState {
    /**
     * The terrain holding all the UI elements
     */
    protected Terrain terrain;
    protected ArrayList<Player> players;
    protected Player currentPlayer;
    protected int iCurrentTurn;
    /**
     * A queue containing the game events (handled by the renderer Class)
     */
    Queue<GameEvent> gameEventQueue;
    /**
     * Variable to flag a finished game
     */
    boolean gameFinished = false;
    /**
     * Variable to indicate whether a new game should start immediately
     */
    boolean loadNextLevel = false;
    /**
     * Variable to indicate whether the current lavel should replay
     */
    public boolean replayLevel;
    /**
     * indexes defining the user poistion on the GridPane
     */
    private int columnIndex = 0;
    private int rowIndex = 0;
    public boolean gameInterrupted = false;

    public MemoriGameState(GameLevel gameLevel, GameType gameType) {
        terrain = new MemoriTerrain(gameLevel.getDimensions());
        setUpPlayers(gameType);
    }

    public MemoriGameState(Map<CategorizedCard, Point2D> givenGameCards, GameLevel gameLevel, GameType gameType) {
        terrain = new MemoriTerrain(givenGameCards, gameLevel.getDimensions());
        setUpPlayers(gameType);
    }

    private void setUpPlayers(GameType gameType) {
        players = new ArrayList<>();

        Player player1 = new Player("player1");
        players.add(player1);
        PlayerManager.setLocalPlayer(player1);

        switch (gameType) {
            case SINGLE_PLAYER:
                currentPlayer = player1;
                break;
            case VS_CPU:
                Player player2 = new Player("player2");
                PlayerManager.setOpponentPlayer(player2);
                players.add(player2);
                currentPlayer = player1;
                break;
            case VS_PLAYER:
                Player onlinePlayer = PlayerManager.getOpponentPlayer();
                players.add(onlinePlayer);
                currentPlayer = PlayerManager.getStartingPlayer();
            break;
        }

        iCurrentTurn = 0;
        gameEventQueue = new LinkedList();
    }

    /**
     * Updates the column index based on the Key event passed
     * @param direction the direction of the user action
     */
    public void updateColumnIndex(String direction) {
        switch(direction) {
            case "LEFT":
                columnIndex--;
                break;
            case "RIGHT":
                columnIndex++;
                break;
            default: break;
        }
    }

    /**
     * Updates the row index based on the Key event passed
     * @param direction the direction of the user action
     */
    public void updateRowIndex(String direction) {

        switch(direction) {
            case "UP":
                rowIndex--;
                break;
            case "DOWN":
                rowIndex++;
                break;
            default: break;
        }
    }


    public int getColumnIndex() {

        return columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Get the terrain that the UI elements are laid on
     */
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Getter for the game events queue
     * @return A LinkedList representation of the game events queue
     */
    @Override
    public LinkedList<GameEvent> getEventQueue() {
        return (LinkedList)gameEventQueue;
    }


    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    /**
     * Empties the game events queue
     */
    @Override
    public void resetEventsQueue() {
        gameEventQueue = new LinkedList();
    }

    public void incrementTurn() {
        this.iCurrentTurn++;
        for(Player player: players) {
            if(!player.equals(currentPlayer)) {
                currentPlayer = player;
                break;
            }
        }
    }

    public int getiCurrentTurn() {
        return this.iCurrentTurn;
    }

    /**
     * Checks if all tiles are in a won state
     * @return true if all tiles are won
     */
    public boolean areAllTilesWon() {
        return terrain.areAllTilesWon();
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished(boolean gameFinished) {

        this.gameFinished = gameFinished;
    }

    public void setLoadNextLevel(boolean loadNextLevel) {
        this.loadNextLevel = loadNextLevel;
    }

}
