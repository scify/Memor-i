package org.scify.memori;

import org.scify.memori.card.MemoriCardService;
import org.scify.memori.helper.MemoriConfiguration;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class GameLevelService {

    private ArrayList<Point2D> gameDimensionsPlayable = new ArrayList<>();
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
        gameDimensions.add(new Point2D.Double(6,6));
        gameDimensions.add(new Point2D.Double(6,7));
        gameDimensions.add(new Point2D.Double(6,8));

        for(Point2D dimensions: gameDimensions) {
            if((int)dimensions.getX() * (int)dimensions.getX() <= cardService.getNumberOfCards()) {
                gameDimensionsPlayable.add(dimensions);
            } else {
                break;
            }
        }
    }


    /**
     * The available game levels are computed based on the level intro sounds directory
     * (Given that each game level has EXACTLY one intro sound)
     * @return
     */
    public List<MemoriGameLevel> getAllLevels() {
        MemoriConfiguration configuration = new MemoriConfiguration();
        ResourceLocator resourceLocator = new ResourceLocator();

        String levelIntroSoundPath = configuration.getProjectProperty("LEVEL_INTRO_SOUNDS");
        String levelNameSoundPath = configuration.getProjectProperty("LEVEL_NAME_SOUNDS");

        String packageName = configuration.getProjectProperty("DATA_PACKAGE");
        String audiosBasePath = configuration.getProjectProperty("AUDIOS_BASE_PATH");

        //the number of sounds in the level_name_sounds directory is the name of the levels available for the current game version
        ArrayList<String> levelIntroductorySounds = (ArrayList<String>) resourceLocator.getResourcesFromDirectory("/" + packageName + "/" + audiosBasePath + levelIntroSoundPath);
        ArrayList<String> levelNameSounds = (ArrayList<String>) resourceLocator.getResourcesFromDirectory("/" + packageName + "/" + audiosBasePath + levelNameSoundPath);

        int levelIndex = 0;
        ArrayList<MemoriGameLevel> gameLevels = new ArrayList<>();
        for(Point2D levelDimensions : gameDimensionsPlayable) {
            MemoriGameLevel memoriGameLevel = new MemoriGameLevel(
                    levelIndex,
                    levelDimensions,
                    (int) levelDimensions.getX() + "x" + (int) levelDimensions.getY(),
                    levelNameSoundPath + levelNameSounds.get(levelIndex),
                    levelIntroSoundPath + levelIntroductorySounds.get(levelIndex)
            );
            gameLevels.add(memoriGameLevel);
            levelIndex++;
        }
        return gameLevels;
    }


}