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
            boolean actionsCompleted = false;
            Iterator<UserAction> listIterator = opponentActions.iterator();
            while(!actionsCompleted && listIterator.hasNext()) {
                UserAction currentAction = listIterator.next();
                if(currentAction.getActionType().equals("movement")) {
                    actions.add(new UserAction("opponent_movement", currentAction.getDirection()));
                    listIterator.remove();
                } else {
                    listIterator.remove();
                    actionsCompleted = true;
                }
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

    private void sendUserMoveToServer(String userActionsParam) {
        gameMovementManager.sendMovementToServer(userActionsParam);
    }

    @Override
    public void update(Observable o, Object arg) {
        RuleObserverObject ruleObserverObject = (RuleObserverObject) arg;
        String ruleObserverCode = ruleObserverObject.code;
        if(ruleObserverCode.equals("PLAYER_MOVE")) {
            UserAction userAction = (UserAction) ruleObserverObject.parameters;
            userAction.setTimestamp(System.currentTimeMillis());
            sendUserMoveToServer(packUserAction(userAction));
        }
    }

    private String packUserAction(UserAction userAction) {
        GsonBuilder gb;
        gb = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .setVersion(1.0)
                .registerTypeAdapter(Point2D.class, new OnlineMoveFactory.Point2DDeserializer());
        Gson gson = gb.create();
        String jsonInString = gson.toJson(userAction);
        return jsonInString;
    }

    public static class Point2DDeserializer implements JsonDeserializer<Point2D>, JsonSerializer<Point2D> {

        @Override
        public Point2D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            Point2D.Double ans = new Point2D.Double();
            JsonObject obj = json.getAsJsonObject();

            ans.x = obj.get("x").getAsDouble();
            ans.y = obj.get("y").getAsDouble();

            return ans;
        }

        @Override
        public JsonElement serialize(final Point2D point2D, final Type type, final JsonSerializationContext jsonSerializationContext) {
            return new Gson().toJsonTree(point2D);
        }
    }
}
