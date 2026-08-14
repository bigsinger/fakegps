package com.bigsing.fakemap.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeakIdentitySetTest {
    @Test
    public void equalObjectsRemainDifferentKeys() {
        WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        String first = new String("same");
        String second = new String("same");

        assertTrue(set.add(first));
        assertTrue(set.contains(first));
        assertFalse(set.contains(second));
    }

    @Test
    public void mutableHashCodeDoesNotLoseIdentity() {
        WeakIdentitySet<MutableValue> set = new WeakIdentitySet<>();
        MutableValue value = new MutableValue();

        assertTrue(set.add(value));
        value.state = 42;

        assertTrue(set.contains(value));
        assertFalse(set.contains(null));
    }

    private static final class MutableValue {
        private int state;

        @Override
        public int hashCode() {
            return state;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MutableValue
                    && state == ((MutableValue) other).state;
        }
    }
}
