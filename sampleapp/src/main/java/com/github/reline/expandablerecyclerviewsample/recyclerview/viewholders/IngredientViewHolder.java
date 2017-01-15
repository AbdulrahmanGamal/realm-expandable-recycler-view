package com.github.reline.expandablerecyclerviewsample.recyclerview.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerviewsample.R;
import com.github.reline.expandablerecyclerviewsample.model.Ingredient;

import io.realm.ChildViewHolder;

public class IngredientViewHolder extends ChildViewHolder {

    private TextView mIngredientTextView;

    public IngredientViewHolder(@NonNull View itemView) {
        super(itemView);
        mIngredientTextView = (TextView) itemView.findViewById(R.id.ingredient_textview);
    }

    public void bind(@NonNull Ingredient ingredient) {
        mIngredientTextView.setText(ingredient.getName());
    }
}
