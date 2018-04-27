package ru.puzpuzpuz.http.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple lock with optimistic locking support. Based on {@link AtomicBoolean}.
 * <p>
 * Note: StampedLock is not used as it's only available since Java 8.
 */
public final class OptimisticLock {

    private final AtomicBoolean lockMarker = new AtomicBoolean(false);

    public void lock() {
        while (!tryLock()) {
            // keep the loop
        }
    }

    public boolean tryLock() {
        return lockMarker.compareAndSet(false, true);
    }

    public void unlock() {
        lockMarker.set(false);
    }

}
