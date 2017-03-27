package io.realm;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.realm.model.Child;

/**
 * ViewHolder for a child list item.
 * <p>
 * The user should extend this class and implement as they wish for their
 * child list item.
 */
public class ChildViewHolder<C extends Child> extends RecyclerView.ViewHolder {
    C child;
    RealmExpandableRecyclerAdapter expandableAdapter;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public ChildViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * @return the childListItem associated with this view holder
     */
    @UiThread
    public C getChild() {
        return child;
    }

    /**
     * Returns the adapter position of the Parent associated with this ChildViewHolder
     *
     * @return The adapter position of the Parent if it still exists in the adapter.
     * RecyclerView.NO_POSITION if item has been removed from the adapter,
     * RecyclerView.Adapter.notifyDataSetChanged() has been called after the last
     * layout pass or the ViewHolder has already been recycled.
     */
    @UiThread
    public int getParentAdapterPosition() {
        int flatPosition = getAdapterPosition();
        if (expandableAdapter == null || flatPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        return expandableAdapter.getNearestParentPosition(flatPosition);
    }

    /**
     * Returns the adapter position of the Child associated with this ChildViewHolder
     *
     * @return The adapter position of the Child if it still exists in the adapter.
     * RecyclerView.NO_POSITION if item has been removed from the adapter,
     * RecyclerView.Adapter.notifyDataSetChanged() has been called after the last
     * layout pass or the ViewHolder has already been recycled.
     */
    @UiThread
    public int getChildAdapterPosition() {
        int flatPosition = getAdapterPosition();
        if (expandableAdapter == null || flatPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        return expandableAdapter.getChildPosition(flatPosition);
    }
}