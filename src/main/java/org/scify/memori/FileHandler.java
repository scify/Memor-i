
/**
 * Copyright 2016 SciFY.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scify.memori;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Responsible for interacting with files
 * Files used by this program are: a) high score file and b) the json representation of the DB holding all cards (animals)
 */
public class FileHandler {

    /**
     * The file that represents the DB
     */
    private String propertiesFile;

    public FileHandler() {
        String userDir;
        if ((System.getProperty("os.name")).toUpperCase().contains("WINDOWS")) {
            userDir = System.getenv("AppData");
        } else {
            userDir = System.getProperty("user.dir");
        }
        this.propertiesFile = userDir + File.separator + "high_scores.properties";
    }

    public ArrayList<JSONObject> readCardsFromJSONFile() {
        //  each DB "row" will be represented as a Map of String (id) to a Map of Strings (the card attributes, like sound and image)
        ArrayList<JSONObject> cards = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner( new InputStreamReader(getClass().getClassLoader().getResourceAsStream("json_DB/cards.json")));
            String jsonStr = scanner.useDelimiter("\\A").next();

            JSONObject rootObject = new JSONObject(jsonStr); // Parse the JSON to a JSONObject
            JSONArray rows = rootObject.getJSONArray("cards"); // Get all JSONArray rows
            // shuffle the rows (we want the cards to be in a random order)
            rows = shuffleJsonArray(rows);
            ArrayList<JSONObject> tempMap;
            Iterator it = rows.iterator();
            /**
             * The number of cards we need depends on the level (number of rows and columns)
             * divided by the number of the card tuple we want to form (2-card patterns, 3-card patterns, etc)
             */
            int numOfCards = (MainOptions.NUMBER_OF_COLUMNS * MainOptions.NUMBER_OF_ROWS) / MainOptions.NUMBER_OF_OPEN_CARDS;
//            for(int i = 0; i < numOfCards; i++) { // Loop over each each row
//                JSONObject cardObj = rows.getJSONObject(i); // Get row object
//                tempMap = new ArrayList<>();
//                JSONObject cardAttrs = cardObj.getJSONObject("attrs");
//                tempMap.add(0, (JSONObject) cardAttrs.get("images"));
//                tempMap.add(1, (JSONObject) cardAttrs.getString("sounds"));
//                tempMap.add(2, (JSONObject) cardAttrs.getString("description_sound"));
//                map.put(cardObj.getString("label"), tempMap);
//            }

            int cardIndex = 0;
            while(it.hasNext()) {
                if(cardIndex < numOfCards) {
                    JSONObject currCard = (JSONObject) it.next();
                    cards.add(currCard);
                } else {
                    break;
                }
                cardIndex ++;
            }

        } finally {
            scanner.close();
        }
        return cards;
    }

    public static JSONArray shuffleJsonArray (JSONArray array) throws JSONException {
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


    public String readHighScoreForCurrentLevel() {
        String highScore = "";
        Properties prop = new Properties();

        File scoresFile = new File(propertiesFile);
        try {
            if(!scoresFile.exists())
                scoresFile.createNewFile();
            FileInputStream in = new FileInputStream(scoresFile);
            prop.load(in);
            highScore = prop.getProperty(String.valueOf(MainOptions.gameLevel));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return highScore;
    }

    /**
     * Reads the current high score for a given game level and the current game.
     * @param level the given game level (1,2,... etc)
     * @return the high score (can be null if no high score)
     */
    public String readHighScoreForLevel(String level) {
        return getPropertyByName(propertiesFile, level);
    }

    /**
     * Sets a given high score for the current game level and the current game.
     * @param highScore the high score to be set.
     */
    public void setHighScoreForLevel (String highScore) {
        setPropertyByName(propertiesFile, String.valueOf(MainOptions.gameLevel), highScore);
    }

    /**
     * Get a variable from project.properties file
     * @param propertyName the name of the property
     * @return the value of the given property
     */
    public String getPropertyByName(String propertyFile, String propertyName) {
        Properties props = new Properties();
        File propertiesFile = new File(propertyFile);
        try {
            if (!propertiesFile.exists())
                propertiesFile.createNewFile();
            FileInputStream in = new FileInputStream(propertiesFile);
            props.load(in);
            return props.getProperty(String.valueOf(propertyName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets a property identified by it's name, to a given value
     * @param propertyFilePath the properties file
     * @param propertyName the name of the property
     * @param propertyValue the value that the property will be set to.
     */
    public void setPropertyByName(String propertyFilePath, String propertyName, String propertyValue) {
        Properties props = new Properties();
        FileInputStream in = null;

        File propertyFile = new File(propertyFilePath);
        try {

            if(!propertyFile.exists())
                propertyFile.createNewFile();
            in = new FileInputStream(propertyFile);
            props.load(in);
            in.close();
            FileOutputStream out = new FileOutputStream(propertyFile);

            props.setProperty(propertyName, propertyValue);
            props.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the default user directory for the current architecture
     */
    public String getUserDir() {
        String userDir;
        if ((System.getProperty("os.name")).toUpperCase().contains("WINDOWS")) {
            userDir = System.getenv("AppData");
        } else {
            userDir = System.getProperty("user.dir");
        }
        return userDir + File.separator;
    }
}
