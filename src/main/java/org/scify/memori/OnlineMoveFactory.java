package org.scify.memori;

import com.google.gson.*;
import org.scify.memori.interfaces.MoveFactory;
import org.scify.memori.interfaces.Player;
import org.scify.memori.interfaces.UserAction;
import org.scify.memori.network.GameMovementManager;
import org.scify.memori.rules.RuleObserverObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class OnlineMoveFactory implements Observer, MoveFactory {

    Queue<UserAction> opponentActions;
    GameMovementManager gameMovementManager;
    Thread sendUserMovementsToServerThread;
    private Thread threadSetPlayerOnline;

    public OnlineMoveFactory() {
        opponentActions = new LinkedList<>();
        gameMovementManager = new GameMovementManager();
        setPlayerOnlineThread();
    }

    private void setPlayerOnlineThread() {
        threadSetPlayerOnline = new Thread(() -> setPlayerOnline());
        threadSetPlayerOnline.start();
    }

    private void setPlayerOnline() {
        PlayerManager playerManager = new PlayerManager();
        while(true) {
            playerManager.setPlayerOnline();
            try {
                Thread.sleep(PlayerManager.MARK_PLAYER_ACTIVE_CALL_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public ArrayList<UserAction> getNextUserMovement(MemoriGameState memoriGameState) throws Exception {
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

    @Override
    public void terminateFactoryComponents() {
        threadSetPlayerOnline.interrupt();
    }

    private int timesCalled = 0;
    private void getLatestMovementFromServer() throws Exception {
//        System.out.println("timesCalled: " + timesCalled);

        TimeUnit.MILLISECONDS.sleep(200);
        UserAction opponentLatestAction = gameMovementManager.getLatestMovementFromToServer();
        timesCalled++;
        if(opponentLatestAction != null) {
            timesCalled = 0;
            opponentActions.add(opponentLatestAction);
        } else {
            if(timesCalled > gameMovementManager.MAX_REQUEST_TRIES_FOR_MOVEMENT) {
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
            sendUserMovementsToServerThread = new Thread(() ->sendUserMovementToServer(packUserAction(userAction)));
            sendUserMovementsToServerThread.start();
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
