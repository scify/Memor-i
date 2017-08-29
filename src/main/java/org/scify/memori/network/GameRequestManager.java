package org.scify.memori.network;

import com.google.gson.Gson;
import org.scify.memori.PlayerManager;

import java.util.concurrent.Callable;

public class GameRequestManager implements Callable<String> {

    private RequestManager requestManager;
    private PlayerManager playerManager;
    public GameRequestManager(String step) {
        requestManager = new RequestManager();
        playerManager = new PlayerManager();
    }

    public String call() throws Exception {
        return askServerForGameRequests();
    }

    public String askServerForGameRequests() {
        String url = "games/request?player_id=" + playerManager.getIdOfLastPlayer();
        String response = requestManager.doGet(url);
        if(response != null) {
            String opponentUserName = parseGameRequestResponse(response);
            if(opponentUserName != null) {
                return opponentUserName;
            }
        }
        return null;
    }

    private String parseGameRequestResponse(String serverResponse) {
        Gson g = new Gson();
        ServerOperationResponse response = g.fromJson(serverResponse, ServerOperationResponse.class);
        int code = response.getCode();
        switch (code) {
            case 1:
                // New game request
                // TODO prompt for new game request
                String opponentUserName = (String) response.getParameters();
                return opponentUserName;
            case 2:
                return null;
        }
        return null;
    }
}
