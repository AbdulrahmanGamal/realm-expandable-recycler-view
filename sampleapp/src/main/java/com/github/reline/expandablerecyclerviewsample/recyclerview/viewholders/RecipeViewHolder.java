package com.github.reline.expandablerecyclerviewsample.recyclerview.viewholders;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.reline.expandablerecyclerviewsample.R;
import com.github.reline.expandablerecyclerviewsample.model.Recipe;

import io.realm.ParentViewHolder;
import io.realm.Realm;

public class RecipeViewHolder extends ParentViewHolder {

    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    @NonNull
    private final ImageView arrowExpandImageView;
    private TextView recipeTextView;
    private TextView starredIngredientCount;
    private TextView ingredientCount;

    private Recipe recipe;

    public RecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        recipeTextView = (TextView) itemView.findViewById(R.id.recipe_textview);
        arrowExpandImageView = (ImageView) itemView.findViewById(R.id.arrow_expand_imageview);
        starredIngredientCount = (TextView) itemView.findViewById(R.id.starred_ingredient_count);
        ingredientCount = (TextView) itemView.findViewById(R.id.ingredient_count);
    }

    public void bind(@NonNull Recipe r) {
        this.recipe = r;
        recipeTextView.setText(r.getName());
        starredIngredientCount.setText(String.valueOf(recipe.getFavoritesCount()));
        ingredientCount.setText(String.valueOf(recipe.getIngredientsCount()));

        if (r.isFavorite()) {
            itemView.setBackgroundColor(Color.YELLOW);
        } else {
            itemView.setBackgroundColor(Color.WHITE);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Realm", "onClick: recipe");
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                recipe.setFavorite(!recipe.isFavorite());
                realm.commitTransaction();
                realm.close();
            }
        });
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (expanded) {
                arrowExpandImageView.setRotation(ROTATED_POSITION);
            } else {
                arrowExpandImageView.setRotation(INITIAL_POSITION);
            }
        }
    }

    @Override
    public void onExpansionToggled(final boolean expanded) {
        super.onExpansionToggled(expanded);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            RotateAnimation rotateAnimation;
            if (expanded) { // rotate clockwise
                 rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            } else { // rotate counterclockwise
                rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            }

            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            arrowExpandImageView.startAnimation(rotateAnimation);
        }

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                recipe.setExpanded(!expanded);
            }
        });
        realm.close();
    }

    @Override
    public void setMainItemClickToExpand() {
        arrowExpandImageView.setOnClickListener(this);
    }

}
