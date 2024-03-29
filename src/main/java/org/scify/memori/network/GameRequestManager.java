package org.scify.memori.network;

import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.PlayerManager;
import org.scify.memori.helper.MemoriConfiguration;

import java.util.ArrayList;
import java.util.List;

public class GameRequestManager {

    public static final int GAME_REQUESTS_CALL_INTERVAL = 2000;
    public static final int SHUFFLE_CARDS_INTERVAL = 1000;
    private RequestManager requestManager;
    private MemoriConfiguration configuration;
    private static int gameRequestId;

    public static void setGameRequestId(int gameRequestId) {
        GameRequestManager.gameRequestId = gameRequestId;
    }

    public static int getGameRequestId() {

        return gameRequestId;
    }

    public GameRequestManager() {
        requestManager = new RequestManager();
        configuration = MemoriConfiguration.getInstance();
    }

    public ServerOperationResponse askServerForGameRequests() {
        String url = "player/requests?player_id=" + PlayerManager.getPlayerId();
        String response = requestManager.doGet(url);
        if(response != null && !response.equals("null")) {
            ServerOperationResponse serverOperationResponse = parseGameRequestsResponse(response);
            if(serverOperationResponse != null) {
                return serverOperationResponse;
            }
        }
        return null;
    }

    public ServerOperationResponse askServerForGameRequestReply() throws Exception{
        String url = "gameRequest/reply?game_request_id=" + getGameRequestId() + "&opponent_id=" + String.valueOf(PlayerManager.getOpponentPlayer().getId());
        String response = requestManager.doGet(url);
        if(response != null) {
            ServerOperationResponse serverOperationResponse = parseGameRequestResponse(response);
            if(serverOperationResponse != null) {
                return serverOperationResponse;
            }
        }
        return null;
    }

    public ServerOperationResponse askServerForGameRequestShuffledCards() throws Exception{
        String url = "gameRequest/shuffledCards?game_request_id=" + getGameRequestId() + "&opponent_id=" + String.valueOf(PlayerManager.getOpponentPlayer().getId());
        System.out.println("opponent id: " + String.valueOf(PlayerManager.getOpponentPlayer().getId()));
        String response = requestManager.doGet(url);
        if(response != null) {
            ServerOperationResponse serverOperationResponse = parseGameRequestResponse(response);
            return serverOperationResponse;
        }
        return null;
    }

    public String sendGameRequestToPlayer(int playerInitiatorId, int playerOpponentId, int gameLevelId) {
        String url = "gameRequest/initiate";
        String gameIdentifier = configuration.getDataPackProperty("GAME_IDENTIFIER");
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
            case ServerResponse.RESPONSE_SUCCESSFUL:
                return response;
            default:
                return null;
        }
    }

    private ServerOperationResponse parseGameRequestResponse(String serverResponse) throws Exception {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        switch (code) {
            case ServerResponse.RESPONSE_SUCCESSFUL:
                // game request was either accepted or rejected
                return response;
            case ServerResponse.OPPONENT_OFFLINE:
                // opponent went offline.
                // throw an Exception to let rules know that the game Ended.
                throw new Exception("Opponent offline!");
            default:
                return null;
        }
    }

    public String sendShuffledDeckToServer(String jsonRepresentationOfTiles) {
        String url = "gameRequest/shuffledCards";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("shuffled_cards", jsonRepresentationOfTiles));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String endGame() {
        String url = "gameRequest/end";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(getGameRequestId())));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String cancelGame() {
        String url = "gameRequest/cancel";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(getGameRequestId())));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String sendGameRequestAnswerToServer(boolean gameRequestAccepted) {
        String url = "gameRequest/reply";
        System.out.println("answer: " + String.valueOf(gameRequestAccepted));
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("game_request_id", String.valueOf(getGameRequestId())));
        urlParameters.add(new BasicNameValuePair("accepted", String.valueOf(gameRequestAccepted)));
        String serverResponse = this.requestManager.doPost(url, urlParameters);
        System.out.println(serverResponse);
        return  serverResponse;
    }
}
