package com.bignerdranch.expandablerecyclerviewsample.recyclerview.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerviewsample.R;
import com.bignerdranch.expandablerecyclerviewsample.model.Ingredient;

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
