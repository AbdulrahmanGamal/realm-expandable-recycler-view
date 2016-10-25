package com.bignerdranch.expandablerecyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.model.ExpandableWrapper;
import com.bignerdranch.expandablerecyclerview.model.MockRealmObject;
import com.bignerdranch.expandablerecyclerview.model.Parent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RealmExpandableRecyclerAdapterTest {

    private TestRealmExpandableRecyclerAdapter mExpandableRecyclerAdapter;
    private RealmList<Parent<MockRealmObject>> mBaseParents;
    private AdapterDataObserver mDataObserver;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mBaseParents = new RealmList<>();

        for (int i = 0; i < 10; i++) {
            Parent<MockRealmObject> parent = generateParent(i % 2 == 0, 3);
            mBaseParents.add(parent);
        }

        mExpandableRecyclerAdapter = new TestRealmExpandableRecyclerAdapter(mBaseParents);
        mDataObserver = TestUtils.fixAdapterForTesting(mExpandableRecyclerAdapter);
    }

    @Test
    public void adapterCorrectlyInitializesExpandedParents() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
    }

    @Test
    public void adapterCorrectlyInitializesExpandedParentsWhenAllParentItemsAreInitiallyCollapsed() {
        for (Parent parent : mBaseParents) {
            when(parent.isInitiallyExpanded()).thenReturn(false);
        }
        mExpandableRecyclerAdapter = new TestRealmExpandableRecyclerAdapter(mBaseParents);

        assertEquals(10, mExpandableRecyclerAdapter.getItemCount());
    }

    @Test
    public void adapterCorrectlyInitializesExpandedParentsWhenAllParentItemsAreInitiallyExpanded() {
        for (Parent parent : mBaseParents) {
            when(parent.isInitiallyExpanded()).thenReturn(true);
        }
        mExpandableRecyclerAdapter = new TestRealmExpandableRecyclerAdapter(mBaseParents);

        assertEquals(40, mExpandableRecyclerAdapter.getItemCount());
    }

    @Test
    public void collapsingExpandedParentWithIndexRemovesChildren() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), true, 0);
        verifyParentItemsMatch(mBaseParents.get(1), false, 4);

        mExpandableRecyclerAdapter.collapseParent(0);

        verify(mDataObserver).onItemRangeRemoved(1, 3);
        assertEquals(22, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), false, 0);
        verifyParentItemsMatch(mBaseParents.get(1), false, 1);
    }

    @Test
    public void collapsingExpandedParentWithRealmObjectRemovesChildren() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), true, 0);
        verifyParentItemsMatch(mBaseParents.get(1), false, 4);

        Parent<MockRealmObject> firstParent = mBaseParents.get(0);
        mExpandableRecyclerAdapter.collapseParent(firstParent);

        verify(mDataObserver).onItemRangeRemoved(1, 3);
        assertEquals(22, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), false, 0);
        verifyParentItemsMatch(mBaseParents.get(1), false, 1);
    }

    @Test
    public void collapsingCollapsedParentWithIndexHasNoEffect() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), false, 24);

        mExpandableRecyclerAdapter.collapseParent(9);

        verify(mDataObserver, never()).onItemRangeRemoved(anyInt(), anyInt());
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), false, 24);
    }

    @Test
    public void collapsingCollapsedParentWithRealmObjectHasNoEffect() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), false, 24);

        mExpandableRecyclerAdapter.collapseParent(mBaseParents.get(9));

        verify(mDataObserver, never()).onItemRangeRemoved(anyInt(), anyInt());
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), false, 24);
    }

    @Test
    public void expandingParentWithIndexAddsChildren() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), false, 24);

        mExpandableRecyclerAdapter.expandParent(9);

        verify(mDataObserver).onItemRangeInserted(25, 3);
        assertEquals(28, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), true, 24);
    }

    @Test
    public void expandingParentWithRealmObjectAddsChildren() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), false, 24);

        mExpandableRecyclerAdapter.expandParent(mBaseParents.get(9));

        verify(mDataObserver).onItemRangeInserted(25, 3);
        assertEquals(28, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(9), true, 24);
    }

    @Test
    public void expandingExpandedParentWithIndexHasNoEffect() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), true, 0);

        mExpandableRecyclerAdapter.expandParent(0);

        verify(mDataObserver, never()).onItemRangeInserted(anyInt(), anyInt());
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), true, 0);
    }

    @Test
    public void expandingExpandedWithRealmObjectParentHasNoEffect() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), true, 0);

        mExpandableRecyclerAdapter.expandParent(mBaseParents.get(0));

        verify(mDataObserver, never()).onItemRangeInserted(anyInt(), anyInt());
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), true, 0);
    }

    @Test
    public void notifyParentInsertedWithInitiallyCollapsedItem() {
        Parent<MockRealmObject> originalFirstItem = mBaseParents.get(0);
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(originalFirstItem, true, 0);

        Parent<MockRealmObject> insertedItem = generateParent(false, 2);
        when(insertedItem.isInitiallyExpanded()).thenReturn(false);
        mBaseParents.add(0, insertedItem);
        mExpandableRecyclerAdapter.notifyParentInserted(0);

        verify(mDataObserver).onItemRangeInserted(0, 1);
        assertEquals(26, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(insertedItem, false, 0);
        verifyParentItemsMatch(originalFirstItem, true, 1);
    }

    @Test
    public void notifyParentInsertedWithInitiallyExpandedItem() {
        Parent<MockRealmObject> originalLastParent = mBaseParents.get(9);
        Parent<MockRealmObject> insertedItem = generateParent(true, 3);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(originalLastParent, false, 24);

        mBaseParents.add(insertedItem);
        mExpandableRecyclerAdapter.notifyParentInserted(10);

        verify(mDataObserver).onItemRangeInserted(25, 4);
        assertEquals(29, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(originalLastParent, false, 24);
        verifyParentItemsMatch(insertedItem, true, 25);
    }

    @Test
    public void notifyParentRangeInsertedMidList() {
        Parent<MockRealmObject> firstInsertedItem = generateParent(true, 3);
        Parent<MockRealmObject> secondInsertedItem = generateParent(false, 2);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());

        mBaseParents.add(4, firstInsertedItem);
        mBaseParents.add(5, secondInsertedItem);
        mExpandableRecyclerAdapter.notifyParentRangeInserted(4, 2);

        verify(mDataObserver).onItemRangeInserted(10, 5);
        assertEquals(30, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(firstInsertedItem, true, 10);
        verifyParentItemsMatch(secondInsertedItem, false, 14);
    }

    @Test
    public void notifyParentRangeInsertedEndList() {
        Parent<MockRealmObject> firstInsertedItem = generateParent(true, 3);
        Parent<MockRealmObject> secondInsertedItem = generateParent(false, 2);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());

        mBaseParents.add(firstInsertedItem);
        mBaseParents.add(secondInsertedItem);
        mExpandableRecyclerAdapter.notifyParentRangeInserted(10, 2);

        verify(mDataObserver).onItemRangeInserted(25, 5);
        assertEquals(30, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(firstInsertedItem, true, 25);
        verifyParentItemsMatch(secondInsertedItem, false, 29);
    }

    @Test
    public void notifyParentRemovedOnExpandedItem() {
        Parent<MockRealmObject> removedItem = mBaseParents.get(0);
        Parent<MockRealmObject> originalSecondItem = mBaseParents.get(1);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(removedItem, true, 0);
        verifyParentItemsMatch(originalSecondItem, false, 4);

        mBaseParents.remove(0);
        mExpandableRecyclerAdapter.notifyParentRemoved(0);

        verify(mDataObserver).onItemRangeRemoved(0, 4);
        assertEquals(21, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(originalSecondItem, false, 0);
    }

    @Test
    public void notifyParentRemovedOnCollapsedItem() {
        Parent<MockRealmObject> removedItem = mBaseParents.get(9);
        Parent<MockRealmObject> originalSecondToLastItem = mBaseParents.get(8);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(removedItem, false, 24);
        verifyParentItemsMatch(originalSecondToLastItem, true, 20);

        mBaseParents.remove(9);
        mExpandableRecyclerAdapter.notifyParentRemoved(9);

        verify(mDataObserver).onItemRangeRemoved(24, 1);
        assertEquals(24, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(originalSecondToLastItem, true, 20);
    }

    @Test
    public void notifyParentRangeRemoved() {
        Parent<MockRealmObject> firstRemovedItem = mBaseParents.get(7);
        Parent<MockRealmObject> secondRemovedItem = mBaseParents.get(8);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(firstRemovedItem, false, 19);
        verifyParentItemsMatch(secondRemovedItem, true, 20);

        mBaseParents.remove(7);
        mBaseParents.remove(7);
        mExpandableRecyclerAdapter.notifyParentRangeRemoved(7, 2);

        verify(mDataObserver).onItemRangeRemoved(19, 5);
        assertEquals(20, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(7), false, 19);
    }

    @Test
    public void notifyParentChanged() {
        Parent<MockRealmObject> changedParent = generateParent(false, 3);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(4), true, 10);

        mBaseParents.set(4, changedParent);
        mExpandableRecyclerAdapter.notifyParentChanged(4);

        verify(mDataObserver).onItemRangeChanged(10, 4, null);
        verifyParentItemsMatch(changedParent, true, 10);
    }

    @Test
    public void notifyParentRangeChanged() {
        Parent<MockRealmObject> firstChangedParent = generateParent(false, 3);
        Parent<MockRealmObject> secondChangedParent = generateParent(false, 3);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(4), true, 10);
        verifyParentItemsMatch(mBaseParents.get(5), false, 14);

        mBaseParents.set(4, firstChangedParent);
        mBaseParents.set(5, secondChangedParent);
        mExpandableRecyclerAdapter.notifyParentRangeChanged(4, 2);

        verify(mDataObserver).onItemRangeChanged(10, 5, null);
        verifyParentItemsMatch(firstChangedParent, true, 10);
        verifyParentItemsMatch(secondChangedParent, false, 14);
    }

    @Test
    public void notifyParentMovedCollapsedParent() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(5), false, 14);

        Parent<MockRealmObject> movedParent = mBaseParents.remove(5);
        mBaseParents.add(movedParent);
        mExpandableRecyclerAdapter.notifyParentMoved(5, 9);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(movedParent, false, 24);
    }

    @Test
    public void notifyParentMovedExpandedParent() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(6), true, 15);

        Parent<MockRealmObject> movedParent = mBaseParents.remove(6);
        mBaseParents.add(8, movedParent);
        mExpandableRecyclerAdapter.notifyParentMoved(6, 8);

        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(movedParent, true, 20);
    }

    @Test
    public void notifyParentDataSetChangedWithExpansionPreservationAllCollapsed() {
        mExpandableRecyclerAdapter.collapseAllParents();
        Parent<MockRealmObject> movedParent = mBaseParents.remove(0);
        mBaseParents.add(movedParent);
        Parent<MockRealmObject> newParent = generateParent(true, 1);
        mBaseParents.add(3, newParent);
        mExpandableRecyclerAdapter.notifyParentDataSetChanged(true);

        verify(mDataObserver).onChanged();
        assertEquals(12, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), false, 0);
        verifyParentItemsMatch(newParent, true, 3);
        verifyParentItemsMatch(mBaseParents.get(9), false, 10);
        verifyParentItemsMatch(movedParent, false, 11);
    }

    @Test
    public void notifyParentDataSetChangedWithoutExpansionPreservationAllCollapsed() {
        mExpandableRecyclerAdapter.collapseAllParents();
        Parent<MockRealmObject> movedParent = mBaseParents.remove(0);
        mBaseParents.add(movedParent);
        Parent<MockRealmObject> newParent = generateParent(true, 1);
        mBaseParents.add(3, newParent);
        mExpandableRecyclerAdapter.notifyParentDataSetChanged(false);

        verify(mDataObserver).onChanged();
        assertEquals(27, mExpandableRecyclerAdapter.getItemCount());
        verifyParentItemsMatch(mBaseParents.get(0), false, 0);
        verifyParentItemsMatch(newParent, true, 6);
        verifyParentItemsMatch(mBaseParents.get(9), false, 22);
        verifyParentItemsMatch(mBaseParents.get(10), true, 23);
    }

    @Test
    public void notifyParentDataSetWithExpansionPreservationChangedNoChanges() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());

        mExpandableRecyclerAdapter.notifyParentDataSetChanged(true);

        verify(mDataObserver).onChanged();
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        int flatIndex = 0;
        for (Parent<MockRealmObject> baseParent : mBaseParents) {
            verifyParentItemsMatch(baseParent, baseParent.isInitiallyExpanded(), flatIndex);
            flatIndex++;
            if (baseParent.isInitiallyExpanded()) {
                flatIndex += baseParent.getChildList().size();
            }
        }
    }

    @Test
    public void notifyParentDataSetWithoutExpansionPreservationChangedNoChanges() {
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());

        mExpandableRecyclerAdapter.notifyParentDataSetChanged(false);

        verify(mDataObserver).onChanged();
        assertEquals(25, mExpandableRecyclerAdapter.getItemCount());
        int flatIndex = 0;
        for (Parent<MockRealmObject> baseParent : mBaseParents) {
            verifyParentItemsMatch(baseParent, baseParent.isInitiallyExpanded(), flatIndex);
            flatIndex++;
            if (baseParent.isInitiallyExpanded()) {
                flatIndex += baseParent.getChildList().size();
            }
        }
    }

    private void verifyParentItemsMatch(Parent<MockRealmObject> expectedParent, boolean expectedExpansion, int actualParentIndex) {
        assertEquals(expectedParent, getListItem(actualParentIndex));
        assertEquals(expectedExpansion, mExpandableRecyclerAdapter.mFlatItemList.get(actualParentIndex).isExpanded());

        if (expectedExpansion) {
            List<MockRealmObject> expectedChildList = expectedParent.getChildList();
            int actualChildIndex = actualParentIndex + 1;
            for (int i = 0; i < expectedChildList.size(); i++) {
                assertEquals(expectedChildList.get(i), getListItem(actualChildIndex));
                actualChildIndex++;
            }
        }
    }

    private Parent<MockRealmObject> generateParent(boolean initiallyExpanded, int childCount) {
        List<MockRealmObject> childRealmObjects = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            childRealmObjects.add(new MockRealmObject());
        }
        Parent<MockRealmObject> parent = (Parent<MockRealmObject>) mock(Parent.class);
        Mockito.when(parent.getChildList()).thenReturn(childRealmObjects);
        when(parent.isInitiallyExpanded()).thenReturn(initiallyExpanded);

        return parent;
    }

    protected Object getListItem(int flatPosition) {
        ExpandableWrapper<Parent<MockRealmObject>, MockRealmObject> listItem = mExpandableRecyclerAdapter.mFlatItemList.get(flatPosition);
        if (listItem.isParent()) {
            return listItem.getParent();
        } else {
            return listItem.getChild();
        }
    }

    private static class TestRealmExpandableRecyclerAdapter extends RealmExpandableRecyclerAdapter<Parent<MockRealmObject>, MockRealmObject, ParentViewHolder, ChildViewHolder> {

        public TestRealmExpandableRecyclerAdapter(@NonNull RealmList<Parent<MockRealmObject>> parentList) {
            super(null, parentList);
        }

        @NonNull
        @Override
        public ParentViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
            return null;
        }

        @Override
        public ChildViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
            return null;
        }

        @Override
        public void onBindParentViewHolder(@NonNull ParentViewHolder parentViewHolder, int parentPosition, @NonNull Parent<MockRealmObject> parent) {

        }

        @Override
        public void onBindChildViewHolder(@NonNull ChildViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull MockRealmObject child) {

        }
    }
}
