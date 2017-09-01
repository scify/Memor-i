package org.scify.memori;

import com.google.gson.*;
import org.scify.memori.interfaces.MoveFactory;
import org.scify.memori.interfaces.UserAction;
import org.scify.memori.network.GameMovementManager;

import java.awt.geom.Point2D;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class OnlineMoveFactory implements Observer, MoveFactory {

    Queue<UserAction> opponentActions;
    GameMovementManager gameMovementManager;

    public OnlineMoveFactory() {
        opponentActions = new LinkedList<>();
        gameMovementManager = new GameMovementManager();
    }

    @Override
    public ArrayList<UserAction> getNextUserMovements(MemoriGameState memoriGameState) {
        ArrayList<UserAction> actions = new ArrayList<>();
        if(opponentActions.isEmpty()) {
            getLatestMoveFromServer();
        } else {
            Iterator<UserAction> listIterator = opponentActions.iterator();
            while(listIterator.hasNext()) {
                UserAction currentAction = listIterator.next();
                if(currentAction.getActionType().equals("movement")) {
                    actions.add(new UserAction("opponent_movement", currentAction.getDirection()));
                }
                listIterator.remove();
            }

        }
        return actions;
    }

    @Override
    public  UserAction getUserFlip() {
        return new UserAction("flip", "SPACE");
    }

    @Override
    public void updateFactoryComponents() {
        opponentActions = new LinkedList<>();
    }

    private void getLatestMoveFromServer() {
        String sURL = "http://localhost/memoribackend/test";
        try {
            URL url = new URL(sURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
            JsonArray moveObj = rootobj.get("actions").getAsJsonArray(); //just grab the zipcode
            request.connect();
            for (JsonElement movement : moveObj) {
                JsonObject paymentObj = movement.getAsJsonObject();
                String     actionType     = paymentObj.get("actionType").getAsString();
                String     actionDirection = paymentObj.get("direction").getAsString();
                opponentActions.add(new UserAction(actionType, actionDirection));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String sendUserMoveToServer(String userActionsParam) {
        return gameMovementManager.sendMovementToServer(userActionsParam);
    }

    @Override
    public void update(Observable o, Object arg) {
        RuleObserverObject ruleObserverObject = (RuleObserverObject) arg;
        String ruleObserverCode = ruleObserverObject.code;
        if(ruleObserverCode.equals("PLAYER_MOVE")) {
            UserAction userAction = (UserAction) ruleObserverObject.parameters;
            userAction.setTimestamp(System.currentTimeMillis());
            String serverResponse = sendUserMoveToServer(packUserAction(userAction));
            System.out.println("Sent movement. response: " + serverResponse);
        }
    }

    private String packUserAction(UserAction userAction) {
        GsonBuilder gb;
        gb = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .setVersion(1.0);
        Gson gson = gb.create();
        String jsonInString = gson.toJson(userAction);
        return jsonInString;
    }
}
