package org.scify.memori.interfaces;

import java.util.Observer;

public interface Observable {

    public void addObserver(Observer o);

    public void removeObserver(Observer o);

    public void notifyObserver();
}
