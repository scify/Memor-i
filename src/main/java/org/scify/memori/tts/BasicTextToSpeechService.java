package org.scify.memori.tts;

public class BasicTextToSpeechService implements TextToSpeechService {

    protected static BasicTextToSpeechService instance = null;

    public static BasicTextToSpeechService getInstance() {
        if (instance == null)
            instance = new BasicTextToSpeechService();
        return instance;
    }

    private BasicTextToSpeechService() {

    }

    @Override
    public void speak(String s, String langCode) {
        System.out.println("Speaking: " + s + " in lang: " + langCode);
    }

    @Override
    public void postGameStatus(String status) {
        System.out.println("Status: " + status);
    }

}
