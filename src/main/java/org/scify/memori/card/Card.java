
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

package org.scify.memori.card;

import com.google.gson.annotations.Expose;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.scify.memori.MainOptions;
import org.scify.memori.helper.ResourceLocator;
import org.scify.memori.helper.MemoriConfiguration;
import org.scify.memori.interfaces.Tile;


/**
 * Implements the {@link Tile} representation in the game.
 * The user can move on tiles and flip them
 */
public class Card implements Tile{

    /**
     * the button element that binds the card with the UI layout
     */
    private Button button;
    /**
     * the type of the card. Cards with the same type are considered identical
     */
    @Expose
    private String label;
    /**
     * file name of the image associated with the card
     */
    private String[] images;
    /**
     * whether the Card has been flipped
     */
    private boolean isFlipped;
    /**
     * whether the Card has been won
     */
    private boolean isWon;
    /**
     * file name of the sound associated with the card
     */
    private String[] sounds;

    /**
     * file name of the card name sound
     */
    private String cardDescriptionSound;
    private int cardDescriptionSoundProbability;

    @Expose
    public int xPos;
    @Expose
    public int yPos;

    protected String cardImageBasePath;

    protected String cardSoundsBasePath;

    protected String cardDescriptionSoundBasePath;



    protected String imagesBasePath;

    protected static ResourceLocator resourceLocator = new ResourceLocator();


    /**
     *
     * @return the Node (Button) that is laid on the layout
     */
    public Button getButton() {
        return button;
    }

    /**
     * Checks if the card is won
     * @return true if the card is won
     */
    @Override
    public boolean getWon() {
        return isWon;
    }

    /**
     * sets a card as won
     */
    @Override
    public void setWon() {
        isWon = true;
    }

    public int getCardDescriptionSoundProbability() {
        return cardDescriptionSoundProbability;
    }

    /**
     * Checks if the card is flipped
     * @return true if the card is flipped
     */
    @Override
    public boolean getFlipped() {
        return isFlipped;
    }

    public String getLabel() {
        return label;
    }

    public Card(String label, String[] images, String[] sounds, String cardDescriptionSound, int cardDescriptionSoundProbability) {

        MemoriConfiguration configuration = new MemoriConfiguration();

        this.images = images;
        this.button = new Button();
        this.sounds = sounds;
        this.cardDescriptionSound = cardDescriptionSound;
        this.cardDescriptionSoundProbability = cardDescriptionSoundProbability;
        this.button.setId(label);
        // each card takes a dynamic height and width, based on the height and with of the screen

        // apply the appropriate style classes
        this.button.getStyleClass().addAll("cardButton", "closedCard");
        
        this.label = label;
        this.isWon = false;
        this.isFlipped = false;
        this.imagesBasePath =  configuration.getProjectProperty("IMAGES_BASE_PATH");
        this.cardImageBasePath = configuration.getProjectProperty("CARD_IMAGE_BASE_PATH");
        this.cardSoundsBasePath = configuration.getProjectProperty("CARD_SOUND_BASE_PATH");
        this.cardDescriptionSoundBasePath = configuration.getProjectProperty("CARD_NAME_SOUND_BASE_PATH");

        flipBackUI();
    }

    public void setCardWidth(double terrainWidth) {
        double width = MainOptions.mWidth/terrainWidth - ((MainOptions.mWidth/terrainWidth) * 0.1);
        this.button.setPrefHeight(width * 0.6666);
        this.button.setPrefWidth(width);
    }

    public void turnCard() {
        RotateTransition rotator = createRotator(this.getButton());
        rotator.play();
    }

    private RotateTransition createRotator(Node card) {
        RotateTransition rotator = new RotateTransition(Duration.millis(1000), card);
        rotator.setAxis(Rotate.Y_AXIS);
        rotator.setFromAngle(0);
        rotator.setToAngle(360);
        rotator.setInterpolator(Interpolator.EASE_BOTH);
        rotator.setCycleCount(1);

        return rotator;
    }

    @Override
    public String getTileType() {
        return label;
    }


    @Override
    public void flip() {
        isFlipped = !isFlipped;
    }

    /**
     * function to set the UI of the flipped card (change icons)
     * @param imgIndex the index of the image
     */
    public void flipUI(int imgIndex) {
        // only if this image exists
        if(imgIndex < images.length) {
            String imgFile = resourceLocator.getCorrectPathForFile(this.imagesBasePath + this.cardImageBasePath, images[imgIndex]);
            button.setStyle("-fx-background-image: url(" + imgFile + ")");
        }
    }
    /**
     * function to set the UI of the flipped back card (change icons)
     */
    public void flipBackUI () {
        String imgFile = resourceLocator.getCorrectPathForFile(this.imagesBasePath, "box.png");
        button.setStyle("-fx-background-image: url(" + imgFile +")");
    }

    /**
     * Get a random sound from the card sounds
     * @return the sound file name associated with the card
     */
    public String getRandomSound() {
        if(sounds.length != 0)
            return cardSoundsBasePath + sounds[random_int(0, sounds.length)];
        return null;
    }

    public String getDescriptionSound() {
        return cardDescriptionSoundBasePath + cardDescriptionSound;
    }

    private int random_int(int Min, int Max) {
        return (int) (Math.random()*(Max-Min))+Min;
    }
}
