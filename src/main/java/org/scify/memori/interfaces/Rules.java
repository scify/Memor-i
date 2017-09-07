
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

import org.scify.memori.enums.GameType;
import org.scify.memori.card.CategorizedCard;

import java.awt.geom.Point2D;
import java.util.Map;

public interface Rules {
    GameState getInitialState(GameType gameType);
    GameState getInitialState(Map<CategorizedCard, Point2D> givenGameCards, GameType gameType);
    GameState getNextState(GameState gsCurrent, UserAction uaAction);
    boolean isGameFinished(GameState gsCurrent);
}
