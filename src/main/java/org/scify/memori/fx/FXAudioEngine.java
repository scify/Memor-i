
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

package org.scify.memori.fx;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.interfaces.AudioEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Responsible for handling Audio events
 */
public class FXAudioEngine implements AudioEngine{

    private MediaPlayer movementSoundPlayer;
    private Media movementSoundMedia;
    private AudioClip audioClip;
    private String soundBasePath;
    private String numBasePath;
    private String letterBasePath;
    private ArrayList<AudioClip> playingAudios = new ArrayList<>();
    protected static ResourceLocator resourceLocator = new ResourceLocator();

    public FXAudioEngine() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        this.soundBasePath = configuration.getProjectProperty("AUDIOS_BASE_PATH");
        this.numBasePath = configuration.getProjectProperty("NUMBER_SOUNDS_BASE_PATH");
        this.letterBasePath = configuration.getProjectProperty("LETTER_SOUNDS_BASE_PATH");
    }

    /**
     * Pauses the currently playing audio, if there is one
     */
    private void pauseSound() {
        if(audioClip != null)
            audioClip.stop();
        if(movementSoundPlayer != null)
            movementSoundPlayer.stop();
    }

    public void playMovementSound(double balance, double rate) {
        pauseSound();
        if(movementSoundMedia == null) {
            //System.err.println("construct new movement sound player");
            movementSoundMedia = new Media(FXAudioEngine.class.getResource(resourceLocator.getCorrectPathForFile(this.soundBasePath, "miscellaneous/movement_sound.mp3")).toExternalForm());
            movementSoundPlayer = new MediaPlayer(movementSoundMedia);
        }
        movementSoundPlayer.setBalance(balance);
        movementSoundPlayer.setRate(rate);
        movementSoundPlayer.setOnEndOfMedia(() -> movementSoundPlayer.stop());
        movementSoundPlayer.play();
    }

    /**
     * Plays a sound associated with a Card object
     * @param soundFile the file name (path) of the audio clip
     * @param isBlocking whether the player should block the calling Thread while the sound is playing
     */
    public void playCardSound(String soundFile, boolean isBlocking) {
        playSound(soundFile, isBlocking);
    }


    /**
     * Plays an appropriate sound associated with a successful Game Event
     */
    public void playSuccessSound() {
        pauseAndPlaySound("miscellaneous/success.mp3", true);
    }

    /**
     * Plays an appropriate sound associated with an invalid movement
     * @param balance the sound balance (left , right)
     * @param isBlocking if the event should block the ui thread
     */
    public void playInvalidMovementSound(double balance, boolean isBlocking) {
        playBalancedSound(balance, "miscellaneous/bump.mp3", isBlocking);
    }

    /**
     * Plays a sound given a certain balance
     * @param balance the desired balance
     * @param soundFile the file name (path) of the audio clip
     */
    public void playBalancedSound(double balance, String soundFile, boolean isBlocking) {
        pauseCurrentlyPlayingAudios();
        audioClip = new AudioClip(FXAudioEngine.class.getResource(resourceLocator.getCorrectPathForFile(this.soundBasePath, soundFile)).toExternalForm());
        audioClip.play(1, balance, 1, balance, 1);
        playingAudios.add(audioClip);
        if(isBlocking)
            while (audioClip.isPlaying()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
    }


    @Override
    public void playSound(String soundFile) {
        playSound(soundFile, false);
    }

    /**
     * Plays a sound given a sound file path
     * @param soundFilePath the file name (path) of the audio clip
     * @param isBlocking whether the player should block the calling {@link Thread} while the sound is playing
     */
    public void playSound(String soundFilePath, boolean isBlocking) {

        String fileResourcePath = resourceLocator.getCorrectPathForFile(this.soundBasePath, soundFilePath);
        //System.out.println("Playing: " + fileResourcePath);
        try {
            audioClip = new AudioClip(FXAudioEngine.class.getResource(fileResourcePath).toExternalForm());
            audioClip.play();
        } catch (Exception e) {
            MemoriLogger.LOGGER.log(Level.SEVERE, "error loading sound for: " + soundFilePath + ". Queried path was: " + fileResourcePath);
            System.err.println("error loading sound for: " + soundFilePath + ". Queried path was: " + fileResourcePath);
            return;
        }
        playingAudios.add(audioClip);
        if (isBlocking) {
            blockUIThread(audioClip);
        }
    }

    private void blockUIThread(AudioClip audioClip) {
        //System.out.println("Waiting for blocking sound to complete");
        // Wait until completion
        while (audioClip.isPlaying()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("Sound still playing");
        }
        //System.out.println("Sound completed");
    }

    /**
     * Pauses the currently playing sound and plays a new one
     * @param soundFile the file of the sound we want to play
     * @param isBlocking whether the sound should block the {@link Thread} while playing
     */
    public void pauseAndPlaySound(String soundFile, boolean isBlocking) {
        pauseCurrentlyPlayingAudios();
        playSound(soundFile, isBlocking);
    }

    /**
     * Plays the sound representation of a number.
     * @param number the given number
     */
    public void playNumSound(int number) {
        pauseCurrentlyPlayingAudios();
        playSound(numBasePath + String.valueOf(number) + ".mp3", true);
    }

    /**
     * PLay the sound representation of a letter.
     * @param number the number associated with the letter (e.g. 1 for A, 2 for B, etc. We do not care for capital letters or not).
     */
    public void playLetterSound(int number) {
        pauseCurrentlyPlayingAudios();
        playSound( letterBasePath + String.valueOf(number) + ".mp3", true);
    }

    /**
     * Pause any currently playing audios (every audio that is playing is stored in the playingAudios list).
     */
    public void pauseCurrentlyPlayingAudios() {
        for (AudioClip audio: playingAudios) {
            if(audio.isPlaying())
                audio.stop();
        }
    }

}
