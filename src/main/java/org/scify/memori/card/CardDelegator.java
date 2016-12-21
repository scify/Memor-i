package org.scify.memori.card;

import org.json.JSONArray;
import org.json.JSONObject;
import org.scify.memori.helper.JSONFileHandler;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.CardDBHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for loading the cards for the game
 */
public class CardDelegator {

    private CardDBHandler cardDBHandler;

    private String dbFile;

    public CardDelegator() {
        cardDBHandler = new CardDBHandlerJSON();
        MemoriConfiguration configuration = new MemoriConfiguration();
        dbFile = configuration.getProjectProperty("DATA_PACKAGE") + "/json_DB/equivalence_cards_sets.json";
    }



    public List<Card> getCards() {
        ArrayList<Object> initialCardSet = cardDBHandler.getCardsFromDBFile(dbFile);
        JSONFileHandler jsonFileHandler = new JSONFileHandler();
        List<Card> cardSet = new ArrayList<>();
        Iterator it = initialCardSet.iterator();
        while(it.hasNext()) {
            JSONObject currObj = (JSONObject) it.next();
            Card newCard = new CategorizedCard(
                    (String) currObj.get("label"),
                    jsonFileHandler.jsonArrayToStringArray((JSONArray) currObj.get("images")),
                    jsonFileHandler.jsonArrayToStringArray((JSONArray) currObj.get("sounds")),
                    (String)currObj.get("category"),
                    (String)currObj.get("equivalenceCardSetHashCode"),
                    (String)currObj.get("description_sound")
            );
            cardSet.add(newCard);
        }
        return cardSet;
    }

}
