package com.gpsinsight.expandablerecyclerviewsample.recyclerview.viewholders;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.reline.expandablerecyclerviewsample.R;
import com.gpsinsight.expandablerecyclerviewsample.model.Ingredient;

import io.realm.ChildViewHolder;
import io.realm.Realm;

public class IngredientViewHolder extends ChildViewHolder implements View.OnClickListener {

    private TextView ingredientTextView;
    private Ingredient ingredient;

    public IngredientViewHolder(@NonNull View itemView) {
        super(itemView);
        ingredientTextView = (TextView) itemView.findViewById(R.id.ingredient_textview);
        itemView.setOnClickListener(this);
    }

    public void bind(@NonNull Ingredient ingredient) {
        this.ingredient = ingredient;
        ingredientTextView.setText(ingredient.getName());
        if (ingredient.isFavorite()) {
            itemView.setBackgroundColor(Color.YELLOW);
        } else {
            itemView.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void onClick(View v) {
        Log.i("Realm", "onClick: ingredient");
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                ingredient.setFavorite(!ingredient.isFavorite());
            }
        });
        realm.close();
    }
}
