package com.github.reline.expandablerecyclerviewsample.model;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.model.Parent;

@RealmClass
public class Recipe implements RealmModel, Parent<Ingredient> {

    @PrimaryKey
    private String name;
    private RealmList<Ingredient> ingredients = new RealmList<>();
    private boolean expanded;
    private boolean favorite;

    public Recipe() {
        // realm constructor
    }

    public Recipe(String name, RealmList<Ingredient> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    @Override
    public RealmList<Ingredient> getChildList() {
        return ingredients;
    }

    public int getIngredientsCount() {
        return ingredients.size();
    }

    public int getFavoritesCount() {
        int count = 0;
        for (int i = 0, size = ingredients.size(); i < size; i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isFavorite()) {
                count++;
            }
        }
        return count;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    public Ingredient getIngredient(int position) {
        return ingredients.get(position);
    }

    public boolean isVegetarian() {
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isVegetarian()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        for (int i = 0, size = ingredients.size(); i < size; i++) {
            Ingredient ingredient = ingredients.get(i);
            ingredient.setFavorite(favorite);
        }
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "name = [" + name + "], favorite = [" + favorite + "]";
    }
}
