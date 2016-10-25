package com.bignerdranch.expandablerecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import io.realm.Case;
import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.Sort;

public abstract class RealmExpandableSearchRecyclerAdapter<P extends Parent<C>, C extends RealmObject, PVH extends ParentViewHolder, CVH extends ChildViewHolder>
        extends RealmExpandableRecyclerAdapter<P, C, PVH, CVH> {

    private OrderedRealmCollection<P> originalData;
    private String filterKey;
    private boolean useContains;
    private Case casing;
    private Sort sortOrder;
    private String sortKey;
    private String basePredicate;

    public RealmExpandableSearchRecyclerAdapter(
            @NonNull Context context,
            @Nullable OrderedRealmCollection<P> data,
            @NonNull String filterKey) {
        this(context, data, filterKey, true, Case.INSENSITIVE, Sort.ASCENDING, filterKey, null);
    }

    public RealmExpandableSearchRecyclerAdapter(
            @NonNull Context context,
            @Nullable OrderedRealmCollection<P> data,
            @NonNull String filterKey,
            boolean useContains,
            Case casing,
            Sort sortOrder,
            String sortKey,
            String basePredicate) {
        super(context, data);
        this.originalData = data;
        this.filterKey = filterKey;
        this.useContains = useContains;
        this.casing = casing;
        this.sortOrder = sortOrder;
        this.sortKey = sortKey;
        this.basePredicate = basePredicate;
    }

    public void filter(String input) {
        RealmQuery<P> where = originalData.where();
        OrderedRealmCollection<P> filteredData;
        if (input.isEmpty() && basePredicate != null) {
            if (useContains) {
                where = where.contains(filterKey, basePredicate, casing);
            } else {
                where = where.beginsWith(filterKey, basePredicate, casing);
            }
        } else if (!input.isEmpty()) {
            if (useContains) {
                where = where.contains(filterKey, input, casing);
            } else {
                where = where.beginsWith(filterKey, input, casing);
            }
        }

        if (sortKey == null) {
            filteredData = where.findAll();
        } else {
            filteredData = where.findAllSorted(sortKey, sortOrder);
        }
        setParentList(filteredData, true);
    }

    /**
     * The columnKey by which the results are filtered.
     */
    public void setFilterKey(String filterKey) {
        if (filterKey == null) {
            throw new IllegalStateException("The filterKey cannot be null.");
        }
        this.filterKey = filterKey;
    }

    /**
     * If true, {@link RealmQuery#contains} is used else {@link RealmQuery#beginsWith}.
     */
    public void setUseContains(boolean useContains) {
        this.useContains = useContains;
    }

    /**
     * Sets if the filtering is case sensitive or case insensitive.
     */
    public void setCasing(Case casing) {
        this.casing = casing;
    }

    /**
     * Sets if the sort order is ascending or descending.
     */
    public void setSortOrder(Sort sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Sets the sort columnKey.
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * Sets the basePredicate which is used filters the results when the search query is empty.
     */
    public void setBasePredicate(String basePredicate) {
        this.basePredicate = basePredicate;
    }
}
