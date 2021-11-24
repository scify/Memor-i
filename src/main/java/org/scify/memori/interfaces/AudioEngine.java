
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

package org.scify.memori.interfaces;


public abstract class AudioEngine {
    abstract public void playSound(String sSoundType, boolean isBlocking);

    public void playSound(String soundFile) {
        playSound(soundFile, false);
    }

    abstract public void playBalancedSound(double balance, String soundFile, boolean isBlocking);

    abstract public void pauseCurrentlyPlayingAudios();

    abstract public void pauseAndPlaySound(String s, boolean b);

    abstract public void playNumSound(int minutes);

    public abstract void playSuccessSound();

    public abstract void playLetterSound(int number);

    public abstract void playMovementSound(double soundBalance, double rate);

    public abstract void playInvalidMovementSound(double soundBalance, boolean isBlocking);
}
