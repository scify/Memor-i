package org.scify.memori;

import org.scify.memori.interfaces.GameLevel;

import java.awt.geom.Point2D;

/**
 * Description of a game level for the Memori game
 *
 */
public class MemoriGameLevel implements GameLevel{
    /**
     * the code associated with each level (1, 2, etc)
     */
    protected int levelCode;
    /**
     * In memor-i, each level is associated with dimesions for the level's terrain
     */
    protected Point2D dimensions;
    /**
     * The name of the level (eg 2x3)
     */
    protected String levelName;
    /**
     * Each game level has a discriptive sound playing upon user interaction with the level button
     * (in order for a blind person to know which level is there)
     */
    protected String introScreenSound;

    /**
     * Introductory sound for the game lavel. Plays when the game is loaded.
     */
    protected String introSound;

    public MemoriGameLevel(int levelCode, Point2D dimensions, String levelName, String introScreenSound, String introSound) {
        this.levelCode = levelCode;
        this.dimensions = dimensions;
        this.levelName = levelName;
        this.introScreenSound = introScreenSound;
        this.introSound = introSound;
    }

    public int getLevelCode() {
        return levelCode;
    }

    @Override
    public Point2D getDimensions() {
        return dimensions;

    }

    public String getLevelName() {
        return levelName;
    }

    public String getIntroScreenSound() {
        return introScreenSound;
    }

    public String getIntroSound() {
        return introSound;
    }
}
