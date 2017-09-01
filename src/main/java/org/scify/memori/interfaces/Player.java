
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
    int id;

    protected int score;
    @Expose
    protected String name;


    public Player(String playerName) {
        this.name = playerName;
    }

    public Player(int id) {
        this.id = id;
    }

    public Player(String playerName, int id) {
        this.name = playerName;
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int iNewScore) {
        score = iNewScore;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
