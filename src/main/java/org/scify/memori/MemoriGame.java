
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
import org.scify.memori.card.MemoriCardService;
import org.scify.memori.interfaces.*;
import org.scify.memori.network.GameRequestManager;
import org.scify.memori.network.ServerOperationResponse;
import org.scify.memori.rules.MultiPlayerRules;
import org.scify.memori.rules.SinglePlayerRules;
import org.scify.memori.rules.TutorialRules;

public abstract class MemoriGame implements Game<Integer> {
    /**
     * constant defining whether the game is finished
     */
    private static final Integer GAME_FINISHED = 1;
    /**
     * constant defining whether the game should continue to next level
     */
    private static final Integer NEXT_LEVEL = 2;
    /**
     * constant defining whether the game should continue to next level
     */
    private static final Integer SAME_LEVEL = 3;
    Rules rRules;
    /**
     * Object responsible for UI events (User actions)
     */
    protected UI uInterface;
    /**
     * Object rensponsible for UI rendering events (sounds, graphics etc)
     */
    protected RenderingEngine reRenderer;

    @Override
    /**
     * Subclasses should initialize a UI
     */
    public void initialize() {
        if(MainOptions.TUTORIAL_MODE)
            rRules = new TutorialRules();
        else {
            if(MainOptions.GAME_TYPE == 1) {
                rRules = new SinglePlayerRules();
            } else {
                rRules = new MultiPlayerRules();
            }
        }
    }

    @Override
    public Integer call() {
        final GameState gsInitialState = rRules.getInitialState();
        reRenderer.drawGameState(gsInitialState); // Initialize UI layout
        // Run asyncronously
        GameState gsCurrentState = gsInitialState; // Init
        if(MainOptions.GAME_TYPE == 3) {
            sendShuffledDeckToServer((MemoriGameState)gsInitialState);
        }
        // For every cycle
        while (!rRules.isGameFinished(gsCurrentState)) {
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

        }
        // the final game state will contain the user input relevant to the future of the game
        // the user can select to
        // a) end the game and return to the main screen
        // b) play the same level again
        // c) go to the next level
        MemoriGameState memoriGameState = (MemoriGameState) gsCurrentState;

        reRenderer.cancelCurrentRendering();
        if(memoriGameState.loadNextLevel) {
            if(!MainOptions.TUTORIAL_MODE)
                MainOptions.storyLineLevel++;
            if(MainOptions.GAME_LEVEL_CURRENT < MainOptions.MAX_NUM_OF_LEVELS)
                return NEXT_LEVEL;
            else
                return GAME_FINISHED;
        }
        else if(memoriGameState.replayLevel) {
            if(!MainOptions.TUTORIAL_MODE)
                MainOptions.storyLineLevel++;
            return SAME_LEVEL;
        }
        else
            return GAME_FINISHED;
    }

    @Override
    public void finalize() {
        System.err.println("FINALIZE");
    }

    private void sendShuffledDeckToServer(MemoriGameState initialGameState) {
        MemoriTerrain terrain = (MemoriTerrain) initialGameState.getTerrain();
        MemoriCardService cardService = new MemoriCardService();
        String JSONRepresentationOfTiles = cardService.terrainTilesToJSON(terrain.getTiles());
        System.out.println(JSONRepresentationOfTiles);
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
                // Error
                responseParameters = response.getParameters().toString();
                System.err.println("ERROR: " + responseParameters);
                break;
            case 3:
                // Error in server validation rules
                responseParameters = (String) response.getParameters();
                System.err.println("ERROR: " + responseParameters);
                break;
            default:
                break;
        }
    }
}
