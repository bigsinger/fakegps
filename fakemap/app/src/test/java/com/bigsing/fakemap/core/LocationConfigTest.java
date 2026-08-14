package com.bigsing.fakemap.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationConfigTest {
    @Test
    public void validatesRangesAndNormalizesBearing() {
        LocationConfig config = new LocationConfig(
                true, true, 30.0, 120.0, 5.0f, 20.0, 2.0f, 370.0f, 1L);

        assertTrue(config.isValid());
        assertEquals(10.0f, config.bearing, 0.0f);
    }

    @Test
    public void rejectsInvalidLatitude() {
        LocationConfig config = new LocationConfig(
                true, true, 91.0, 120.0, 5.0f, 20.0, 0.0f, 0.0f, 1L);

        assertFalse(config.isValid());
    }
}
