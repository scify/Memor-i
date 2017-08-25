
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

import com.google.gson.annotations.Expose;
import org.scify.memori.helper.PropertyHandlerImpl;

import java.io.File;
import java.util.UUID;

public class Player {
    @Expose
    String id;

    protected int score;
    @Expose
    protected String name;

    private PropertyHandler propertyHandler;
    @Expose
    private static String userDataFile;

    public Player(String playerName) {
        propertyHandler = new PropertyHandlerImpl();
        this.name = playerName;
        String userDir;
        if ((System.getProperty("os.name")).toUpperCase().contains("WINDOWS")) {
            userDir = System.getenv("AppData");
        } else {
            userDir = System.getProperty("user.dir");
        }
        userDataFile = userDir + File.separator + ".user_data.properties";

        if(this.playerHasId()) {
            this.id = getId();
        } else {
            this.id = UUID.randomUUID().toString();
            storeId();
        }
    }

    public String getId() {
        return propertyHandler.getPropertyByName(userDataFile, this.name + "_" + "user_id");
    }

    private void storeId() {
        propertyHandler.setPropertyByName(userDataFile, this.name + "_" + "user_id", this.id);
    }

    private boolean playerHasId() {
        return propertyHandler.getPropertyByName(userDataFile, this.name + "_" + "user_id") != null;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int iNewScore) {
        score = iNewScore;
    }

    public String getName() {
        return name;
    }

}
