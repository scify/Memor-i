package org.scify.memori.network;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.PlayerManager;
import org.scify.memori.interfaces.UserAction;

import java.util.ArrayList;
import java.util.List;

public class GameMovementManager {

    private RequestManager requestManager;
    private static long timestampOfLastOpponentMovement;

    public static long getTimestampOfLastOpponentMovement() {
        return timestampOfLastOpponentMovement;
    }

    public static void setTimestampOfLastOpponentMovement(long timestampOfLastOpponentMovement) {

        GameMovementManager.timestampOfLastOpponentMovement = timestampOfLastOpponentMovement;
    }

    public GameMovementManager() {
        requestManager = new RequestManager();
    }

    public String sendMovementToServer(String movementJson) {
        String url = "gameMovement/create";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(GameRequestManager.getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(PlayerManager.getPlayerId())));
        urlParameters.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
        urlParameters.add(new BasicNameValuePair("movement_json", movementJson));
        return this.requestManager.doPost(url, urlParameters);
    }

    public UserAction getLatestMovementFromToServer() {
        String url = "gameMovement/latest";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(GameRequestManager.getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("opponent_id", String.valueOf(PlayerManager.getOpponentPlayer().getId())));
        urlParameters.add(new BasicNameValuePair("last_timestamp", String.valueOf(timestampOfLastOpponentMovement)));
        String serverResponse = this.requestManager.doPost(url, urlParameters);
        return parseGameRequestResponse(serverResponse);
    }

    private UserAction parseGameRequestResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        switch (code) {
            case 1:
                // game request was either accepted or rejected
                LinkedTreeMap parameters = (LinkedTreeMap) response.getParameters();
                LinkedTreeMap movement = (LinkedTreeMap) parameters.get("game_movement_json");
                Object timestampObj = movement.get("timestamp");
                String timestampStr = timestampObj.toString();
                Long timestamp = Double.valueOf(Double.parseDouble(timestampStr)).longValue();
                UserAction action = new UserAction(movement.get("actionType").toString(), movement.get("direction").toString(), timestamp);

                return action;
            case 2:
                // error
                return null;
            case 3:
                // server validation not passed
                return null;
            case 4:
                // reply is "empty" (should not be passed back)
                return null;
        }
        return null;
    }
}
