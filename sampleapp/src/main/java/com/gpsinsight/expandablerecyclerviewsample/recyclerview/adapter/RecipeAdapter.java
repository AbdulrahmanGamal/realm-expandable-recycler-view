package com.gpsinsight.expandablerecyclerviewsample.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gpsinsight.expandablerecyclerviewsample.R;
import com.gpsinsight.expandablerecyclerviewsample.model.Ingredient;
import com.gpsinsight.expandablerecyclerviewsample.model.Recipe;
import com.gpsinsight.expandablerecyclerviewsample.recyclerview.viewholders.IngredientViewHolder;
import com.gpsinsight.expandablerecyclerviewsample.recyclerview.viewholders.RecipeViewHolder;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmExpandableSearchRecyclerAdapter;

public class RecipeAdapter extends RealmExpandableSearchRecyclerAdapter<Recipe, Ingredient, RecipeViewHolder, IngredientViewHolder> {

    private static final int PARENT_VEGETARIAN = 0;
    private static final int PARENT_NORMAL = 1;
    private static final int CHILD_VEGETARIAN = 2;
    private static final int CHILD_NORMAL = 3;

    private List<Recipe> recipeList;

    public RecipeAdapter(@NonNull OrderedRealmCollection<Recipe> recipeList, @NonNull String filterKey) {
        super(recipeList, filterKey);
        this.recipeList = recipeList;
    }

    @UiThread
    @NonNull
    @Override
    public RecipeViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View recipeView;
        LayoutInflater mInflater = LayoutInflater.from(parentViewGroup.getContext());
        switch (viewType) {
            default:
            case PARENT_NORMAL:
                recipeView = mInflater.inflate(R.layout.recipe_view, parentViewGroup, false);
                break;
            case PARENT_VEGETARIAN:
                recipeView = mInflater.inflate(R.layout.vegetarian_recipe_view, parentViewGroup, false);
                break;
        }
        return new RecipeViewHolder(recipeView);
    }

    @UiThread
    @NonNull
    @Override
    public IngredientViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View ingredientView;
        LayoutInflater mInflater = LayoutInflater.from(childViewGroup.getContext());
        switch (viewType) {
            default:
            case CHILD_NORMAL:
                ingredientView = mInflater.inflate(R.layout.ingredient_view, childViewGroup, false);
                break;
            case CHILD_VEGETARIAN:
                ingredientView = mInflater.inflate(R.layout.vegetarian_ingredient_view, childViewGroup, false);
                break;
        }
        return new IngredientViewHolder(ingredientView);
    }

    @UiThread
    @Override
    public void onBindParentViewHolder(@NonNull RecipeViewHolder recipeViewHolder, int parentPosition, @NonNull Recipe recipe) {
        recipeViewHolder.bind(recipe);
    }

    @UiThread
    @Override
    public void onBindChildViewHolder(@NonNull IngredientViewHolder ingredientViewHolder, int parentPosition, int childPosition, @NonNull Ingredient ingredient) {
        ingredientViewHolder.bind(ingredient);
    }

    @Override
    public int getParentViewType(int parentPosition) {
        if (recipeList.get(parentPosition).isVegetarian()) {
            return PARENT_VEGETARIAN;
        } else {
            return PARENT_NORMAL;
        }
    }

    @Override
    public int getChildViewType(int parentPosition, int childPosition) {
        Ingredient ingredient = recipeList.get(parentPosition).getIngredient(childPosition);
        if (ingredient.isVegetarian()) {
            return CHILD_VEGETARIAN;
        } else {
            return CHILD_NORMAL;
        }
    }

    @Override
    public boolean isParentViewType(int viewType) {
        return viewType == PARENT_VEGETARIAN || viewType == PARENT_NORMAL;
    }

}
