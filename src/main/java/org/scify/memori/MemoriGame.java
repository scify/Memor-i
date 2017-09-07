
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

import com.google.gson.Gson;
import org.scify.memori.card.CategorizedCard;
import org.scify.memori.card.MemoriCardService;
import org.scify.memori.interfaces.*;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.ServerOperationResponse;
import org.scify.memori.rules.MultiPlayerRules;
import org.scify.memori.rules.SinglePlayerRules;
import org.scify.memori.rules.TutorialRules;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public abstract class MemoriGame implements Game<GameEndState> {

    protected Rules rRules;
    /**
     * Object responsible for UI events (User actions)
     */
    protected UI uInterface;
    /**
     * Object rensponsible for UI rendering events (sounds, graphics etc)
     */
    protected RenderingEngine reRenderer;

    protected Map<CategorizedCard, Point2D> givenGameCards = new HashMap<>();

    protected GameType gameType;

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public GameType getGameType() {
        return gameType;
    }

    @Override
    /**
     * Subclasses should initialize a UI
     */
    public void initialize(GameLevel gameLevel) {
        setUpRules(gameLevel);
    }

    public void initialize(Map<CategorizedCard, Point2D> givenGameCards, GameLevel gameLevel) {
        this.givenGameCards = givenGameCards;
        setUpRules(gameLevel);
    }

    private void setUpRules(GameLevel gameLevel) {
        if(isTutorial())
            rRules = new TutorialRules(gameLevel);
        else if(isSinglePlayer()) {
            rRules = new SinglePlayerRules(gameLevel);
        } else {
            rRules = new MultiPlayerRules(gameLevel, gameType);
        }
    }

    @Override
    public GameEndState call() {
        final GameState gsInitialState;
        if(givenGameCards.size() == 0)
            gsInitialState = rRules.getInitialState(gameType);
        else
            gsInitialState = rRules.getInitialState(givenGameCards, gameType);
        // Initialize UI layout
        reRenderer.drawGameState(gsInitialState);
        // Send cards to server if online game
        GameState gsCurrentState = gsInitialState;
        if(gameType.equals(GameType.VS_PLAYER) && PlayerManager.localPlayerIsInitiator) {
            sendShuffledDeckToServer((MemoriGameState)gsInitialState);
        }
        // For every cycle
        while (!rRules.isGameFinished(gsCurrentState)) {
            gsCurrentState = doGameLoop(gsCurrentState);
        }
        // the final game state will contain the user input relevant to the future of the game
        // the user can select to
        // a) end the game and return to the main screen
        // b) play the same level again
        // c) go to the next level
        MemoriGameState memoriGameState = (MemoriGameState) gsCurrentState;
        reRenderer.cancelCurrentRendering();
        return handleEndGame(memoriGameState);
    }

    private GameEndState handleEndGame(MemoriGameState memoriGameState) {
        if(memoriGameState.loadNextLevel) {
            if(!isTutorial())
                MainOptions.STORY_LINE_LEVEL++;
            if(MainOptions.GAME_LEVEL_CURRENT < MainOptions.MAX_NUM_OF_LEVELS)
                return GameEndState.NEXT_LEVEL;
            else
                return GameEndState.GAME_FINISHED;
        }
        else if(memoriGameState.replayLevel) {
            if(!isTutorial())
                MainOptions.STORY_LINE_LEVEL++;
            return GameEndState.SAME_LEVEL;
        }
        else
            return GameEndState.GAME_FINISHED;
    }

    private GameState doGameLoop(GameState gsCurrentState) {
        final GameState toHandle = gsCurrentState;
        // Ask to draw the state
        reRenderer.drawGameState(toHandle);
        // and keep on doing the loop in this thread
        //get next user action
        UserAction uaToHandle = uInterface.getNextUserAction(gsCurrentState.getCurrentPlayer());

        //apply it and determine the next state
        gsCurrentState = rRules.getNextState(gsCurrentState, uaToHandle);
        try {
            Thread.sleep(50L); // Allow repainting
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return gsCurrentState;
    }

    @Override
    public void finalize() {
        if(gameType.equals(GameType.VS_PLAYER))
            markOnlineGameAsFinished();
        System.err.println("FINALIZE");
    }

    private void sendShuffledDeckToServer(MemoriGameState initialGameState) {
        MemoriTerrain terrain = (MemoriTerrain) initialGameState.getTerrain();
        MemoriCardService cardService = new MemoriCardService();
        String JSONRepresentationOfTiles = cardService.terrainTilesToJSON(terrain.getTiles());
        GameRequestManager gameRequestManager = new GameRequestManager();
        String serverResponse = gameRequestManager.sendShuffledDeckToServer(JSONRepresentationOfTiles);
        if(serverResponse != null) {
            parseServerResponse(serverResponse);
        }
    }

    private void parseServerResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        String responseParameters;
        switch (code) {
            case 1:
                // Cards sent successfully
                break;
            case 2:
                // Error in creating game request
                responseParameters = (String) response.getParameters();
                System.err.println("ERROR: " + responseParameters);
                break;
            case 3:
                // Error in server validation rules
                responseParameters = (String) response.getParameters();
                System.err.println("VALIDATION ERROR: " + responseParameters);
                break;
            default:
                break;
        }
    }

    private void markOnlineGameAsFinished() {
        GameRequestManager gameRequestManager = new GameRequestManager();
        gameRequestManager.endGame();
    }

    public boolean isTutorial() {
        return gameType.equals(GameType.TUTORIAL);
    }

    public boolean isSinglePlayer() {
        return gameType.equals(GameType.SINGLE_PLAYER);
    }
}
