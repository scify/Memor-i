package org.scify.memori.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Responsible for dealing with JSON files.
 */
public class JSONFileHandler {


    /**
     * Shuffles the contents of a {@link JSONArray}. Mutates the initial array.
     * @param array the initial array
     * @return the shuffled array
     * @throws JSONException if trying to access an element out of bounds, for example.
     */
    public JSONArray shuffleJsonArray (JSONArray array) throws JSONException {
        // Implementing Fisher–Yates shuffle
        Random rnd = new Random();
        for (int i = array.length() - 1; i >= 0; i--)
        {
            int j = rnd.nextInt(i + 1);
            // Simple swap
            Object object = array.get(j);
            array.put(j, array.get(i));
            array.put(i, object);
        }
        return array;
    }


    /**
     * Given a JSONObject and a name of an array inside this object, get the array
     * @param arrayName the array name
     * @return the shuffled set
     */
    public JSONArray getJSONArrayFromObject(JSONObject object, String arrayName) {
        JSONArray jsonArray = object.getJSONArray(arrayName); // Get all JSONArray rows
        // shuffle the rows (we want the cards to be in a random order)
        return jsonArray;
    }


    /**
     * Parses a {@link JSONArray} elements to a String array
     * @param jsonArray the JSON formatted array ( eg ["1", "2"] )
     * @return a String array containing the elements of the JSON array
     */
    public String[] jsonArrayToStringArray(JSONArray jsonArray){
        String[] stringArray = null;
        int length = jsonArray.length();
        if(jsonArray!=null){
            stringArray = new String[length];
            for(int i=0;i<length;i++){
                stringArray[i]= jsonArray.optString(i);
            }
        }
        return stringArray;
    }


}
