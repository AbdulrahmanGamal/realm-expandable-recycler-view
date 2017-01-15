package com.github.reline.expandablerecyclerviewsample.model;

import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.model.Child;

@RealmClass
public class Ingredient implements RealmModel, Child {

    @PrimaryKey
    private String mName;
    private boolean mIsVegetarian;

    public Ingredient() {
        // io.realm constructor
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

    @Override
    public RealmQuery<? extends Child> queryByPrimaryKey(RealmQuery<? extends Child> query) {
        return query.or().equalTo("mName", mName);
    }
}
