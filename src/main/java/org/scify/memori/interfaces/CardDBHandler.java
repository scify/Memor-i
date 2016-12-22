package org.scify.memori.interfaces;

import org.scify.memori.card.Card;

import java.util.List;

public interface CardDBHandler {

    /**
     * Gets a specified number of cards from the DB
     * @param numOfCards the desired number of cards
     * @return a set {@link List} of cards
     */
    List<Card> getCardsFromDB(int numOfCards);

}
