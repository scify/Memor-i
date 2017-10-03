package org.scify.memori.interfaces;

import org.scify.memori.MemoriGameState;

import java.util.ArrayList;

public interface MoveFactory {
    ArrayList<UserAction> getNextUserMovement(MemoriGameState memoriGameState) throws Exception;
    UserAction getUserFlip();
    void updateFactoryComponents();
    int getMovementDelay();
    void terminateFactoryComponents();
}
