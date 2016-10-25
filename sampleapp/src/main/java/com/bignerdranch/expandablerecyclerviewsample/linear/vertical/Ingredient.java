package com.bignerdranch.expandablerecyclerviewsample.linear.vertical;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Ingredient extends RealmObject {

    @PrimaryKey
    private String mName;
    private boolean mIsVegetarian;

    public Ingredient() {
        // realm constructor
    }

    public Ingredient(String name, boolean isVegetarian) {
        mName = name;
        mIsVegetarian = isVegetarian;
    }

    public String getName() {
        return mName;
    }

    public boolean isVegetarian() {
        return mIsVegetarian;
    }
}
