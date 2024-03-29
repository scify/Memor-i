
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

import org.scify.memori.helper.DefaultExceptionHandler;
import org.scify.memori.helper.PropertyHandlerImpl;
import org.scify.memori.helper.TimeWatch;
import org.scify.memori.helper.Utils;
import org.scify.memori.interfaces.HighScoresHandler;
import org.scify.memori.interfaces.PropertyHandler;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HighScoresHandlerImpl implements HighScoresHandler{

    /**
     * The file that the high scores are set and get from
     */
    private static String highScoresFile;
    /**
     * The high scores of this application are stored using a property-like way
     */
    private PropertyHandler propertyHandler;

    public HighScoresHandlerImpl() {
        propertyHandler = new PropertyHandlerImpl();

        String userDir;
        if (Utils.isWindows()) {
            userDir = System.getenv("AppData");
        } else {
            userDir = System.getProperty("user.dir");
        }
        highScoresFile = userDir + "/high_scores.properties";
    }

    public void updateHighScore(TimeWatch watch) {
        long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
        String highScore = readHighScoreForLevel(String.valueOf(MainOptions.GAME_LEVEL_CURRENT));
        if (highScore == null || Objects.equals(highScore, ""))
            highScore = "99:00:00";
        System.out.println("highScore " + TimeToSeconds(highScore));
        System.out.println("time: " + passedTimeInSeconds);
        if(passedTimeInSeconds < TimeToSeconds(highScore))
            this.setHighScoreForLevel(String.valueOf(ConvertSecondToHHMMSSString((int) passedTimeInSeconds)));
    }

    public String getHighScoreForLevel(int level) {
        return this.readHighScoreForLevel(String.valueOf(level));
    }

    private String ConvertSecondToHHMMSSString(int nSecondTime) {
        return LocalTime.MIN.plusSeconds(nSecondTime).toString();
    }


    public String readHighScoreForLevel(String level) {
        return propertyHandler.getPropertyByName(highScoresFile, level);
    }

    public void setHighScoreForLevel (String highScore) {
        propertyHandler.setPropertyByName(highScoresFile, String.valueOf(MainOptions.GAME_LEVEL_CURRENT), highScore);
    }


    private long TimeToSeconds(String time) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date reference = null;
        Date scoreDate = null;
        try {
            scoreDate = dateFormat.parse(time);
            reference = dateFormat.parse("00:00:00");
        } catch (ParseException e) {
            DefaultExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }
        return (scoreDate.getTime() - reference.getTime()) / 1000L;
    }
}
