package com.github.reline.expandablerecyclerviewsample.recyclerview.viewholders;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.NonNull;
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
    private final ImageView mArrowExpandImageView;
    private TextView mRecipeTextView;

    private Recipe recipe;

    public RecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        mRecipeTextView = (TextView) itemView.findViewById(R.id.recipe_textview);

        mArrowExpandImageView = (ImageView) itemView.findViewById(R.id.arrow_expand_imageview);
    }

    public void bind(@NonNull Recipe recipe) {
        this.recipe = recipe;
        mRecipeTextView.setText(recipe.getName());
    }

    @SuppressLint("NewApi")
    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (expanded) {
                mArrowExpandImageView.setRotation(ROTATED_POSITION);
            } else {
                mArrowExpandImageView.setRotation(INITIAL_POSITION);
            }
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
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
            mArrowExpandImageView.startAnimation(rotateAnimation);
        }
    }

    @Override
    protected void expandView() {
        super.expandView();
        saveExpandedState(true);
    }

    @Override
    protected void collapseView() {
        super.collapseView();
        saveExpandedState(false);
    }

    private void saveExpandedState(boolean expanded) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        recipe.setExpanded(expanded);
        realm.commitTransaction();
        realm.close();
    }
}
