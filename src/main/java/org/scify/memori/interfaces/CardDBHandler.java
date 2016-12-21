package org.scify.memori.interfaces;

import java.util.ArrayList;

public interface CardDBHandler {

    ArrayList<Object> getCardsFromDBFile(String dbFile);

}
