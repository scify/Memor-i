package org.scify.memori.tts;

import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.network.RequestManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ARIRobotTextToSpeechService implements TextToSpeechService {

    protected static ARIRobotTextToSpeechService instance = null;
    protected static String URL = null;
    protected RequestManager requestManager;
    protected Map<String, String> languages;

    public static ARIRobotTextToSpeechService getInstance() {
        if (instance == null)
            instance = new ARIRobotTextToSpeechService();
        return instance;
    }

    private ARIRobotTextToSpeechService() {
        requestManager = new RequestManager();
        languages = new HashMap<String, String>() {{
            put("en", "en_GB");
            put("el", "el_GR");
            put("es", "es_ES");
            put("no", "en_GB");
            put("it", "it_IT");
        }};
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        URL = configuration.getPropertyByName("TTS_URL");
    }

    @Override
    public void speak(String s, String langCode) {
        MemoriLogger.LOGGER.log(Level.INFO, "Speaking: " + s + " in: " + languages.get(langCode));
        String payload = "{\"rawtext\": {\"text\":\"" + s + "\", \"lang_id\":\"" + languages.get(langCode) + "\"}}";
        String res = this.requestManager.doPost(URL + "tts", payload);
        MemoriLogger.LOGGER.log(Level.INFO, "ROBOT API RESULT: " + res);
    }

    @Override
    public void postGameStatus(String status) {
        String payload = "{\"game_state\": \"" + status + "\"}";
        String res = this.requestManager.doPost(URL + "memori_state", payload);
        MemoriLogger.LOGGER.log(Level.INFO, "ROBOT API STATUS RESULT: " + res);
    }

}
