package org.scify.memori.card;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scify.memori.helper.JSONFileHandler;
import org.scify.memori.MainOptions;
import org.scify.memori.interfaces.CardDBHandler;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class CardDBHandlerJSON implements CardDBHandler {

    public JSONFileHandler jsonFileHandler;

    public CardDBHandlerJSON() {
        jsonFileHandler = new JSONFileHandler();
    }

    @Override
    public ArrayList<Object> getCardsFromDBFile(String dbFile) {
        ArrayList<Object> jsonCards = new ArrayList<>();
        Scanner scanner = null;
        try {

            scanner = new Scanner(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(dbFile)));
            String jsonStr = scanner.useDelimiter("\\A").next();

            JSONObject rootObject = new JSONObject(jsonStr); // Parse the JSON to a JSONObject
            JSONArray cardSets = jsonFileHandler.getJSONArrayFromObject(rootObject, "equivalence_card_sets");
            cardSets = assignHashCodesToCardsSets(cardSets);
            /*
              The number of cards we need depends on the level (number of rows and columns)
              divided by the number of the card tuple we want to form (2-card patterns, 3-card patterns, etc)
             */
            int numOfCards = (MainOptions.NUMBER_OF_COLUMNS * MainOptions.NUMBER_OF_ROWS);
            System.out.println("num of cards needed: " + numOfCards);

            jsonCards = extractCardsFromSets(cardSets, numOfCards);

        } finally {
            scanner.close();
        }
        return jsonCards;
    }


    /**
     * Extract a specific number of  {@link JSONObject}, Given a set of Json Objects
     * @param cardSets the set of Json objects
     * @param numOfCards the number of objects to be extracted
     * @return a subset of the initial set
     */
    private ArrayList<Object> extractCardsFromSets(JSONArray cardSets, int numOfCards) {
        ArrayList<JSONObject> cardsListTemp;
        ArrayList<Object> extractedCards = new ArrayList<>();
        int randomNumber;
        int cardCount = 0;
        while (cardCount < numOfCards) {
            cardsListTemp = new ArrayList<>();
            // produce a random number for the card sets (we want to select a random card set)
            randomNumber = random_int(0, cardSets.length());
            // select a random equivalence card set
            JSONArray randomCardSet = cardSets.getJSONArray(randomNumber);
            // equivalenceCardSetHashCode describes the current card set
            // shuffle the selected card set so that we pick random cards
            randomCardSet = jsonFileHandler.shuffleJsonArray(randomCardSet);
            Iterator it = randomCardSet.iterator();
            // categories will hold every category that has been already read so we only add one card from each category
            ArrayList categories = new ArrayList();
            while(it.hasNext()) {
                JSONObject currCard = (JSONObject) it.next();
                // if the current category has not been read before and the current card has not been already added
                if(!categories.contains(currCard.get("category"))) {
                    // if the current card is set to be unique
                    if(currCard.get("unique").equals(true)) {
                        // if not unique (ie already exists)
                        if(extractedCards.contains(currCard)) {
                            // reset the temporary cards list
                            cardsListTemp = new ArrayList<>();
                            //we need to break the loop so that we change equivalence card set
                            break;
                        }
                    }
                    // add card
                    cardsListTemp.add(currCard);
                    // mark category as read
                    categories.add(currCard.get("category"));

                }

            }
            extractedCards.addAll(cardsListTemp);
            cardCount += cardsListTemp.size();
        }
        return extractedCards;
    }


    /**
     * given a JSONArray of card objects, assign a unique hash code (for the equivalence set) to each card.
     * @param cardSets the set of cards
     * @return the set of cards with the hash codes
     */
    public JSONArray assignHashCodesToCardsSets(JSONArray cardSets) {
        Iterator it = cardSets.iterator();
        while(it.hasNext()) {
            String equivalenceCardSetHashCode = randomString();
            JSONArray currSet = (JSONArray) it.next();
            Iterator itCards = currSet.iterator();
            while(itCards.hasNext()) {
                JSONObject currCard = (JSONObject) itCards.next();
                if(currCard.get("equivalenceCardSetHashCode").equals(""))
                    currCard.put("equivalenceCardSetHashCode", equivalenceCardSetHashCode);
            }
        }
        return cardSets;
    }


    /**
     * Produce a random integer in [Min - Max) set
     * @param Min the minimum number
     * @param Max the maximum number
     * @return a random integer in [Min - Max)
     */
    private int random_int(int Min, int Max) {
        return (int) (Math.random()*(Max-Min))+Min;
    }

    /**
     * Produces a random String.
     * @return a random String object
     */
    private String randomString() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
