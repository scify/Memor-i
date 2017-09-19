package org.scify.memori;

import org.scify.memori.card.MemoriCardService;
import org.scify.memori.helper.MemoriConfiguration;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that creates game levels according to the number of available cards in the current game pack.
 */
public class GameLevelService {

    private ArrayList<Point2D> gameDimensionsPlayable = new ArrayList<>();
    private ArrayList<String> gameLevelNames = new ArrayList<>();
    private ArrayList<String> gameLevelIntroSounds = new ArrayList<>();
    private ArrayList<String> gameLevelDescriptionSounds = new ArrayList<>();
    public GameLevelService() {
        MemoriCardService cardService = new MemoriCardService();
        ArrayList<Point2D> gameDimensions = new ArrayList<>();

        gameDimensions.add(new Point2D.Double(2,3));
        gameDimensions.add(new Point2D.Double(2,4));
        gameDimensions.add(new Point2D.Double(3,4));
        gameDimensions.add(new Point2D.Double(4,4));
        gameDimensions.add(new Point2D.Double(5,4));
        gameDimensions.add(new Point2D.Double(4,6));
        gameDimensions.add(new Point2D.Double(5,6));
        gameDimensions.add(new Point2D.Double(8,8));

        int numberOfSets = cardService.getNumberOfSets();
        for(Point2D dimensions: gameDimensions) {
            if((int)dimensions.getX() * (int)dimensions.getY() <= numberOfSets) {
                gameDimensionsPlayable.add(dimensions);
            } else {
                break;
            }
        }

        //the max number of levels is the size of the game levels list
        MainOptions.MAX_NUM_OF_LEVELS = gameDimensionsPlayable.size();
        for(int i = 1; i <= gameDimensionsPlayable.size() ; i++) {
            gameLevelNames.add("level" + i + ".mp3");
            gameLevelIntroSounds.add("level" + i + ".mp3");
            gameLevelDescriptionSounds.add("level" + i + ".mp3");
        }
    }


    /**
     * The available game levels are computed based on the level intro sounds directory
     * (Given that each game level has EXACTLY one intro sound)
     * @return
     */
    public List<MemoriGameLevel> createGameLevels() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        String levelIntroSoundPath = configuration.getProjectProperty("LEVEL_INTRO_SOUNDS");
        String levelNameSoundPath = configuration.getProjectProperty("LEVEL_NAME_SOUNDS");
        String levelDescriptionSoundPath = configuration.getProjectProperty("LEVEL_DESCRIPTION_SOUNDS");
        int levelIndex = 0;
        ArrayList<MemoriGameLevel> gameLevels = new ArrayList<>();
        for(Point2D levelDimensions : gameDimensionsPlayable) {
            MemoriGameLevel memoriGameLevel = new MemoriGameLevel(
                    levelIndex + 1,
                    levelDimensions,
                    (int) levelDimensions.getX() + "x" + (int) levelDimensions.getY(),
                    levelNameSoundPath + gameLevelNames.get(levelIndex),
                    levelIntroSoundPath + gameLevelIntroSounds.get(levelIndex),
                    levelDescriptionSoundPath + gameLevelDescriptionSounds.get(levelIndex)
            );
            gameLevels.add(memoriGameLevel);
            levelIndex++;
        }
        return gameLevels;
    }


}
