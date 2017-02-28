
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
import org.scify.memori.ResourceLocator;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.helper.MemoriLogger;
import org.scify.memori.interfaces.AudioEngine;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Responsible for handling Audio events
 */
public class FXAudioEngine implements AudioEngine{

    private Media audioMedia;
    private MediaPlayer audioMediaPlayer;
    private MediaPlayer concurrentAudioMediaPlayer;

    private String soundBasePath;
    private String numBasePath;
    private String letterBasePath;
    private ArrayList<MediaPlayer> playingAudios = new ArrayList<>();
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
        if(audioMediaPlayer != null)
            audioMediaPlayer.stop();
    }

    /**
     * Plays a sound describing a certain movement on the UI layout.
     * We use {@link MediaPlayer} instead of {@link AudioClip} because of a bug of the former in Windows
     * (when playing in left or right headphone, the clip plays unstoppably)
     *
     * @param balance left/right panning value of the sound
     * @param rate indicates how fast the sound will be playing. Used to distinguish vertical movements
     */
    public void playMovementSound(double balance, double rate) {
        pauseSound();
        audioMedia = new Media(FXAudioEngine.class.getResource(resourceLocator.getCorrectPathForFile(this.soundBasePath, "miscellaneous/movement_sound.mp3")).toExternalForm());
        concurrentAudioMediaPlayer = new MediaPlayer(audioMedia);
        concurrentAudioMediaPlayer.setBalance(balance);
        concurrentAudioMediaPlayer.setRate(rate);
        //Windows bug: when the sound is completed, if the rate has changed it keeps playing.
        //so, we need to force it to stop by overriding OnEndOfMedia method.
        concurrentAudioMediaPlayer.setOnEndOfMedia(() -> concurrentAudioMediaPlayer.stop());
        concurrentAudioMediaPlayer.play();
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
        playSound("miscellaneous/success.mp3", true);
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
     * Plays an appropriate sound associated with a failure Game Event
     */
//    public void playFailureSound() {
//        pauseAndPlaySound(failureSound, false);
//    }

    @Override
    public void playSound(String soundFile) {
        playSound(soundFile, false);
    }

    /**
     * Plays a sound given a certain balance
     * @param balance the desired balance
     * @param soundFile the file name (path) of the audio clip
     */
    public void playBalancedSound(double balance, String soundFile, boolean isBlocking) {
        audioMedia = new Media(FXAudioEngine.class.getResource(resourceLocator.getCorrectPathForFile(this.soundBasePath, soundFile)).toExternalForm());
        concurrentAudioMediaPlayer = new MediaPlayer(audioMedia);
        concurrentAudioMediaPlayer.setBalance(balance);
        //Windows OS bug: when the sound is completed, if the rate has changed it keeps playing.
        //so, we need to force it to stop by overriding OnEndOfMedia method.
        concurrentAudioMediaPlayer.setOnEndOfMedia(() -> concurrentAudioMediaPlayer.stop());
        concurrentAudioMediaPlayer.play();
        if (isBlocking) {
            blockUIThread(concurrentAudioMediaPlayer);
        }
    }
    boolean playing;

    /**
     * Plays a sound given a sound file path
     * @param soundFilePath the file name (path) of the audio clip
     * @param isBlocking whether the player should block the calling {@link Thread} while the sound is playing
     */
    public void playSound(String soundFilePath, boolean isBlocking) {

        String fileResourcePath = resourceLocator.getCorrectPathForFile(this.soundBasePath, soundFilePath);
        System.out.println("Playing: " + fileResourcePath);
        try {
            audioMedia = new Media(FXAudioEngine.class.getResource(fileResourcePath).toExternalForm());
            audioMediaPlayer = new MediaPlayer(audioMedia);
            audioMediaPlayer.play();
        } catch (Exception e) {
            MemoriLogger.LOGGER.log(Level.SEVERE, "error loading sound for: " + soundFilePath + ". Queried path was: " + fileResourcePath);
            System.err.println("error loading sound for: " + soundFilePath + ". Queried path was: " + fileResourcePath);
            return;
        }
        playingAudios.add(audioMediaPlayer);
        playing = true;
        if (isBlocking) {
            blockUIThread(audioMediaPlayer);
        }
    }

    private void blockUIThread(MediaPlayer mediaPlayer) {
        System.out.println("Waiting for blocking sound to complete");
        // Wait until completion
        mediaPlayer.setOnEndOfMedia(() -> {
            System.err.println("completed!");
            playing = false;
        });
        while (playing) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Sound still playing");
        }
        System.out.println("Sound completed");
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
        for (MediaPlayer audio: playingAudios) {
            if(audio.getStatus().equals(MediaPlayer.Status.PLAYING))
                audio.stop();
        }
    }

}
