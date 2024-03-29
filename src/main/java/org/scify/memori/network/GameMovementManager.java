package org.scify.memori.network;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.PlayerManager;
import org.scify.memori.interfaces.UserAction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class GameMovementManager {

    public static final int MAX_REQUEST_TRIES_FOR_MOVEMENT = 100;
    private RequestManager requestManager;
    private static long timestampOfLastOpponentMovement;
    protected Semaphore sMoveRequest = new Semaphore(1);

    public static void setTimestampOfLastOpponentMovement(long timestampOfLastOpponentMovement) {

        GameMovementManager.timestampOfLastOpponentMovement = timestampOfLastOpponentMovement;
    }

    public GameMovementManager() {
        requestManager = new RequestManager();
    }

    public String sendMovementToServer(String movementJson, long userActionTimestamp) {
        System.err.println("sending movement to server: " + movementJson);
        String url = "gameMovement/create";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(GameRequestManager.getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(PlayerManager.getPlayerId())));
        urlParameters.add(new BasicNameValuePair("timestamp", String.valueOf(userActionTimestamp)));
        urlParameters.add(new BasicNameValuePair("movement_json", movementJson));
        return this.requestManager.doPost(url, urlParameters);
    }

    public boolean callOngoing() {
        return sMoveRequest.availablePermits() == 0;
    }

    public UserAction getNextMovementFromServer() throws Exception {
        // Use semaphore to ascertain that the call is served before another
        sMoveRequest.acquire();

        String url = "gameMovement/latest";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(GameRequestManager.getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("opponent_id", String.valueOf(PlayerManager.getOpponentPlayer().getId())));
        urlParameters.add(new BasicNameValuePair("last_timestamp", String.valueOf(timestampOfLastOpponentMovement)));
        System.out.println(url);
        System.out.println(urlParameters.get(0).getValue());
        System.out.println(urlParameters.get(1).getValue());
        System.out.println(urlParameters.get(2).getValue());
        String serverResponse = this.requestManager.doPost(url, urlParameters);
        // Call served. Release semaphore.
        sMoveRequest.release();
        // TODO if post fails retry
        return parseGameRequestResponse(serverResponse);
    }

    private UserAction parseGameRequestResponse(String serverResponse) throws Exception {
        if(serverResponse == null)
            return null;
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        switch (code) {
            case ServerResponse.RESPONSE_SUCCESSFUL:
                // game request was either accepted or rejected
                LinkedTreeMap parameters = (LinkedTreeMap) response.getParameters();
                LinkedTreeMap movement = (LinkedTreeMap) parameters.get("game_movement_json");
                Object timestampObj = movement.get("timestamp");
                String timestampStr = timestampObj.toString();
                long timestamp = Double.valueOf(Double.parseDouble(timestampStr)).longValue();
                return new UserAction(movement.get("actionType").toString(), movement.get("direction").toString(), timestamp);
            case ServerResponse.RESPONSE_ERROR:
                // error
                return null;
            case ServerResponse.VALIDATION_ERROR:
                // server validation not passed
                return null;
            case ServerResponse.OPPONENT_OFFLINE:
                // opponent went offline.
                // throw an Exception to let rules know that the game Ended.
                throw new Exception("Opponent offline!");
        }
        return null;
    }
}
