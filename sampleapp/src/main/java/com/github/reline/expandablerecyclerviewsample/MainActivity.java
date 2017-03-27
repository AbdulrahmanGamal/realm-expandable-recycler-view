package com.github.reline.expandablerecyclerviewsample;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.github.reline.expandablerecyclerviewsample.model.Ingredient;
import com.github.reline.expandablerecyclerviewsample.model.Recipe;
import com.github.reline.expandablerecyclerviewsample.recyclerview.adapter.RecipeAdapter;

import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmExpandableRecyclerAdapter;
import io.realm.RealmList;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view_sample);

        realm = Realm.getDefaultInstance();

        Ingredient beef = new Ingredient("beef", false);
        Ingredient cheese = new Ingredient("cheese", true);
        Ingredient salsa = new Ingredient("salsa", true);
        Ingredient tortilla = new Ingredient("tortilla", true);
        Ingredient ketchup = new Ingredient("ketchup", true);
        Ingredient bun = new Ingredient("bun", true);

        RealmList<Ingredient> tacoIngredients = new RealmList<>();
        tacoIngredients.addAll(Arrays.asList(beef, cheese, salsa, tortilla));
        Recipe taco = new Recipe("taco", tacoIngredients);

        RealmList<Ingredient> quesadillaIngredients = new RealmList<>();
        quesadillaIngredients.addAll(Arrays.asList(cheese, tortilla));
        Recipe quesadilla = new Recipe("quesadilla", quesadillaIngredients);

        RealmList<Ingredient> burgerIngredients = new RealmList<>();
        burgerIngredients.addAll(Arrays.asList(beef, cheese, ketchup, bun));
        Recipe burger = new Recipe("burger", burgerIngredients);

        final List<Recipe> recipes = Arrays.asList(taco, quesadilla, burger);
        realm.beginTransaction();
        for (Recipe recipe : recipes) {
            Recipe realmRecipe = realm.where(Recipe.class).equalTo("name", recipe.getName()).findFirst();
            if (realmRecipe != null) {
                recipe.setExpanded(realmRecipe.isExpanded());
            }
        }
        realm.insertOrUpdate(recipes);
        realm.commitTransaction();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        adapter = new RecipeAdapter(realm.where(Recipe.class).findAll(), "name");
        adapter.setExpandCollapseListener(new RealmExpandableRecyclerAdapter.ExpandCollapseListener() {
            @UiThread
            @Override
            public void onParentExpanded(int parentPosition) {
                Recipe expandedRecipe = adapter.getItem(parentPosition);

                String toastMsg = getResources().getString(R.string.expanded, expandedRecipe.getName());
                Toast.makeText(MainActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @UiThread
            @Override
            public void onParentCollapsed(int parentPosition) {
                Recipe collapsedRecipe = adapter.getItem(parentPosition);

                String toastMsg = getResources().getString(R.string.collapsed, collapsedRecipe.getName());
                Toast.makeText(MainActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SearchView searchView = (SearchView) findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
