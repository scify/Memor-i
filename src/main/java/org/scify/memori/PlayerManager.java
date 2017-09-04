package org.scify.memori;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.RequestManager;
import org.scify.memori.network.ServerOperationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PlayerManager  implements Runnable {

    private RequestManager requestManager;

    private static int playerId;
    private static Player localPlayer;
    private static Player opponentPlayer;
    private static Player startingPlayer;
    public static boolean localPlayerIsInitiator = false;
    private String callIdentifier;

    public PlayerManager() {
        requestManager = new RequestManager();
    }

    public PlayerManager(String callIdentifier) {
        requestManager = new RequestManager();
        this.callIdentifier = callIdentifier;
    }


    private void markPlayerActive() {
        String url = "player/markActive";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(getPlayerId())));
        String response = requestManager.doPost(url, urlParameters);
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
        return this.requestManager.doPost(url, urlParameters);
    }

    public String login(String userName, String password) {
        String url = "player/login";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("user_name", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String getPlayerAvailability(String userName) {
        String url = "player/availability?user_name=" + userName;
        return this.requestManager.doGet(url);
    }

    public String getOnlinePlayersFromServer() {
        String url = "players/online";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(PlayerManager.getPlayerId())));
        url += "?player_id=" + PlayerManager.getPlayerId();
        return this.requestManager.doGet(url);
    }

    @Override
    public void run() {
        switch (callIdentifier) {
            case "PLAYER_ACTIVE":
                markPlayerActive();
            default:
                break;
        }
    }
}
