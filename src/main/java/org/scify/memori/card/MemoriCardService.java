package org.scify.memori.card;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.scify.memori.OnlineMoveFactory;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Responsible for loading and handling the cards for the game
 */
public class MemoriCardService {

    // DB handler is a representation of the DB towards the application
    protected CardDBHandlerJSON cardDBHandlerJSON;

    public MemoriCardService() {
        this.cardDBHandlerJSON = new CardDBHandlerJSON();
    }

    public List<Card> getMemoriCards(int numOfCards) {
        /*
          The number of cards we need depends on the level (number of rows and columns)
          divided by the number of the card tuple we want to form (2-card patterns, 3-card patterns, etc)
         */
        List<Card> cards = shuffleCards(this.cardDBHandlerJSON.getCardsFromDB(numOfCards));
        GsonBuilder gb;
        gb = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .setVersion(1.0);
        Gson gson = gb.create();
        String jsonInString = gson.toJson(cards);
        System.out.println(jsonInString);
        return cards;
    }

    public int getNumberOfSets() {
        int numOfSets = this.cardDBHandlerJSON.getNumberOfEquivalenceCardSets();
        int numOfCards = this.cardDBHandlerJSON.getNumOfCardsInDB();
        System.out.println("equivalence card sets: " + numOfSets + ", num of cards: " + numOfCards);
        return numOfCards;
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
