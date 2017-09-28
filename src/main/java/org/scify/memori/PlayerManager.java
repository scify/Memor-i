package org.scify.memori;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.RequestManager;
import org.scify.memori.network.ServerOperationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PlayerManager  implements Callable<String> {

    public static final int MARK_PLAYER_ACTIVE_CALL_INTERVAL = 3;
    private RequestManager requestManager;

    private static int playerId;
    private static Player localPlayer;
    private static Player opponentPlayer;
    private static Player startingPlayer;
    private MemoriConfiguration configuration = new MemoriConfiguration();
    public static boolean localPlayerIsInitiator = false;
    private String callIdentifier;
    String gameIdentifier;

    public PlayerManager() {
        requestManager = new RequestManager();
        gameIdentifier = configuration.getProjectProperty("GAME_IDENTIFIER");
    }

    public PlayerManager(String callIdentifier) {
        requestManager = new RequestManager();
        this.callIdentifier = callIdentifier;
        gameIdentifier = configuration.getProjectProperty("GAME_IDENTIFIER");
    }


    private String markPlayerActive() {
        String url = "player/markActive";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(getPlayerId())));
        String response = requestManager.doPost(url, urlParameters);
        System.out.println("player/markActive");
        return response;
    }

    public static void setLocalPlayer(Player localPlayer) {
        PlayerManager.localPlayer = localPlayer;
    }

    public static void setStartingPlayer(Player startingPlayer) {
        PlayerManager.startingPlayer = startingPlayer;
    }

    public static Player getStartingPlayer() {
        return localPlayerIsInitiator ? localPlayer : opponentPlayer;
    }

    public static void setOpponentPlayer(Player opponent) {
        PlayerManager.opponentPlayer = opponent;
    }

    public static Player getOpponentPlayer() {
        return opponentPlayer;
    }

    public static Player getLocalPlayer() {
        return localPlayer;
    }

    public static void setPlayerId(int playerId) {
        PlayerManager.playerId = playerId;
    }

    public static int getPlayerId() {
        return playerId;
    }

    public String register(String userName, String password) {
        String url = "player/register";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("user_name", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("game_flavor_pack_identifier", gameIdentifier));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String login(String userName, String password) {
        String url = "player/login";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("user_name", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("game_flavor_pack_identifier", gameIdentifier));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String getPlayerAvailability(String userName) {
        String url = "player/availability?user_name=" + userName + "&game_flavor_pack_identifier=" + gameIdentifier + "&player_initiator_id=" + String.valueOf(getPlayerId());
        return this.requestManager.doGet(url);
    }

    public String searchForRandomPlayer() {
        String url = "player/random?player_id=" + getPlayerId() + "&game_flavor_pack_identifier=" + gameIdentifier;
        return this.requestManager.doGet(url);
    }

    @Override
    public String call() throws Exception {
        switch (callIdentifier) {
            case "PLAYER_ACTIVE":
                return markPlayerActive();
            default:
                break;
        }
        return null;
    }
}
