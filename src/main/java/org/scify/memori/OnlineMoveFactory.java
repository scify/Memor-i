package org.scify.memori;

import com.google.gson.*;
import org.scify.memori.interfaces.MoveFactory;
import org.scify.memori.interfaces.UserAction;
import org.scify.memori.network.GameMovementManager;
import org.scify.memori.rules.RuleObserverObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class OnlineMoveFactory implements Observer, MoveFactory {

    Queue<UserAction> opponentActions;
    Queue<UserAction> playerActions;
    GameMovementManager gameMovementManager;
    Thread sendUserMovementsToServerThread;
    private Thread threadSetPlayerOnline;
    private int timesNextMovementCalled = 0;
    private static final int SEND_MOVEMENT_INTERVAL = 200;

    public OnlineMoveFactory() {
        opponentActions = new ConcurrentLinkedQueue<>();
        playerActions = new ConcurrentLinkedQueue<>();
        gameMovementManager = new GameMovementManager();
        setPlayerOnlineThread();
        sendUserMovementsToServerThread = new Thread(() ->sendNextMovementsToServer());
        sendUserMovementsToServerThread.start();
    }

    private void sendNextMovementsToServer() {
        while(true) {
            if (!playerActions.isEmpty()) {
                UserAction userActionToSend = playerActions.poll();
                sendUserMovementToServer(packUserAction(userActionToSend), userActionToSend.getTimestamp());
            }
            try {
                Thread.sleep(SEND_MOVEMENT_INTERVAL);
            } catch (InterruptedException e) {
                System.err.println("sendNextMovementsToServer thread interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
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
            getNextMovementFromServer();
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

    private void getNextMovementFromServer() throws Exception {
        // Minimum time before call
        TimeUnit.MILLISECONDS.sleep(200);
        // Make sure no existing call is ongoing
        if (gameMovementManager.callOngoing())
            return;

        UserAction opponentNextAction = gameMovementManager.getNextMovementFromServer();
        timesNextMovementCalled++;
        if(opponentNextAction != null) {
            timesNextMovementCalled = 0;
            opponentActions.add(opponentNextAction);
        } else {
            if(timesNextMovementCalled > gameMovementManager.MAX_REQUEST_TRIES_FOR_MOVEMENT) {
                throw new Exception("Queried too many times for latest movement");
            }
        }
    }

    private String sendUserMovementToServer(String userActionsParam, long userActionTimestamp) {
        String sRes =  gameMovementManager.sendMovementToServer(userActionsParam, userActionTimestamp);
        while (sRes == null) {
            sRes =  gameMovementManager.sendMovementToServer(userActionsParam, userActionTimestamp);
        }
        return sRes;
    }

    @Override
    public void update(Observable o, Object arg) {
        RuleObserverObject ruleObserverObject = (RuleObserverObject) arg;
        String ruleObserverCode = ruleObserverObject.code;
        if(ruleObserverCode.equals("PLAYER_MOVE")) {
            UserAction userAction = (UserAction) ruleObserverObject.parameters;
            // TODO check order of user actions to be sent to server (queue) or semaphore
            playerActions.add(userAction);
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
