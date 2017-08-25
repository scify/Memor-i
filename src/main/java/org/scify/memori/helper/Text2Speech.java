package org.scify.memori.helper;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Text2Speech {
    private static final String VOICENAME_kevin = "kevin16";
    private String text; // string to speech

    public Text2Speech(String text) {
        this.text = text;
    }

    public void speak() {
        Voice voice;
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(VOICENAME_kevin);
        voice.allocate();
        voice.speak(text);
    }

}
