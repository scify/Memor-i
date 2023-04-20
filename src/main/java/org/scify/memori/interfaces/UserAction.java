
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
import org.scify.memori.helper.MemoriConfiguration;

import java.awt.geom.Point2D;

public class UserAction {
    @Expose
    private
    String actionType;
    private Point2D coords;
    @Expose
    private String direction;

    @Expose protected long timestamp;

    protected boolean isKeyboardEvent;


    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setCoords(Point2D coords) {
        this.coords = coords;
    }

    public String getDirection() {
        return direction;
    }

    public UserAction(String sType, String direction) {
        actionType = sType;
        this.direction = direction;
        this.timestamp = System.currentTimeMillis();
    }

    public UserAction(String sType, String direction, long timestamp) {
        actionType = sType;
        this.direction = direction;
        this.timestamp = timestamp;
    }

    public Point2D getCoords() {
        return coords;
    }

    public String getActionType() {
        return actionType;
    }

    public boolean isKeyboardEvent() {
        return isKeyboardEvent;
    }

    public void setIsKeyboardEvent(boolean keyboardEvent) {
        isKeyboardEvent = keyboardEvent;
    }
}
