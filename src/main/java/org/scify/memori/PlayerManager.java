package org.scify.memori;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.scify.memori.helper.PropertyHandlerImpl;
import org.scify.memori.interfaces.PropertyHandler;
import org.scify.memori.network.RequestManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private String userNamesFile;
    private PropertyHandler propertyHandler;

    private RequestManager requestManager;

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

    public void storeNewPlayer(String userName, int newPlayerId) {
        propertyHandler.setPropertyByName(userNamesFile, userName, String.valueOf(newPlayerId));
        propertyHandler.setPropertyByName(userNamesFile, "last_used_id", String.valueOf(newPlayerId));
    }

    public String getPlayerIdFromUserName(String userName) {
        return propertyHandler.getPropertyByName(userNamesFile, userName);
    }

    public String getIdOfLastPlayer() {
        return propertyHandler.getPropertyByName(userNamesFile, "last_used_id");
    }

    public String sendUserNametoServer(String userName, String userId) {
        String url = "username";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("user_name", userName));
        urlParameters.add(new BasicNameValuePair("player_id", userId));
        return this.requestManager.doPost(url, urlParameters);
    }

    public String getOnlinePlayersFromServer() {
        String url = "players/online";
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("player_id", getIdOfLastPlayer()));
        url += "?player_id=" + getIdOfLastPlayer();
        return this.requestManager.doGet(url);
    }
}
