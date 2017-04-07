package io.realm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.realm.model.Child;
import io.realm.model.ExpandableWrapper;
import io.realm.model.Parent;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * RecyclerView.Adapter implementation that
 * adds the ability to expand and collapse list items.
 */
public abstract class RealmExpandableRecyclerAdapter<P extends Parent<C>, C extends Child, PVH extends ParentViewHolder, CVH extends ChildViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Default ViewType for parent rows
     */
    public static final int TYPE_PARENT = 0;
    /**
     * Default ViewType for children rows
     */
    public static final int TYPE_CHILD = 1;

    private final OrderedRealmCollectionChangeListener parentCollectionListener;
    private final List<Triplet<RealmList<C>, OrderedRealmCollectionChangeListener<RealmList<C>>, List<RealmObjectChangeListener<C>>>> childCollectionListeners;

    /**
     * A {@link List} of all currently expanded parents and their children, in order.
     * Changes to this list should be made through the add/remove methods
     * available in {@link RealmExpandableRecyclerAdapter}.
     */
    @NonNull
    protected List<ExpandableWrapper<P, C>> flatItemList;

    @NonNull
    private OrderedRealmCollection<P> parentList;

    @Nullable
    private ExpandCollapseListener expandCollapseListener;

    /**
     * Allows objects to register themselves as expand/collapse listeners to be
     * notified of change events.
     * <p>
     * Implement this in your {@link android.app.Activity} or {@link android.app.Fragment}
     * to receive these callbacks.
     */
    public interface ExpandCollapseListener {
        /**
         * Called when a parent is expanded.
         *
         * @param parentPosition The position of the parent in the list being expanded
         */
        @UiThread
        void onParentExpanded(int parentPosition);

        /**
         * Called when a parent is collapsed.
         *
         * @param parentPosition The position of the parent in the list being collapsed
         */
        @UiThread
        void onParentCollapsed(int parentPosition);
    }

    /**
     * Primary constructor. Sets up parentList {@link #flatItemList} and {@link #flatItemList}.
     *
     * @param parentList List of all parents to be displayed in the RecyclerView that this
     *                       adapter is linked to
     */
    public RealmExpandableRecyclerAdapter(@NonNull OrderedRealmCollection<P> parentList) {
        if (!parentList.isManaged()) {
            throw new IllegalStateException("Only use this adapter with managed RealmCollection, " +
            "for un-managed lists you can just use the RecyclerView.Adapter");
        }
        this.parentList = parentList;
        childCollectionListeners = new ArrayList<>(parentList.size());
        flatItemList = generateFlattenedParentChildList(parentList);
        parentCollectionListener = createParentCollectionListener();
    }

    /**
     * Returns the item associated with the specified position.
     * Can return {@code null} if provided Realm instance by {@link OrderedRealmCollection} is closed.
     *
     * @param index index of the item.
     * @return the item at the specified position, {@code null} if adapter data is not valid.
     */
    @Nullable
    public P getItem(int index) {
        return parentList.isValid() ? parentList.get(index) : null;
    }

    private void addParentCollectionListener(@NonNull OrderedRealmCollection<P> data) {
        if (data instanceof RealmResults) {
            RealmResults<P> results = (RealmResults<P>) data;
            //noinspection unchecked
            results.addChangeListener(parentCollectionListener);
        } else if (data instanceof RealmList) {
            RealmList<P> list = (RealmList<P>) data;
            //noinspection unchecked
            list.addChangeListener(parentCollectionListener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeParentCollectionListener(@NonNull OrderedRealmCollection<P> data) {
        if (data instanceof RealmResults) {
            RealmResults<P> realmResults = (RealmResults<P>) data;
            //noinspection unchecked
            realmResults.removeChangeListener(parentCollectionListener);
        } else if (data instanceof RealmList) {
            RealmList<P> list = (RealmList<P>) data;
            //noinspection unchecked
            list.removeChangeListener(parentCollectionListener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void addChildCollectionListeners() {
        for (int i = 0, size = parentList.size(); i < size; i++) {
            addChildCollectionListener(i);
        }
    }

    private void addChildCollectionListener(int parentPosition) {
        RealmList<C> data = parentList.get(parentPosition).getChildList();
        OrderedRealmCollectionChangeListener<RealmList<C>> childCollectionListener = createChildCollectionListener(parentPosition);
        List<RealmObjectChangeListener<C>> childListeners = new ArrayList<>();
        childCollectionListeners.add(parentPosition, new Triplet<>(data, childCollectionListener, childListeners));
        data.addChangeListener(childCollectionListener);
        for (int i = 0, size = data.size(); i < size; i++) {
            C child = data.get(i);
            RealmObjectChangeListener<C> childListener = createChildListener(parentPosition, i);
            RealmObject.addChangeListener(child, childListener);
            childListeners.add(childListener);
        }
    }

    private void removeChildCollectionListeners() {
        for (int i = childCollectionListeners.size() - 1; i >= 0; i--) {
            removeChildCollectionListener(i);
        }
    }

    private void removeChildCollectionListener(int parentPosition) {
        childCollectionListeners.remove(parentPosition);
    }

    private OrderedRealmCollectionChangeListener<OrderedRealmCollection<P>> createParentCollectionListener() {
        return new OrderedRealmCollectionChangeListener<OrderedRealmCollection<P>>() {
            @Override
            public void onChange(OrderedRealmCollection<P> collection, OrderedCollectionChangeSet changeSet) {
                // null Changes means the async query returns the first time.
                if (changeSet == null) {
                    notifyDataSetChanged();
                    return;
                }

                // For deletions, the adapter has to be notified in reverse order.
                OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                for (int i = deletions.length - 1; i >= 0; i--) {
                    OrderedCollectionChangeSet.Range range = deletions[i];
                    for (int j = range.length - 1; j >= 0; j--) {
                        int parentPosition = range.startIndex + j;
                        int flatParentPosition = getFlatParentPosition(parentPosition);
                        notifyItemRemoved(flatParentPosition);
                        removeChildCollectionListener(parentPosition);
                    }
                }

                // Insertions need to be notified in reverse order because they are incremental
                OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                for (OrderedCollectionChangeSet.Range range : insertions) {
                    for (int i = range.length - 1; i >= 0; i--) {
                        int parentPosition = range.startIndex + i;
                        int flatParentPosition = getFlatParentPosition(parentPosition);
                        notifyItemInserted(flatParentPosition);
                        if (parentList.get(parentPosition).isExpanded()) {
                            addChildCollectionListener(parentPosition);
                        }
                    }
                }

                OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                for (OrderedCollectionChangeSet.Range range : modifications) {
                    for (int i = 0; i < range.length; i++) {
                        int parentPosition = range.startIndex + i;
                        int flatParentPosition = getFlatParentPosition(parentPosition);
                        notifyItemChanged(flatParentPosition);
                    }
                }
            }
        };
    }

    private OrderedRealmCollectionChangeListener<RealmList<C>> createChildCollectionListener(final int parentIndex) {
        return new OrderedRealmCollectionChangeListener<RealmList<C>>() {
            @Override
            public void onChange(RealmList<C> collection, OrderedCollectionChangeSet changeSet) {
                // null Changes means the async query returns the first time.
                if (changeSet == null) {
                    notifyDataSetChanged();
                    return;
                }

                int flatStartPosition = getFlatParentPosition(parentIndex) + 1;
                OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                for (OrderedCollectionChangeSet.Range range : insertions) {
                    notifyItemRangeInserted(flatStartPosition + range.startIndex, range.length);
                }
            }
        };
    }

    private RealmObjectChangeListener<C> createChildListener(final int parentIndex, final int childIndex) {
        return new RealmObjectChangeListener<C>() {
            @Override
            public void onChange(C object, ObjectChangeSet changeSet) {
                int flatPosition = getFlatParentPosition(parentIndex) + childIndex + 1;
                if (changeSet.isDeleted()) {
                    notifyItemRemoved(flatPosition);
                    return;
                }
                notifyItemChanged(flatPosition);
            }
        };
    }

    /**
     * Implementation of Adapter.onCreateViewHolder(ViewGroup, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either {@link #onCreateParentViewHolder(ViewGroup, int)}
     * or {@link #onCreateChildViewHolder(ViewGroup, int)}.
     *
     * @param viewGroup The {@link ViewGroup} into which the new {@link android.view.View}
     *                  will be added after it is bound to an adapter position.
     * @param viewType  The view type of the new {@code android.view.View}.
     * @return A new RecyclerView.ViewHolder
     * that holds a {@code android.view.View} of the given view type.
     */
    @NonNull
    @Override
    @UiThread
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (isParentViewType(viewType)) {
            PVH pvh = onCreateParentViewHolder(viewGroup, viewType);
            pvh.setParentViewHolderExpandCollapseListener(mParentViewHolderExpandCollapseListener);
            pvh.expandableAdapter = this;
            return pvh;
        } else {
            CVH cvh = onCreateChildViewHolder(viewGroup, viewType);
            cvh.expandableAdapter = this;
            return cvh;
        }
    }

    /**
     * Implementation of Adapter.onBindViewHolder(RecyclerView.ViewHolder, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either
     * {@link #onBindParentViewHolder(ParentViewHolder, int, Parent)} or
     * {@link #onBindChildViewHolder(ChildViewHolder, int, int, Child)}.
     *
     * @param holder The RecyclerView.ViewHolder to bind data to
     * @param flatPosition The index in the merged list of children and parents at which to bind
     */
    @Override
    @SuppressWarnings("unchecked")
    @UiThread
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int flatPosition) {
        if (flatPosition > flatItemList.size()) {
            throw new IllegalStateException("Trying to bind item out of bounds, size " + flatItemList.size()
                    + " flatPosition " + flatPosition + ". Was the data changed without a call to notify...()?");
        }

        ExpandableWrapper<P, C> listItem = flatItemList.get(flatPosition);
        if (listItem.isParent()) {
            PVH parentViewHolder = (PVH) holder;

            if (parentViewHolder.shouldItemViewClickToggleExpansion()) {
                parentViewHolder.setMainItemClickToExpand();
            }

            parentViewHolder.setExpanded(listItem.isExpanded());
            parentViewHolder.parent = listItem.getParent();
            if (RealmObject.isValid(parentViewHolder.parent)) {
                onBindParentViewHolder(parentViewHolder, getNearestParentPosition(flatPosition), listItem.getParent());
            }
        } else {
            CVH childViewHolder = (CVH) holder;
            childViewHolder.child = listItem.getChild();
            if (RealmObject.isValid(childViewHolder.child)) {
                onBindChildViewHolder(childViewHolder, getNearestParentPosition(flatPosition), getChildPosition(flatPosition), listItem.getChild());
            }
        }
    }

    /**
     * Callback called from {@link #onCreateViewHolder(ViewGroup, int)} when
     * the list item created is a parent.
     *
     * @param parentViewGroup The {@link ViewGroup} in the list for which a {@link PVH} is being
     *                        created
     * @return A {@code PVH} corresponding to the parent with the {@code ViewGroup} parentViewGroup
     */
    @NonNull
    @UiThread
    public abstract PVH onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType);

    /**
     * Callback called from {@link #onCreateViewHolder(ViewGroup, int)} when
     * the list item created is a child.
     *
     * @param childViewGroup The {@link ViewGroup} in the list for which a {@link CVH}
     *                       is being created
     * @return A {@code CVH} corresponding to the child with the {@code ViewGroup} childViewGroup
     */
    @NonNull
    @UiThread
    public abstract CVH onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType);

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a parent.
     * <p>
     * Bind data to the {@link PVH} here.
     *
     * @param parentViewHolder The {@code PVH} to bind data to
     * @param parentPosition The position of the parent to bind
     * @param parent The parent which holds the data to be bound to the {@code PVH}
     */
    @UiThread
    public abstract void onBindParentViewHolder(@NonNull PVH parentViewHolder, int parentPosition, @NonNull P parent);

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a child.
     * <p>
     * Bind data to the {@link CVH} here.
     *
     * @param childViewHolder The {@code CVH} to bind data to
     * @param parentPosition The position of the parent that contains the child to bind
     * @param childPosition The position of the child to bind
     * @param child The child which holds that data to be bound to the {@code CVH}
     */
    @UiThread
    public abstract void onBindChildViewHolder(@NonNull CVH childViewHolder, int parentPosition, int childPosition, @NonNull C child);

    /**
     * Gets the number of parents and children currently expanded.
     *
     * @return The size of {@link #flatItemList}
     */
    @Override
    @UiThread
    public int getItemCount() {
        return flatItemList.size();
    }

    /**
     * For multiple view type support look at overriding {@link #getParentViewType(int)} and
     * {@link #getChildViewType(int, int)}. Almost all cases should override those instead
     * of this method.
     *
     * @param flatPosition The index in the merged list of children and parents to get the view type of
     * @return Gets the view type of the item at the given flatPosition.
     */
    @Override
    @UiThread
    public int getItemViewType(int flatPosition) {
        ExpandableWrapper<P, C> listItem = flatItemList.get(flatPosition);
        if (listItem.isParent()) {
            return getParentViewType(getNearestParentPosition(flatPosition));
        } else {
            return getChildViewType(getNearestParentPosition(flatPosition), getChildPosition(flatPosition));
        }
    }

    /**
     * Return the view type of the parent at {@code parentPosition} for the purposes of view recycling.
     * <p>
     * The default implementation of this method returns {@link #TYPE_PARENT}, making the assumption of
     * a single view type for the parents in this adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     * <p>
     * If you are overriding this method make sure to override {@link #isParentViewType(int)} as well.
     * <p>
     *
     * @param parentPosition The index of the parent to query
     * @return integer value identifying the type of the view needed to represent the parent at
     *                 {@code parentPosition}. Type codes need not be contiguous.
     */
    public int getParentViewType(int parentPosition) {
        return TYPE_PARENT;
    }


    /**
     * Return the view type of the child {@code parentPosition} contained within the parent
     * at {@code parentPosition} for the purposes of view recycling.
     * <p>
     * The default implementation of this method returns {@link #TYPE_CHILD}, making the assumption of
     * a single view type for the children in this adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     * <p>
     *
     * @param parentPosition The index of the parent continaing the child to query
     * @param childPosition The index of the child within the parent to query
     * @return integer value identifying the type of the view needed to represent the child at
     *                 {@code parentPosition}. Type codes need not be contiguous.
     */
    public int getChildViewType(int parentPosition, int childPosition) {
        return TYPE_CHILD;
    }

    /**
     * Used to determine whether a viewType is that of a parent or not, for ViewHolder creation purposes.
     * <p>
     * Only override if {@link #getParentViewType(int)} is being overriden
     *
     * @param viewType the viewType identifier in question
     * @return whether the given viewType belongs to a parent view
     */
    public boolean isParentViewType(int viewType) {
        return viewType == TYPE_PARENT;
    }

    /**
     * Gets the list of parents that is backing this adapter.
     *
     * @return The list of parents that this adapter represents
     */
    @NonNull
    @UiThread
    public OrderedRealmCollection<P> getData() {
        return parentList;
    }

    /**
     * Set a new list of parents and notify any registered observers that the data set has changed.
     * <p>
     * This setter does not specify what about the data set has changed, forcing
     * any observers to assume that all existing items and structure may no longer be valid.
     * LayoutManagers will be forced to fully rebind and relayout all visible views.</p>
     */
    @UiThread
    public void updateData(@NonNull OrderedRealmCollection<P> data) {
        if (isDataValid()) {
            removeChildCollectionListeners();
            removeParentCollectionListener(parentList);
            addParentCollectionListener(data);
            addChildCollectionListeners();
        }
        parentList = data;
        notifyParentDataSetChanged();
    }

    private boolean isDataValid() {
        return parentList.isValid();
    }

    /**
     * Implementation of Adapter#onAttachedToRecyclerView(RecyclerView).
     * <p>
     * Called when this {@link RealmExpandableRecyclerAdapter} is attached to a RecyclerView.
     *
     * @param recyclerView The {@code RecyclerView} this {@code RealmExpandableRecyclerAdapter}
     *                     is being attached to
     */
    @Override
    @UiThread
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        if (isDataValid()) {
            addParentCollectionListener(parentList);
            addChildCollectionListeners();
        }
    }


    /**
     * Implementation of Adapter.onDetachedFromRecyclerView(RecyclerView)
     * <p>
     * Called when this RealmExpandableRecyclerAdapter is detached from a RecyclerView.
     *
     * @param recyclerView The {@code RecyclerView} this {@code RealmExpandableRecyclerAdapter}
     *                     is being detached from
     */
    @Override
    @UiThread
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (isDataValid()) {
            removeChildCollectionListeners();
            removeParentCollectionListener(parentList);
        }
    }

    @UiThread
    public void setExpandCollapseListener(@Nullable ExpandCollapseListener expandCollapseListener) {
        this.expandCollapseListener = expandCollapseListener;
    }

    /**
     * Called when a ParentViewHolder has triggered an expansion for it's parent
     *
     * @param flatParentPosition the position of the parent that is calling to be expanded
     */
    @UiThread
    protected void parentExpandedFromViewHolder(int flatParentPosition) {
        ExpandableWrapper<P, C> parentWrapper = flatItemList.get(flatParentPosition);
        updateExpandedParent(parentWrapper, flatParentPosition, true);
    }

    /**
     * Called when a ParentViewHolder has triggered a collapse for it's parent
     *
     * @param flatParentPosition the position of the parent that is calling to be collapsed
     */
    @UiThread
    protected void parentCollapsedFromViewHolder(int flatParentPosition) {
        ExpandableWrapper<P, C> parentWrapper = flatItemList.get(flatParentPosition);
        updateCollapsedParent(parentWrapper, flatParentPosition, true);
    }

    private ParentViewHolder.ParentViewHolderExpandCollapseListener mParentViewHolderExpandCollapseListener = new ParentViewHolder.ParentViewHolderExpandCollapseListener() {

        /**
         * Implementation of {@link ParentViewHolder.ParentViewHolderExpandCollapseListener#onParentExpanded(int)}.
         * <p>
         * Called when a {@link P} is triggered to expand.
         *
         * @param flatParentPosition The index of the item in the list being expanded, relative to the flattened list
         */
        @Override
        @UiThread
        public void onParentExpanded(int flatParentPosition) {
            parentExpandedFromViewHolder(flatParentPosition);
        }

        /**
         * Implementation of {@link ParentViewHolder.ParentViewHolderExpandCollapseListener#onParentCollapsed(int)}.
         * <p>
         * Called when a {@link P} is triggered to collapse.
         *
         * @param flatParentPosition The index of the item in the list being collapsed, relative to the flattened list
         */
        @Override
        @UiThread
        public void onParentCollapsed(int flatParentPosition) {
            parentCollapsedFromViewHolder(flatParentPosition);
        }
    };

    /**
     * Expands a specified parent. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * flat list of items.
     *
     * @param parentWrapper The ExpandableWrapper of the parent to expand
     * @param flatParentPosition The index of the parent to expand
     * @param expansionTriggeredByListItemClick true if expansion was triggered
     *                                          by a click event, false otherwise.
     */
    @UiThread
    private void updateExpandedParent(@NonNull ExpandableWrapper<P, C> parentWrapper, int flatParentPosition, boolean expansionTriggeredByListItemClick) {
        if (parentWrapper.isExpanded()) {
            return;
        }

        parentWrapper.setExpanded(true);

        List<ExpandableWrapper<P, C>> wrappedChildList = parentWrapper.getWrappedChildList();
        if (wrappedChildList != null) {
            int childCount = wrappedChildList.size();
            for (int i = 0; i < childCount; i++) {
                flatItemList.add(flatParentPosition + i + 1, wrappedChildList.get(i));
            }

            notifyItemRangeInserted(flatParentPosition + 1, childCount);
        }

        // add realm change listener to children
        P parent = parentWrapper.getParent();
        int parentPosition = parentList.indexOf(parent);
        addChildCollectionListener(parentPosition);

        if (expansionTriggeredByListItemClick && expandCollapseListener != null) {
            expandCollapseListener.onParentExpanded(getNearestParentPosition(flatParentPosition));
        }
    }

    /**
     * Collapses a specified parent item. Calls through to the
     * ExpandCollapseListener and removes children of the specified parent from the
     * flat list of items.
     *
     * @param parentWrapper The ExpandableWrapper of the parent to collapse
     * @param flatParentPosition The index of the parent to collapse
     * @param collapseTriggeredByListItemClick true if expansion was triggered
     *                                         by a click event, false otherwise.
     */
    @UiThread
    private void updateCollapsedParent(@NonNull ExpandableWrapper<P, C> parentWrapper, int flatParentPosition, boolean collapseTriggeredByListItemClick) {
        if (!parentWrapper.isExpanded()) {
            return;
        }

        parentWrapper.setExpanded(false);

        // remove realm change listener from children
        P parent = parentWrapper.getParent();
        int parentPosition = parentList.indexOf(parent);
        removeChildCollectionListener(parentPosition);

        List<ExpandableWrapper<P, C>> wrappedChildList = parentWrapper.getWrappedChildList();
        if (wrappedChildList != null) {
            int childCount = wrappedChildList.size();
            for (int i = childCount - 1; i >= 0; i--) {
                flatItemList.remove(flatParentPosition + i + 1);
            }

            notifyItemRangeRemoved(flatParentPosition + 1, childCount);
        }

        if (collapseTriggeredByListItemClick && expandCollapseListener != null) {
            expandCollapseListener.onParentCollapsed(getNearestParentPosition(flatParentPosition));
        }
    }

    /**
     * Given the index relative to the entire RecyclerView, returns the nearest
     * ParentPosition without going past the given index.
     * <p>
     * If it is the index of a parent, will return the corresponding parent position.
     * If it is the index of a child within the RV, will return the position of that child's parent.
     */
    @UiThread
    int getNearestParentPosition(int flatPosition) {
        if (flatPosition == 0) {
            return 0;
        }

        int parentCount = -1;
        for (int i = 0; i <= flatPosition; i++) {
            ExpandableWrapper<P, C> listItem = flatItemList.get(i);
            if (listItem.isParent()) {
                parentCount++;
            }
        }
        return parentCount;
    }

    /**
     * Given the index relative to the entire RecyclerView for a child item,
     * returns the child position within the child list of the parent.
     */
    @UiThread
    int getChildPosition(int flatPosition) {
        if (flatPosition == 0) {
            return 0;
        }

        int childCount = 0;
        for (int i = 0; i < flatPosition; i++) {
            ExpandableWrapper<P, C> listItem = flatItemList.get(i);
            if (listItem.isParent()) {
                childCount = 0;
            } else {
                childCount++;
            }
        }
        return childCount;
    }

    /**
     * @param parentPosition index relative to the parent list for a parent item
     * @return parent position relative to the entire RecyclerView or -1 if there aren't that many parents
     */
    @UiThread
    private int getFlatParentPosition(int parentPosition) {
        int parentCount = -1;
        for (int i = 0, size = flatItemList.size(); i < size; i++) {
            ExpandableWrapper<P, C> listItem = flatItemList.get(i);
            if (listItem.isParent()) {
                parentCount++;
                if (parentCount == parentPosition) {
                    return i;
                }
            }
        }
        return NO_POSITION;
    }

    /**
     * Notify any registered observers that the data set has changed.
     * <p>
     * This event does not specify what about the data set has changed, forcing
     * any observers to assume that all existing items and structure may no longer be valid.
     * LayoutManagers will be forced to fully rebind and relayout all visible views.</p>
     */
    @UiThread
    public void notifyParentDataSetChanged() {
        flatItemList = generateFlattenedParentChildList(getData());
        super.notifyDataSetChanged();
    }

    /**
     * Generates a full list of all parents and their children, in order.
     *
     * @param parentList A list of the parents from
     *                   the {@link RealmExpandableRecyclerAdapter}
     * @return A list of all parents and their children, expanded
     */
    private RealmList<ExpandableWrapper<P, C>> generateFlattenedParentChildList(@NonNull OrderedRealmCollection<P> parentList) {
        RealmList<ExpandableWrapper<P, C>> flatItemList = new RealmList<>();
        for (int i = 0, size = parentList.size(); i < size; i++) {
            P parent = parentList.get(i);
            generateParentWrapper(flatItemList, parent, parent.isExpanded());
        }
        return flatItemList;
    }

    private void generateParentWrapper(RealmList<ExpandableWrapper<P, C>> flatItemList, P parent, boolean shouldExpand) {
        ExpandableWrapper<P, C> parentWrapper = new ExpandableWrapper<>(parent);
        flatItemList.add(parentWrapper);
        if (shouldExpand) {
            generateExpandedChildren(flatItemList, parentWrapper);
        }
    }

    private void generateExpandedChildren(RealmList<ExpandableWrapper<P, C>> flatItemList, ExpandableWrapper<P, C> parentWrapper) {
        parentWrapper.setExpanded(true);

        RealmList<ExpandableWrapper<P, C>> wrappedChildList = parentWrapper.getWrappedChildList();
        for (int j = 0, size = wrappedChildList.size(); j < size; j++) {
            ExpandableWrapper<P, C> childWrapper = wrappedChildList.get(j);
            flatItemList.add(childWrapper);
        }
    }
}
