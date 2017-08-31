package org.scify.memori.network;

import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.PlayerManager;
import org.scify.memori.helper.MemoriConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GameRequestManager implements Callable<ServerOperationResponse> {

    private RequestManager requestManager;
    private MemoriConfiguration configuration;
    private static int gameRequestId;
    private String callIdentifier;

    public static void setGameRequestId(int gameRequestId) {
        GameRequestManager.gameRequestId = gameRequestId;
    }

    public static int getGameRequestId() {

        return gameRequestId;
    }

    public GameRequestManager() {
        requestManager = new RequestManager();
        configuration = new MemoriConfiguration();
    }

    public GameRequestManager(String callIdentifier) {
        requestManager = new RequestManager();
        configuration = new MemoriConfiguration();
        this.callIdentifier = callIdentifier;
    }

    public ServerOperationResponse call() throws Exception {
        switch (callIdentifier) {
            case "GET_GAME_REQUEST_REPLY":
                return askServerForGameRequestReply();
            case "GET_REQUESTS":
                return askServerForGameRequests();
            default:
                break;
        }
        return null;
    }

    private ServerOperationResponse askServerForGameRequests() {
        String url = "player/requests?player_id=" + PlayerManager.getPlayerId();
        String response = requestManager.doGet(url);
        if(response != null) {
            ServerOperationResponse serverOperationResponse = parseGameRequestsResponse(response);
            if(serverOperationResponse != null) {
                return serverOperationResponse;
            }
        }
        return null;
    }

    private ServerOperationResponse askServerForGameRequestReply() {
        String url = "gameRequest/reply?game_request_id=" + getGameRequestId();
        String response = requestManager.doGet(url);
        if(response != null) {
            ServerOperationResponse serverOperationResponse = parseGameRequestReplyResponse(response);
            if(serverOperationResponse != null) {
                return serverOperationResponse;
            }
        }
        return null;
    }

    public String sendGameRequestToPlayer(int playerInitiatorId, int playerOpponentId, int gameLevelId) {
        String url = "gameRequest/initiate";
        String gameIdentifier = configuration.getProjectProperty("GAME_IDENTIFIER");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_initiator_id", String.valueOf(playerInitiatorId)));
        urlParameters.add(new BasicNameValuePair("player_opponent_id", String.valueOf(playerOpponentId)));
        urlParameters.add(new BasicNameValuePair("game_identifier", gameIdentifier));
        urlParameters.add(new BasicNameValuePair("game_level_id", String.valueOf(gameLevelId)));
        return this.requestManager.doPost(url, urlParameters);
    }

    private ServerOperationResponse parseGameRequestsResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        response.setParameters(g.toJsonTree(response.getParameters()).getAsJsonObject());
        int code = response.getCode();
        switch (code) {
            case 1:
                return response;
            case 2:
                return null;
        }
        return null;
    }

    private ServerOperationResponse parseGameRequestReplyResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        System.out.println("game request reply response code: " + code);
        switch (code) {
            case 1:
                // game request was either accepted or rejected
                return response;
            case 2:
                // error
                return null;
            case 3:
                // server validation not passed
                return null;
            case 4:
                // no reply for game request yet
                return null;
        }
        return null;
    }

    public String sendShuffledDeckToServer(String jsonRepresentationOfTiles) {
        String url = "gameRequest/shuffledCards";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("shuffled_cards", jsonRepresentationOfTiles));
        return this.requestManager.doPost(url, urlParameters);
    }
}
