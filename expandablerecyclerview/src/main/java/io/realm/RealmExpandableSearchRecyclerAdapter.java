package io.realm;

import android.support.annotation.NonNull;

import io.realm.model.Parent;
import io.realm.model.Child;

public abstract class RealmExpandableSearchRecyclerAdapter<P extends Parent<C>, C extends Child, PVH extends ParentViewHolder, CVH extends ChildViewHolder>
        extends RealmExpandableRecyclerAdapter<P, C, PVH, CVH> {

    @NonNull
    private OrderedRealmCollection<P> originalData;
    private String filterKey;
    private boolean useContains;
    private Case casing;
    private Sort sortOrder;
    private String sortKey;
    private String basePredicate;

    public RealmExpandableSearchRecyclerAdapter(
            @NonNull OrderedRealmCollection<P> data,
            @NonNull String filterKey) {
        this(data, filterKey, true, Case.INSENSITIVE, Sort.ASCENDING, filterKey, null);
    }

    public RealmExpandableSearchRecyclerAdapter(
            @NonNull OrderedRealmCollection<P> data,
            @NonNull String filterKey,
            boolean useContains,
            Case casing,
            Sort sortOrder,
            String sortKey,
            String basePredicate) {
        super(data);
        this.originalData = data;
        this.filterKey = filterKey;
        this.useContains = useContains;
        this.casing = casing;
        this.sortOrder = sortOrder;
        this.sortKey = sortKey;
        this.basePredicate = basePredicate;
    }

    public void filter(String input) {
        if (!isDataValid()) return;
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
        updateData(filteredData);
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

    private boolean isDataValid() {
        return originalData.isValid();
    }
}
