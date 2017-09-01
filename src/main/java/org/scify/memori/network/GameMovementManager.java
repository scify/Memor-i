package org.scify.memori.network;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.PlayerManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GameMovementManager {

    private RequestManager requestManager;

    public GameMovementManager() {
        requestManager = new RequestManager();
    }

    public String sendMovementToServer(String movementJson) {
        String url = "gameMovement/create";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(GameRequestManager.getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(PlayerManager.getPlayerId())));
        urlParameters.add(new BasicNameValuePair("timestamp", String.valueOf(timestamp.getTime())));
        urlParameters.add(new BasicNameValuePair("movement_json", movementJson));
        return this.requestManager.doPost(url, urlParameters);
    }
}
