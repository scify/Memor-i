package org.scify.memori.card;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Responsible for loading the cards for the game
 */
public class MemoriCardService {

    protected CardDBHandlerJSON cardDBHandlerJSON;

    public MemoriCardService() {
        this.cardDBHandlerJSON = new CardDBHandlerJSON();
    }



    public List<Card> getMemoriCards(int numOfCards) {
        /*
          The number of cards we need depends on the level (number of rows and columns)
          divided by the number of the card tuple we want to form (2-card patterns, 3-card patterns, etc)
         */
        return shuffleCards(this.cardDBHandlerJSON.getCardsFromDB(numOfCards));
    }

    public int getNumberOfCards() {
        System.out.println("equivalence card sets: " + this.cardDBHandlerJSON.getNumberOfEquivalenceCardSets() + ", num of cards: " + this.cardDBHandlerJSON.getNumOfCardsInDB());
        return this.cardDBHandlerJSON.getNumberOfEquivalenceCardSets();
    }


    /**
     * Shuffles the given {@link List} of {@link Card}s.
     * @param cards the list of cards
     * @return the shuffled list of cards
     */
    private List<Card> shuffleCards(List<Card> cards) {
        long seed = System.nanoTime();
        Collections.shuffle(cards, new Random(seed));
        return cards;
    }

}
