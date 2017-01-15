package io.realm.model;

import io.realm.RealmModel;
import io.realm.RealmQuery;

/**
 * Interface for implementing required methods in a child.
 */
public interface Child extends RealmModel {

    /**
     * Query for this {@link Child}
     *
     * @return the {@link RealmQuery} for this {@link Child}
     */
    RealmQuery<? extends Child> queryByPrimaryKey(RealmQuery<? extends Child> query);
}
