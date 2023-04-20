package org.scify.memori.game_flavor;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.MemoriConfiguration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameFlavorService {

    public static List<GameFlavor> gameFlavors = new ArrayList<>();
    private static GameFlavorService instance = null;

    private GameFlavorService() {

    }

    public static GameFlavorService getInstance() {
        if (instance == null)
            instance = new GameFlavorService();
        return instance;
    }

    public static void reset() {
        gameFlavors = new ArrayList<>();
    }

    public List<GameFlavor> getGameFlavors() {
        if (gameFlavors.isEmpty())
            gameFlavors = getGameFlavorsFromServer();
        return gameFlavors;
    }

    public List<GameFlavor> getGameFlavorsFromServer() {
        JSONArray objectSets;
        List<GameFlavor> gameFlavors = new ArrayList<>();
        try {
            JSONObject rootObject = new JSONObject(IOUtils.toString(new URL(MemoriConfiguration.getInstance().getDataPackProperty("SERVER_URL") + "games?lang=" + MemoriConfiguration.getInstance().getDataPackProperty("APP_LANG")), StandardCharsets.UTF_8));
            objectSets = rootObject.getJSONArray("data");
            for (Object objectSet : objectSets) {
                JSONObject currGameFlavor = (JSONObject) objectSet;
                GameFlavor gameFlavor = new GameFlavor();
                gameFlavor.id = currGameFlavor.getInt("id");
                gameFlavor.name = currGameFlavor.getString("name");
                gameFlavor.description = currGameFlavor.getString("description");
                gameFlavor.coverImgFilePath = currGameFlavor.getString("cover_img_file_path");
                gameFlavor.equivalenceSetFilePath = currGameFlavor.getString("equivalence_set_file_path");
                gameFlavors.add(gameFlavor);
            }
        } catch (IOException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }

        return gameFlavors;
    }

    public GameFlavor getGameFlavor(int gameFlavorId) {
        return gameFlavors.stream().filter(gameFlavor -> gameFlavorId == gameFlavor.id).findFirst().orElse(null);
    }
}
