package org.scify.memori.interfaces;

import org.scify.memori.MemoriGameState;

import java.util.ArrayList;

public interface MoveFactory {
    public ArrayList<UserAction> getNextUserMovement(MemoriGameState memoriGameState) throws Exception;
    public  UserAction getUserFlip();
    public void updateFactoryComponents();
    int getMovementDelay();
}
