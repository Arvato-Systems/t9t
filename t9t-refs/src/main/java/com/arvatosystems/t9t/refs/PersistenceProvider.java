package com.arvatosystems.t9t.refs;

/**
 * API provided by persistence implementations like JPA, in-memory storage, noSQL providers...
 *
 * PersistenceProviders register at the RequestContext in order to get their commit / rollback callback invoked at the end of the request.
 * */
public interface PersistenceProvider extends AutoCloseable {
    /** Returns some identification of the provider. (PersistenceProviders.name()) */
    String getId();

    /** Returns the numeric ID for the provider. (PersistenceProviders.ordinal()) */
    int getPriority();

    /** Starts a transaction. */
    void open();

    /** Rolls back the current transaction. */
    void rollback();

    /** Commits the current transaction. */
    void commit() throws Exception;

    /** Closes the context, should be preceeded by commit() or rollback(). */
    @Override
    void close() throws Exception;
}
