

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

package org.scify.memori.rules;

import javafx.util.Pair;
import org.scify.memori.*;
import org.scify.memori.card.CategorizedCard;
import org.scify.memori.enums.GameType;
import org.scify.memori.interfaces.*;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.Observable;

/**
 * Integral part of the Game Engine. This class is used to evaluate every User event of the game and
 * to also prepare the {@link GameState} object to be rendered at every cycle
 */
public class MemoriRules extends Observable implements Rules {

    GameLevel currentGameLevel;

    @Override
    public GameState getInitialState(GameType gameType) {
        return new MemoriGameState(currentGameLevel, gameType);
    }

    public MemoriRules(GameLevel currentGameLevel) {
        this.currentGameLevel = currentGameLevel;
    }

    @Override
    public GameState getInitialState(Map<CategorizedCard, Point2D> givenGameCards, GameType gameType) {
        return new MemoriGameState(givenGameCards, currentGameLevel, gameType);
    }

    @Override
    public GameState getNextState(GameState gsCurrent, UserAction uaAction) {
        return gsCurrent;
    }

    /**
     * If the events queue contains a blocking event it means that this event has not been handled by the rendering engine
     * @param gsCurrentState the current state
     * @return true if the events queue contains a blocking event
     */
    protected boolean eventQueueContainsBlockingEvent(MemoriGameState gsCurrentState) {
        //Iterate through game events. If there is a blocking event, return.
        for (GameEvent currentGameEvent : gsCurrentState.getEventQueue()) {
            if (currentGameEvent.blocking) {
                return true;
            }
        }
        return false;
    }

    protected void removeEventFromQueue(MemoriGameState gsCurrentState, String eventCode) {
        gsCurrentState.getEventQueue().removeIf(currentGameEvent -> currentGameEvent.type.equals(eventCode));
    }

    /**
     * If the level is over the rules should add the relevant game events
     * @param uaAction the user action (flip, move, help)
     * @param gsCurrentState the current game state
     */
    protected void handleLevelFinishGameEvents(UserAction uaAction, MemoriGameState gsCurrentState) {
        if(eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "READY_TO_FINISH")) {
            if(uaAction != null) {
                //listen for user action indicating game over
                if(uaAction.getActionType().equals("enter")) {
                    //the game should finish and load a next level
                    gsCurrentState.replayLevel = true;
                    gsCurrentState.setGameFinished(true);
                }
                if(uaAction.getActionType().equals("flip")) {
                    //the game should finish and load a next level
                    gsCurrentState.setLoadNextLevel(true);
                    gsCurrentState.setGameFinished(true);
                }
            }
        } else {
            gsCurrentState.getEventQueue().add(new GameEvent("READY_TO_FINISH"));
        }
    }

    protected GameEvent invalidMovementGameEvent(MemoriGameState gameState) {
        return new GameEvent("INVALID_MOVEMENT_UI", new Point2D.Double(gameState.getRowIndex(), gameState.getColumnIndex()), 0 , false);
    }

    protected void updateGameStateIndexesAndUserActionCoords(UserAction uaAction, MemoriGameState gsCurrentState) {
        gsCurrentState.updateRowIndex(uaAction.getDirection());
        gsCurrentState.updateColumnIndex(uaAction.getDirection());

        uaAction.setCoords(new Point2D.Double(gsCurrentState.getRowIndex(), gsCurrentState.getColumnIndex()));
    }

    protected void movementUI(UserAction uaAction, MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("movement", uaAction.getCoords()));
    }

    protected void emptySoundUI(MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("EMPTY"));
    }

    protected void cardSoundUI(UserAction uaAction, MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("CARD_SOUND_UI", uaAction.getCoords(), 0, true));
    }

    protected void notifyObserversForTileFlip(UserAction uaAction, Tile currTile) {
        setChanged();
        notifyObservers(new RuleObserverObject(new Pair<>(uaAction, currTile), "TILE_REVEALED"));
    }

    protected void notifyObserversForPlayerMovement(UserAction uaAction) {
        setChanged();
        notifyObservers(new RuleObserverObject(uaAction, "PLAYER_MOVE"));
    }

    protected void successUI(UserAction uaAction, MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("SUCCESS_UI", uaAction.getCoords(), new Date().getTime() + 5000, true));
    }

    protected void cardDescriptionSoundUI(MemoriGameState gsCurrentState) {
        String cardDescriptionSoundFilePath = cardDescriptionSoundFromOpenCardsByChance((MemoriTerrain) gsCurrentState.getTerrain());
        if(cardDescriptionSoundFilePath != null)
            gsCurrentState.getEventQueue().add(new GameEvent("CARD_DESCRIPTION", cardDescriptionSoundFilePath, new Date().getTime() + 6500, true));
    }

    protected void flipTileUI(UserAction uaAction, MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("TURN_ANIMATION", uaAction.getCoords()));
        gsCurrentState.getEventQueue().add(new GameEvent("FLIP_UI", uaAction.getCoords(), new Date().getTime() + 1000, false));
        gsCurrentState.getEventQueue().add(new GameEvent("FLIP_SECOND_UI", uaAction.getCoords(), new Date().getTime() + 3000, false));
        gsCurrentState.getEventQueue().add(new GameEvent("DOOR_OPEN", uaAction.getCoords(), 0, true));
        gsCurrentState.getEventQueue().add(new GameEvent("CARD_SOUND_UI", uaAction.getCoords(), new Date().getTime() + 1800, false));
    }

    protected void updateGameStateAndNextTurn(Tile currTile, MemoriGameState gsCurrentState, MemoriTerrain memoriTerrain) {
        // add tile to open tiles
        memoriTerrain.addTileToOpenTiles(currTile);
        // set all open cards won
        setAllOpenTilesWon(memoriTerrain);
        //reset open tiles
        memoriTerrain.resetOpenTiles();
        gsCurrentState.getCurrentPlayer().setScore(gsCurrentState.getCurrentPlayer().getScore() + 1);
        nextTurn(gsCurrentState);
    }

    protected void flipBackTileAndAddToOpenCards(Tile currTile, MemoriGameState gsCurrentState, MemoriTerrain memoriTerrain) {
        memoriTerrain.addTileToOpenTiles(currTile);
        // Reset all tiles
        List<Point2D> openTilesPoints = resetAllOpenTiles(memoriTerrain);
        memoriTerrain.resetOpenTiles();
        for (Iterator<Point2D> iter = openTilesPoints.iterator(); iter.hasNext(); ) {
            Point2D position = iter.next();
            gsCurrentState.getEventQueue().add(new GameEvent("FLIP_BACK_UI", position, new Date().getTime() + 5500, false));
        }
        gsCurrentState.getEventQueue().add(new GameEvent("STOP_AUDIOS", null, new Date().getTime() + 5000, true));

    }

    protected void doorsShuttingUI(MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("DOORS_SHUTTING", null, new Date().getTime() + 5000, true));
    }

    /**
     * Creates the help ui events
     * @param uaAction the user action
     * @param gsCurrentState the current game state
     */
    protected void createHelpGameEvent(UserAction uaAction, MemoriGameState gsCurrentState) {
        Point2D coords = uaAction.getCoords();
        gsCurrentState.getEventQueue().add(new GameEvent("LETTER", (int)coords.getX() + 1, 0, true));
        gsCurrentState.getEventQueue().add(new GameEvent("NUMERIC",  (int)coords.getY() + 1, 0, true));
        //If in help instructions mode, add appropriate explanation
        if (!eventsQueueContainsEvent(gsCurrentState.getEventQueue(), "HELP_INSTRUCTIONS_EXPLANATION")) {
            gsCurrentState.getEventQueue().add(new GameEvent("HELP_INSTRUCTIONS_EXPLANATION"));
            gsCurrentState.getEventQueue().add(new GameEvent("HELP_EXPLANATION_ROW", null, 0, true));
            gsCurrentState.getEventQueue().add(new GameEvent("LETTER", (int) coords.getX() + 1, 0, true));
            gsCurrentState.getEventQueue().add(new GameEvent("HELP_EXPLANATION_COLUMN", null, 0, true));
            gsCurrentState.getEventQueue().add(new GameEvent("NUMERIC", (int) coords.getY() + 1, 0, true));

        }
    }

    protected void levelEndUserActions(MemoriGameState gsCurrentState) {
        if(MainOptions.GAME_LEVEL_CURRENT < MainOptions.MAX_NUM_OF_LEVELS) {
            gsCurrentState.getEventQueue().add(new GameEvent("LEVEL_END_UNIVERSAL", null, new Date().getTime() + 8600, true));
        } else {
            gsCurrentState.getEventQueue().add(new GameEvent("GAME_END", null, new Date().getTime() + 8600, true));
            gsCurrentState.getEventQueue().add(new GameEvent("PRESS_EXIT", null, new Date().getTime() + 8600, true));
        }
    }

    protected void pressExitUI(MemoriGameState gsCurrentState) {
        gsCurrentState.getEventQueue().add(new GameEvent("PRESS_EXIT", null, new Date().getTime() + 8600, true));
    }

    protected void nextTurn(MemoriGameState gsCurrentState) {
        for(Player player: gsCurrentState.getPlayers()) {
            System.err.println("Score for " + player.getName() + ": " + player.getScore());
        }
        gsCurrentState.incrementTurn();
    }


    private String cardDescriptionSoundFromOpenCardsByChance(MemoriTerrain memoriTerrain) {

        CategorizedCard tileToCard;
        // For each open tile
        for (Tile element : memoriTerrain.getOpenTiles()) {
            // Get the current tile
            tileToCard = (CategorizedCard) element;
            // if the card has a description sound file path set
            if (tileToCard.getDescriptionSound() != null || !tileToCard.getDescriptionSound().equals("")) {
                // we need to check the probability of this sound playing.
                // every card that has a description sound, has a probability (integer 1- 100)
                // describing the percentage probability of this sound playing.
                // for example, a card with a description sound probability of 100 (100 %)
                // will always get it's description sound playing.
                // that is why we multiply by 0.01, to transform the percentage into a floating point number
                // to be in compliance with the Math.random function of Java that returns floating point numbers
                if(Math.random() < tileToCard.getCardDescriptionSoundProbability() * 0.01){
                    return tileToCard.getDescriptionSound();
                }

            }
        }
        return null;
    }

    /**
     * Checks if a given event exists in the events list
     * @param eventQueue the events list
     * @param eventType the type of the event
     * @return true if the event exists in the events list
     */
    protected boolean eventsQueueContainsEvent(Queue<GameEvent> eventQueue, String eventType) {
        Iterator<GameEvent> iter = eventQueue.iterator();
        GameEvent currentGameEvent;
        while (iter.hasNext()) {
            currentGameEvent = iter.next();
            if(currentGameEvent.type.equals(eventType))
                return true;
        }
        return false;
    }

    /**
     * Checks if the current round is the last one (if all the tiles are won)
     * @param gsCurrent the current game state
     * @return true if the current round is the last one
     */
    protected boolean isLastRound(GameState gsCurrent) {
        return ((MemoriGameState)gsCurrent).areAllTilesWon();
    }

    /**
     * Sets the tuple as won
     * @param memoriTerrain the terrain containing the open tiles tuple
     */
    private void setAllOpenTilesWon(MemoriTerrain memoriTerrain) {
        for (Tile openTile : memoriTerrain.getOpenTiles()) {
            openTile.setWon();
        }
    }

    /**
     * Checks if at least one of the currently open (but not won) tiles is different that the current tile
     * @param memoriTerrain the terrain holding all the tiles
     * @param currTile the current tile
     * @return true if one of the open tiles is different from the current tile
     */
    protected boolean atLeastOneOtherTileIsDifferent(MemoriTerrain memoriTerrain, Tile currTile) {
        CategorizedCard tileToCard = (CategorizedCard)currTile;
        boolean answer = false;
        for (Tile element : memoriTerrain.getOpenTiles()) {
            CategorizedCard elementToCard = (CategorizedCard) element;
            // if the current card is not equal with the given card
            if (!cardsAreEqual(elementToCard, tileToCard))
                answer = true;
        }
        return answer;
    }

    /**
     * Checks if 2 given {@link CategorizedCard}s are equal (must be of different categories but be in the same equivalence card set)
     * @param card1 the first {@link CategorizedCard}
     * @param card2 the second {@link CategorizedCard}
     * @return true if the 2 given cards are equal
     */
    protected boolean cardsAreEqual(CategorizedCard card1, CategorizedCard card2) {
        return !(card1.getCategory().equals(card2.getCategory())) && card1.getEquivalenceCardSetHashCode().equals(card2.getEquivalenceCardSetHashCode());
    }

    /**
     * Checks if the current tile is the last of the n-tuple
     * @param memoriTerrain the terrain holding all the tiles
     * @param currTile the current tile
     * @return true if the current tile is the last of the n-tuple
     */
    protected boolean tileIsLastOfTuple(MemoriTerrain memoriTerrain, Tile currTile) {
        boolean answer = false;
        // if all cards in the open cards tuple are equal and we have reached the end of the tuple (2-cards, 3-cards etc)
        if(!atLeastOneOtherTileIsDifferent(memoriTerrain, currTile) && (memoriTerrain.getOpenTiles().size() == MainOptions.NUMBER_OF_OPEN_CARDS - 1))
            answer = true;
        return answer;
    }

    protected void flipTile(Tile currTile) {
        currTile.flip();
    }

    @Override
    public boolean isGameFinished(GameState gsCurrent) {
        MemoriGameState memoriGameState = (MemoriGameState)gsCurrent;
        return memoriGameState.isGameFinished();
    }

    protected boolean isTileFlipped(Tile tile) {
        return tile.getFlipped();
    }

    protected boolean isTileWon(Tile tile) {
        return tile.getWon();
    }

    /**
     * When the user opens a tile that does not belong to the currently n-tuple of tiles
     * the round is failed and all the open tiles should be flipped back
     * @param memoriTerrain the terrain holding all the tiles
     * @return a list containing the coordinates of the open tiles
     */
    private List<Point2D> resetAllOpenTiles(MemoriTerrain memoriTerrain) {
        List<Point2D> openTilesPoints = new ArrayList<>();
        memoriTerrain.getOpenTiles().forEach(Tile::flip);
        for (Tile element : memoriTerrain.getOpenTiles()) {
            for (Object o : memoriTerrain.getTiles().entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (element == pair.getValue()) {
                    Point2D pos = (Point2D) pair.getKey();
                    openTilesPoints.add(new Point2D.Double(pos.getY(), pos.getX()));
                }
            }
        }

        return openTilesPoints;
    }

    /**
     * Determines whether the user move was valid
     * @param direction the direction (action by the user)
     * @param memoriGameState the current game state
     * @return true if the user move was valid
     */
    protected boolean movementValid(String direction, MemoriGameState memoriGameState) {
        switch(direction) {
            case "LEFT":
                if(memoriGameState.getColumnIndex() == 0) {
                    return false;
                }
                break;
            case "RIGHT":
                if(memoriGameState.getColumnIndex() == currentGameLevel.getDimensions().getY() - 1) {
                    return false;
                }
                break;
            case "UP":
                if(memoriGameState.getRowIndex() == 0) {
                    return false;
                }
                break;
            case "DOWN":
                if(memoriGameState.getRowIndex() == currentGameLevel.getDimensions().getX() - 1) {
                    return false;
                }
                break;
        }
        return true;
    }

    protected boolean gameWasTie(MemoriGameState gsCurrentState, Player playerWithMaxScore) {
        for(Player player: gsCurrentState.getPlayers()) {
            if(player.getScore() == playerWithMaxScore.getScore() && !playerWithMaxScore.equals(player))
                return true;
        }
        return false;
    }

    protected Player getWinnerPlayer(MemoriGameState gsCurrentState) {
        Player playerWithMaxScore = new Player("default");
        for(Player player: gsCurrentState.getPlayers()) {
            System.err.println("Score for " + player.getName() + ": " + player.getScore());
            if(player.getScore() > playerWithMaxScore.getScore())
                playerWithMaxScore = player;
        }
        return playerWithMaxScore;
    }

}
