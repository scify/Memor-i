package org.scify.memori;

import com.google.gson.*;
import org.scify.memori.interfaces.MoveFactory;
import org.scify.memori.interfaces.UserAction;
import org.scify.memori.network.GameMovementManager;
import org.scify.memori.rules.RuleObserverObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class OnlineMoveFactory implements Observer, MoveFactory {

    Queue<UserAction> opponentActions;
    GameMovementManager gameMovementManager;

    public OnlineMoveFactory() {
        opponentActions = new LinkedList<>();
        gameMovementManager = new GameMovementManager();
    }

    @Override
    public ArrayList<UserAction> getNextUserMovements(MemoriGameState memoriGameState) throws Exception {
        ArrayList<UserAction> actions = new ArrayList<>();
        if(opponentActions.isEmpty()) {
            getLatestMovementFromServer();
        } else {
            Iterator<UserAction> listIterator = opponentActions.iterator();
            while(listIterator.hasNext()) {
                UserAction currentAction = listIterator.next();
                if(currentAction.getActionType().equals("movement")) {
                    actions.add(new UserAction("opponent_movement", currentAction.getDirection()));
                } else if (!currentAction.getActionType().equals("escape") && !currentAction.getActionType().equals("enter")) {
                    actions.add(new UserAction(currentAction.getActionType(), currentAction.getDirection()));
                }
                GameMovementManager.setTimestampOfLastOpponentMovement(currentAction.getTimestamp());
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

    @Override
    public int getMovementDelay() {
        return 0;
    }

    private int timesCalled = 0;
    private void getLatestMovementFromServer() throws Exception {
        System.out.println("timesCalled: " + timesCalled);
        timesCalled++;
        TimeUnit.MILLISECONDS.sleep(500);
        UserAction opponentLatestAction = gameMovementManager.getLatestMovementFromToServer();
        if(opponentLatestAction != null) {
            timesCalled = 0;
            opponentActions.add(opponentLatestAction);
        } else {
            if(timesCalled > 70) {
                throw new Exception("Queried too many times for latest movement");
            }
        }
    }

    private String sendUserMovementToServer(String userActionsParam) {
        return gameMovementManager.sendMovementToServer(userActionsParam);
    }

    @Override
    public void update(Observable o, Object arg) {
        RuleObserverObject ruleObserverObject = (RuleObserverObject) arg;
        String ruleObserverCode = ruleObserverObject.code;
        if(ruleObserverCode.equals("PLAYER_MOVE")) {
            UserAction userAction = (UserAction) ruleObserverObject.parameters;
            String serverResponse = sendUserMovementToServer(packUserAction(userAction));
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
