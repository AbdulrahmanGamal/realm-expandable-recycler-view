package io.realm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.realm.model.Parent;
import io.realm.model.Child;

import static android.support.v7.widget.RecyclerView.NO_POSITION;


/**
 * ViewHolder for a {@link Parent}
 * Keeps track of expanded state and holds callbacks which can be used to
 * trigger expansion-based events.
 */
public class ParentViewHolder<P extends Parent<C>, C extends Child> extends RecyclerView.ViewHolder implements View.OnClickListener {
    @Nullable
    private ParentViewHolderExpandCollapseListener parentViewHolderExpandCollapseListener;
    private boolean expanded;
    P parent;
    RealmExpandableRecyclerAdapter expandableAdapter;

    /**
     * Empowers {@link RealmExpandableRecyclerAdapter}
     * implementations to be notified of expand/collapse state change events.
     */
    public interface ParentViewHolderExpandCollapseListener {

        /**
         * Called when a parent is expanded.
         *
         * @param flatParentPosition The index of the parent in the list being expanded
         */
        @UiThread
        void onParentExpanded(int flatParentPosition);

        /**
         * Called when a parent is collapsed.
         *
         * @param flatParentPosition The index of the parent in the list being collapsed
         */
        @UiThread
        void onParentCollapsed(int flatParentPosition);
    }

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    @UiThread
    public ParentViewHolder(@NonNull View itemView) {
        super(itemView);
        expanded = false;
    }

    /**
     * @return the Parent associated with this ViewHolder
     */
    @UiThread
    public P getParent() {
        return parent;
    }

    /**
     * Returns the adapter position of the Parent associated with this ParentViewHolder
     *
     * @return The adapter position of the Parent if it still exists in the adapter.
     * RecyclerView.NO_POSITION if item has been removed from the adapter,
     * RecyclerView.Adapter.notifyDataSetChanged() has been called after the last
     * layout pass or the ViewHolder has already been recycled.
     */
    @UiThread
    public int getParentAdapterPosition() {
        int flatPosition = getAdapterPosition();
        if (flatPosition == NO_POSITION) {
            return flatPosition;
        }

        return expandableAdapter.getNearestParentPosition(flatPosition);
    }

    /**
     * Sets a {@link View.OnClickListener} on the entire parent
     * view to trigger expansion.
     */
    @UiThread
    public void setMainItemClickToExpand() {
        itemView.setOnClickListener(this);
    }

    /**
     * Returns expanded state for the {@link Parent}
     * corresponding to this {@link ParentViewHolder}.
     *
     * @return true if expanded, false if not
     */
    @UiThread
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Setter method for expanded state, used for initialization of expanded state.
     * changes to the state are given in {@link #onExpansionToggled(boolean)}
     *
     * @param expanded true if expanded, false if not
     */
    @UiThread
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Callback triggered when expansion state is changed, but not during
     * initialization.
     * <p>
     * Useful for implementing animations on expansion.
     *
     * @param expanded true if view is expanded before expansion is toggled,
     *                 false if not
     */
    @UiThread
    public void onExpansionToggled(boolean expanded) {

    }

    /**
     * Setter for the {@link ParentViewHolderExpandCollapseListener} implemented in
     * {@link RealmExpandableRecyclerAdapter}.
     *
     * @param parentViewHolderExpandCollapseListener The {@link ParentViewHolderExpandCollapseListener} to set on the {@link ParentViewHolder}
     */
    @UiThread
    void setParentViewHolderExpandCollapseListener(@Nullable ParentViewHolderExpandCollapseListener parentViewHolderExpandCollapseListener) {
        this.parentViewHolderExpandCollapseListener = parentViewHolderExpandCollapseListener;
    }

    /**
     * {@link View.OnClickListener} to listen for click events on
     * the entire parent {@link View}.
     * <p>
     * Only registered if {@link #shouldItemViewClickToggleExpansion()} is true.
     *
     * @param v The {@link View} that is the trigger for expansion
     */
    @Override
    @UiThread
    public void onClick(View v) {
        if (expanded) {
            collapseView();
        } else {
            expandView();
        }
    }

    /**
     * Used to determine whether a click in the entire parent {@link View}
     * should trigger row expansion.
     * <p>
     * If you return false, you can call {@link #expandView()} to trigger an
     * expansion in response to a another event or {@link #collapseView()} to
     * trigger a collapse.
     *
     * @return true to set an {@link View.OnClickListener} on the item view
     */
    @UiThread
    public boolean shouldItemViewClickToggleExpansion() {
        return true;
    }

    /**
     * Triggers expansion of the parent.
     */
    @UiThread
    protected void expandView() {
        if (parentViewHolderExpandCollapseListener != null) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition == NO_POSITION) return;
            parentViewHolderExpandCollapseListener.onParentExpanded(adapterPosition);
        }

        setExpanded(true);
        onExpansionToggled(false);
    }

    /**
     * Triggers collapse of the parent.
     */
    @UiThread
    protected void collapseView() {
        if (parentViewHolderExpandCollapseListener != null) {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition == NO_POSITION) return;
            parentViewHolderExpandCollapseListener.onParentCollapsed(adapterPosition);
        }

        setExpanded(false);
        onExpansionToggled(true);
    }
}
