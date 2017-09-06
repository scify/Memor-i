package org.scify.memori.card;

import com.google.gson.annotations.Expose;

/**
 * CategorizedCard is a class that represents a Card with a category.
 * This card belongs to an equivalence card set
 * Created by pisaris on 10/10/2016.
 */
public class CategorizedCard extends Card{

    /**
     * the category that the cards belongs to
     */
    @Expose
    private String category;

    /**
     * the equivalence Card Set identifier (hash code) that the card belongs to
     */
    private String equivalenceCardSetHashCode;

    // number 1-100 describing the probability for the description sound to be played, after the card has found
    //TODO: implement
    protected int descriptionSoundProbability;

    public String getCategory() {
        return category;
    }

    public String getEquivalenceCardSetHashCode() {
        return equivalenceCardSetHashCode;
    }

    public CategorizedCard(String label, String[] images, String[] sounds, String category, String equivalenceCardSetHashCode, String descriptionSound, int descriptionSoundProbability, int terrainWidth) {
        super(label, images, sounds, descriptionSound, descriptionSoundProbability, terrainWidth);
        this.category = category;
        this.equivalenceCardSetHashCode = equivalenceCardSetHashCode;
    }
}
