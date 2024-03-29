package org.scify.memori.card;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.JSONFileHandler;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.interfaces.CardDBHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;

public class CardDBHandlerJSON implements CardDBHandler {

    public JSONFileHandler jsonFileHandler;
    public JSONArray jsonArray;
    private List<Card> cards = new ArrayList<>();
    private static String dbFilePath;
    protected static CardDBHandlerJSON instance = null;

    public static CardDBHandlerJSON getInstance() {
        if (instance == null)
            instance = new CardDBHandlerJSON();
        return instance;
    }

    private CardDBHandlerJSON() {
        jsonFileHandler = new JSONFileHandler();
        ResourceLocator resourceLocator = ResourceLocator.getInstance();
        if (dbFilePath == null)
            dbFilePath = resourceLocator.getCorrectPathForFile("json_DB", "/equivalence_cards_sets.json");
        //because we want to perform getResourceAsStream on the dbFile, we need to eliminate the slash "/" that the string starts with:
        if (dbFilePath.charAt(0) == '/') {
            dbFilePath = dbFilePath.substring(1);
        }
        // MemoriLogger.LOGGER.log(Level.INFO, "Loaded: " + dbFilePath);
    }

    public static void setDbFilePath(String filePath) {
        dbFilePath = filePath;
    }

    public void initCards() {
        if (dbFilePath.startsWith("http"))
            this.jsonArray = getObjectRemote("equivalence_card_sets");
        else
            this.jsonArray = getObjectFromJSONFile("equivalence_card_sets");
        this.cards = this.getCardsFromDB(this.getNumOfCardsInDB());
    }

    @Override
    public int getNumberOfEquivalenceCardSets() {
        return jsonArray.length();
    }

    public int getNumOfCardsInDB() {
        Iterator<Object> it = jsonArray.iterator();
        int cardsNum = 0;
        while (it.hasNext()) {
            JSONArray currentEquivalenceCardSet = (JSONArray) it.next();
            cardsNum += currentEquivalenceCardSet.length();
        }
        return cardsNum;
    }

    @Override
    public List<Card> getCardsFromDB(int numOfCards) {

        ArrayList<Object> setObjects = extractObjectsFromJSONArray(jsonArray, numOfCards);
        JSONFileHandler jsonFileHandler = new JSONFileHandler();
        List<Card> cardSet = new ArrayList<>();
        for (Object setObject : setObjects) {
            JSONObject currObj = (JSONObject) setObject;
            int cardDescriptionSoundProbability = 0;
            if (currObj.has("description_sound_probability"))
                cardDescriptionSoundProbability = currObj.getInt("description_sound_probability");
            Card newCard = new CategorizedCard(
                    (String) currObj.get("label"),
                    jsonFileHandler.jsonArrayToStringArray((JSONArray) currObj.get("images")),
                    jsonFileHandler.jsonArrayToStringArray((JSONArray) currObj.get("sounds")),
                    (String) currObj.get("category"),
                    (String) currObj.get("equivalenceCardSetHashCode"),
                    (String) currObj.get("description_sound"),
                    cardDescriptionSoundProbability
            );
            cardSet.add(newCard);
        }
        return cardSet;
    }

    /**
     * Given a json file, read the root object (identified by the second parameter)
     *
     * @param objectId the id identifying the desired object
     * @return
     */
    public JSONArray getObjectFromJSONFile(String objectId) {
        //System.err.println("opening db file: " + dbFile);
        JSONArray objectSets;
        Scanner scanner = null;
        try {

            scanner = new Scanner(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(dbFilePath))));
            String jsonStr = scanner.useDelimiter("\\A").next();

            JSONObject rootObject = new JSONObject(jsonStr); // Parse the JSON to a JSONObject
            objectSets = jsonFileHandler.getJSONArrayFromObject(rootObject, objectId);
            objectSets = assignHashCodesToCardsSets(objectSets);

        } finally {
            assert scanner != null;
            scanner.close();
        }
        return objectSets;
    }

    public JSONArray getObjectRemote(String objectId) {
        JSONArray objectSets = null;
        try {
            JSONObject rootObject = new JSONObject(IOUtils.toString(new URL(dbFilePath), StandardCharsets.UTF_8));
            objectSets = jsonFileHandler.getJSONArrayFromObject(rootObject, objectId);

        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }

        return assignHashCodesToCardsSets(objectSets);
    }

    /**
     * Extract a specific number of  {@link JSONObject}, Given a set of Json Objects
     *
     * @param cardSets   the set of Json objects
     * @param numOfCards the number of objects to be extracted
     * @return a subset of the initial set
     */
    private ArrayList<Object> extractObjectsFromJSONArray(JSONArray cardSets, int numOfCards) {

        ArrayList<JSONObject> cardsListTemp;
        ArrayList<Object> extractedCards = new ArrayList<>();
        int randomNumber;
        int cardCount = 0;
        ArrayList equivalenceCardSetHashCodes = new ArrayList();
        while (cardCount < numOfCards) {
            cardsListTemp = new ArrayList<>();
            // produce a random number for the card sets (we want to select a random card set)
            randomNumber = random_int(0, cardSets.length());
            // select a random equivalence card set
            JSONArray randomCardSet = cardSets.getJSONArray(randomNumber);
            // equivalenceCardSetHashCode describes the current card set
            // shuffle the selected card set so that we pick random cards
            Iterator it = randomCardSet.iterator();
            // categories will hold every category that has been already read so we only add one card from each category
            ArrayList categories = new ArrayList();

            while (it.hasNext()) {
                JSONObject currCard = (JSONObject) it.next();
                // if the current category has not been read before and the current card has not been already added
                if (!categories.contains(currCard.get("category"))) {
                    // if the current card is set to be unique
                    if (currCard.get("unique").equals(true) || currCard.get("unique").equals(1)) {
                        // if we haven't picked from this equivalence set
                        if (equivalenceCardSetHashCodes.contains(currCard.get("equivalenceCardSetHashCode"))) {
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
            if (cardsListTemp.size() > 0) {
                extractedCards.addAll(cardsListTemp);
                cardCount += cardsListTemp.size();
                equivalenceCardSetHashCodes.add(cardsListTemp.get(0).get("equivalenceCardSetHashCode"));
            }
        }
        return extractedCards;
    }


    /**
     * given a JSONArray of card objects, assign a unique hash code (for the equivalence set) to each card.
     *
     * @param cardSets the set of cards
     * @return the set of cards with the hash codes
     */
    public JSONArray assignHashCodesToCardsSets(JSONArray cardSets) {
        Iterator it = cardSets.iterator();
        while (it.hasNext()) {
            String equivalenceCardSetHashCode = randomString();
            JSONArray currSet = (JSONArray) it.next();
            Iterator itCards = currSet.iterator();
            while (itCards.hasNext()) {
                JSONObject currCard = (JSONObject) itCards.next();
                currCard.put("equivalenceCardSetHashCode", equivalenceCardSetHashCode);
            }
        }
        return cardSets;
    }


    /**
     * Produce a random integer in [Min - Max) set
     *
     * @param Min the minimum number
     * @param Max the maximum number
     * @return a random integer in [Min - Max)
     */
    private int random_int(int Min, int Max) {
        return (int) (Math.random() * (Max - Min)) + Min;
    }

    /**
     * Produces a random String.
     *
     * @return a random String object
     */
    private String randomString() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public List<Card> getCards() {
        return cards;
    }
}
