package org.scify.memori.helper;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Text2Speech {

    private static final String VOICENAME_kevin = "kevin16";
    VoiceManager voiceManager;
    Voice voice;

    public Text2Speech() {
        voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(VOICENAME_kevin);
        System.err.println(voiceManager.getVoiceInfo());
    }

    public void speak(String text) {
        voice.getAudioPlayer().cancel();
        voice.allocate();
        voice.speak(text);
    }

}
