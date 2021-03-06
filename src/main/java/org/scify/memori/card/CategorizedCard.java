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

    public String getCategory() {
        return category;
    }

    public String getEquivalenceCardSetHashCode() {
        return equivalenceCardSetHashCode;
    }

    public CategorizedCard(String label, String[] images, String[] sounds, String category, String equivalenceCardSetHashCode, String descriptionSound, int descriptionSoundProbability) {
        super(label, images, sounds, descriptionSound, descriptionSoundProbability);
        this.category = category;
        this.equivalenceCardSetHashCode = equivalenceCardSetHashCode;
        this.button.setId(label + "_" + category);
    }
}
