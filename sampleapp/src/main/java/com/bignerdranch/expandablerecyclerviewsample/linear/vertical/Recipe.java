package com.bignerdranch.expandablerecyclerviewsample.linear.vertical;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Recipe extends RealmObject implements Parent<Ingredient> {

    @PrimaryKey
    private String mName;
    private RealmList<Ingredient> mIngredients;
    private boolean expanded;

    public Recipe() {
        // realm constructor
    }

    public Recipe(String name, RealmList<Ingredient> ingredients) {
        mName = name;
        mIngredients = ingredients;
    }

    public String getName() {
        return mName;
    }

    @Override
    public List<Ingredient> getChildList() {
        return mIngredients;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return expanded;
    }

    public Ingredient getIngredient(int position) {
        return mIngredients.get(position);
    }

    public boolean isVegetarian() {
        for (Ingredient ingredient : mIngredients) {
            if (!ingredient.isVegetarian()) {
                return false;
            }
        }
        return true;
    }
}
