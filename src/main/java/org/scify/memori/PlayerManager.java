package org.scify.memori;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.helper.PropertyHandlerImpl;
import org.scify.memori.interfaces.Player;
import org.scify.memori.interfaces.PropertyHandler;
import org.scify.memori.network.RequestManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private String userNamesFile;
    private PropertyHandler propertyHandler;

    private RequestManager requestManager;

    private static int playerId;

    public static void setPlayerId(int playerId) {
        PlayerManager.playerId = playerId;
    }

    public static int getPlayerId() {

        return playerId;
    }

    public PlayerManager() {
        String userNamesFileDir;
        propertyHandler = new PropertyHandlerImpl();
        requestManager = new RequestManager();
        if ((System.getProperty("os.name")).toUpperCase().contains("WINDOWS")) {
            userNamesFileDir = System.getenv("AppData");
        } else {
            userNamesFileDir = System.getProperty("user.dir");
        }
        userNamesFile = userNamesFileDir + File.separator + ".user_names.properties";
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

    public String getOnlinePlayersFromServer() {
        String url = "players/online";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", String.valueOf(PlayerManager.getPlayerId())));
        url += "?player_id=" + PlayerManager.getPlayerId();
        return this.requestManager.doGet(url);
    }


}
