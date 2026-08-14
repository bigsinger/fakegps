package com.bigsing.fakemap.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CoordinateTransformTest {
    @Test
    public void beijingRoundTripKeepsWgs84Coordinate() {
        GeoPoint original = new GeoPoint(39.908823, 116.397470);

        GeoPoint gcj = CoordinateTransform.wgs84ToGcj02(original);
        GeoPoint restored = CoordinateTransform.gcj02ToWgs84(gcj);

        assertTrue(Math.abs(gcj.longitude - original.longitude) > 0.001);
        assertEquals(original.latitude, restored.latitude, 1e-7);
        assertEquals(original.longitude, restored.longitude, 1e-7);
    }

    @Test
    public void coordinateOutsideChinaIsNotChanged() {
        GeoPoint london = new GeoPoint(51.5074, -0.1278);

        assertSame(london, CoordinateTransform.wgs84ToGcj02(london));
        assertSame(london, CoordinateTransform.gcj02ToWgs84(london));
    }

    @Test
    public void nearbyCountriesAreNotTreatedAsGcjRegion() {
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(27.7172, 85.3240)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(37.5665, 126.9780)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(47.8864, 106.9057)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(22.3193, 114.1694)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(22.1987, 113.5439)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(25.0330, 121.5654)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(27.9490, 85.9420)));
        assertTrue(!CoordinateTransform.usesGcj02(new GeoPoint(40.1006, 124.3980)));
    }

    @Test
    public void hainanAndShenzhenUseGcj02() {
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(20.0440, 110.1999)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(22.5431, 114.0579)));
    }

    @Test
    public void mainlandBorderCitiesUseGcj02() {
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(24.0128, 97.8519)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(25.0208, 98.4973)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(22.0017, 100.7974)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(22.5296, 103.9394)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(27.4860, 88.9070)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(46.7450, 82.9800)));
    }

    @Test
    public void mainlandCoastalIslandsUseGcj02() {
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(24.4798, 118.0894)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(24.4470, 118.0660)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(30.0200, 122.1060)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(25.5030, 119.7900)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(31.6220, 121.5260)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(21.0390, 109.1190)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(23.7040, 117.4280)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(21.6190, 111.9100)));
        assertTrue(CoordinateTransform.usesGcj02(new GeoPoint(25.2180, 119.5480)));
    }

    @Test
    public void coastalIslandRoundTripKeepsWgs84Coordinate() {
        GeoPoint original = new GeoPoint(24.4470, 118.0660);

        GeoPoint restored = CoordinateTransform.gcj02ToWgs84(
                CoordinateTransform.wgs84ToGcj02(original));

        assertEquals(original.latitude, restored.latitude, 1e-7);
        assertEquals(original.longitude, restored.longitude, 1e-7);
    }
}
