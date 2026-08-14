package com.bigsing.fakemap.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapProviderSelectorTest {
    @Test
    public void autoUsesAmapOnlyForGcjTargetWhenCredentialsExist() {
        assertEquals(
                MapProviderSelector.AMAP,
                MapProviderSelector.resolve(
                        MapProviderSelector.AUTO,
                        new GeoPoint(39.9042, 116.4074),
                        true));
        assertEquals(
                MapProviderSelector.OSM,
                MapProviderSelector.resolve(
                        MapProviderSelector.AUTO,
                        new GeoPoint(39.9042, 116.4074),
                        false));
        assertEquals(
                MapProviderSelector.OSM,
                MapProviderSelector.resolve(
                        MapProviderSelector.AUTO,
                        new GeoPoint(51.5074, -0.1278),
                        true));
    }

    @Test
    public void explicitAmapIsLimitedToMainlandTargetsWithCredentials() {
        assertEquals(
                MapProviderSelector.OSM,
                MapProviderSelector.resolve(
                        MapProviderSelector.AMAP,
                        new GeoPoint(39.9042, 116.4074),
                        false));
        assertEquals(
                MapProviderSelector.AMAP,
                MapProviderSelector.resolve(
                        MapProviderSelector.AMAP,
                        new GeoPoint(39.9042, 116.4074),
                        true));
        assertEquals(
                MapProviderSelector.OSM,
                MapProviderSelector.resolve(
                        MapProviderSelector.AMAP,
                        new GeoPoint(51.5074, -0.1278),
                        true));
    }
}
