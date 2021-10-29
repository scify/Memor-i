package org.scify.memori.tts;

import org.scify.memori.helper.MemoriConfiguration;

import java.util.Locale;

public class TTSFacade {
    protected static TextToSpeechService textToSpeechService = ARIRobotTextToSpeechService.getInstance();

    public static void speak(String s) {
        MemoriConfiguration configuration = MemoriConfiguration.getInstance();
        Locale locale = new Locale(configuration.getDataPackProperty("APP_LANG"));
        textToSpeechService.speak(s, String.valueOf(locale));
    }
}
