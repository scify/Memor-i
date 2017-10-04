package org.scify.memori;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.Player;
import org.scify.memori.network.RequestManager;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    public static final int MARK_PLAYER_ACTIVE_CALL_INTERVAL = 3000;
    private RequestManager requestManager;

    private static int playerId;
    private static Player localPlayer;
    private static Player opponentPlayer;
    public static boolean localPlayerIsInitiator = false;
    private String gameIdentifier;

    public PlayerManager() {
        requestManager = new RequestManager();
        MemoriConfiguration configuration = new MemoriConfiguration();
        gameIdentifier = configuration.getProjectProperty("GAME_IDENTIFIER");
    }

    public String setPlayerOnline() {
        String url = "player/setOnline";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(getPlayerId())));
        String response = requestManager.doPost(url, urlParameters);
//        System.out.println("player/setOnline");
        return response;
    }

    public static void setLocalPlayer(Player localPlayer) {
        PlayerManager.localPlayer = localPlayer;
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

    public String setPlayerAsNotInGame() {
        String url = "player/notInGame";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(getPlayerId())));
        return this.requestManager.doPost(url, urlParameters);
    }
}
