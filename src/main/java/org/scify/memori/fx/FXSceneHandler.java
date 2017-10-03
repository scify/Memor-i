
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

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scify.memori.interfaces.GameEvent;
import org.scify.memori.screens.InvitePlayerScreen;

import java.util.*;

/**
 * Handles the scenes changing across the application.
 */
public class FXSceneHandler {
    public FXSceneHandler() {
    }

    /**
     * Main window that the application runs on (this application is a one-window application)
     */
    private Stage mainWindow;
    /**
     * History af scenes created and used in the application (ability to go back to a certain scene)
     */
    private Stack<Scene> allScenes = new Stack<>();

    public Stage getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Set a given scene as the active one
     * @param sToPush the scene we want to make active
     */
    public void pushScene(Scene sToPush) {
        allScenes.push(sToPush);
        // Set the added scene as active

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainWindow.setScene(sToPush);
            }
        });
    }

    /**
     * Removes the last scene from the scenes list and sets the previous one as active
     * @return the last scene from the scenes list
     */
    public Scene popScene() {
        Scene sToPop = allScenes.pop(); // Get last added
        if(sToPop != null) {
            // update active scene IN FX ΤΗΕΜΕ
            Scene lastSceneInStack = allScenes.peek();
            Platform.runLater(() -> mainWindow.setScene(lastSceneInStack));
            // WARNING: The above call is asynchronous, so we are not CERTAIN that
            // the mainWindow has already switched to the previous scene, at this point
            // in time.

            // return removed scene
        }
        return sToPop;
    }

    /**
     * Removes the last scene from the scenes list and sets the previous one as active
     */
    public Scene popToScene(Scene scene) {
        List<Scene> eventsList = Collections.synchronizedList(allScenes);
        Collections.reverse(eventsList);
        ListIterator<Scene> listIterator = eventsList.listIterator();
        while (listIterator.hasNext()) {
           Scene currScene = listIterator.next();
            if(currScene.equals(scene)) {
                Platform.runLater(() -> mainWindow.setScene(currScene));
                return scene;
            } else {
                listIterator.remove();
            }
        }
        return null;
    }

    /**
     * Removes the last scene from the scenes list
     */
    public void simplePopScene() {
        allScenes.pop();
    }
}
