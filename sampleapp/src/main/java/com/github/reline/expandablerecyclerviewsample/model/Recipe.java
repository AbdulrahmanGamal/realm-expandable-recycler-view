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
    private RealmList<Ingredient> ingredients;
    private boolean expanded;

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
}
