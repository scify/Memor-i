package org.scify.memori.card;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.scify.memori.interfaces.Tile;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Responsible for loading and handling the cards for the game
 */
public class MemoriCardService {

    // DB handler is a representation of the DB towards the application
    protected CardDBHandlerJSON cardDBHandlerJSON;

    public MemoriCardService() {
        this.cardDBHandlerJSON = CardDBHandlerJSON.getInstance();
    }

    public void initCards() {
        this.cardDBHandlerJSON.initCards();
    }

    public List<Card> getMemoriCards(int numOfCards) {
        return shuffleCards(this.cardDBHandlerJSON.getCardsFromDB(numOfCards));
    }

    public int getNumberOfSets() {
        int numOfSets = this.cardDBHandlerJSON.getNumberOfEquivalenceCardSets();
        int numOfCards = this.cardDBHandlerJSON.getNumOfCardsInDB();
        System.out.println("equivalence card sets: " + numOfSets + ", num of cards: " + numOfCards);
        return numOfCards;
    }


    /**
     * Shuffles the given {@link List} of {@link Card}s.
     *
     * @param cards the list of cards
     * @return the shuffled list of cards
     */
    private List<Card> shuffleCards(List<Card> cards) {
        long seed = System.nanoTime();
        Collections.shuffle(cards, new Random(seed));
        return cards;
    }

    public String terrainTilesToJSON(Map<Point2D, Tile> terrainTiles) {
        Card[] cards = new Card[terrainTiles.size()];
        int index = 0;
        for (Map.Entry<Point2D, Tile> entry : terrainTiles.entrySet()) {
            Card card = (Card) entry.getValue();
            card.xPos = (int) entry.getKey().getX();
            card.yPos = (int) entry.getKey().getY();
            cards[index] = card;
            index++;
        }
        GsonBuilder gb;
        gb = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .setVersion(1.0);
        Gson gson = gb.create();
        String jsonInString = gson.toJson(cards);
        return jsonInString;
    }

    public CategorizedCard getCardFromLabelAndType(String cardLabel, String cardCategory) {
        for (Card card : this.cardDBHandlerJSON.getCards()) {
            CategorizedCard categorizedCard = (CategorizedCard) card;
            if (categorizedCard.getLabel().equals(cardLabel) && categorizedCard.getCategory().equals(cardCategory))
                return categorizedCard;
        }
        return null;
    }

}
