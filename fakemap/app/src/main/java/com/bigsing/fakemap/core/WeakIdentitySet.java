package com.bigsing.fakemap.core;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe weak set whose keys are compared only by object identity. */
public final class WeakIdentitySet<T> {
    private final ReferenceQueue<T> queue = new ReferenceQueue<>();
    private final ConcurrentHashMap<IdentityReference<T>, Boolean> references =
            new ConcurrentHashMap<>();

    public boolean add(T value) {
        if (value == null) {
            return false;
        }
        drainQueue();
        return references.put(new IdentityReference<>(value, queue), Boolean.TRUE) == null;
    }

    public boolean contains(T value) {
        if (value == null) {
            return false;
        }
        drainQueue();
        return references.containsKey(new IdentityReference<>(value, null));
    }

    private void drainQueue() {
        Reference<? extends T> reference;
        while ((reference = queue.poll()) != null) {
            references.remove(reference);
        }
    }

    private static final class IdentityReference<T> extends WeakReference<T> {
        private final int identityHashCode;

        private IdentityReference(T value, ReferenceQueue<T> queue) {
            super(value, queue);
            identityHashCode = System.identityHashCode(value);
        }

        @Override
        public int hashCode() {
            return identityHashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof IdentityReference<?>)) {
                return false;
            }
            Object value = get();
            return value != null && value == ((IdentityReference<?>) other).get();
        }
    }
}
