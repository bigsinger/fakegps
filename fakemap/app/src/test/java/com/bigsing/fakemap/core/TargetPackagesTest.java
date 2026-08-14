package com.bigsing.fakemap.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetPackagesTest {
    @Test
    public void parsesSeveralSeparatorsAndDeduplicates() {
        TargetPackages.ParseResult result = TargetPackages.parse(
                "com.example.one\ncom.example.two,com.example.one");

        assertTrue(result.valid);
        assertEquals(2, result.packages.size());
    }

    @Test
    public void rejectsCoreSystemAndMalformedPackages() {
        assertFalse(TargetPackages.parse("android").valid);
        assertFalse(TargetPackages.parse("com.android.settings").valid);
        assertFalse(TargetPackages.parse("not-a-package").valid);
    }

    @Test
    public void allowsNormalThirdPartyPackage() {
        assertTrue(TargetPackages.parse("org.example.locationtest").valid);
    }
}
