package com.bigsing.fakemap.core;

import com.bigsing.fakemap.BuildConfig;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class TargetPackages {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+");

    private TargetPackages() {
    }

    public static ParseResult parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ParseResult(true, Collections.emptySet());
        }
        LinkedHashSet<String> packages = new LinkedHashSet<>();
        String[] tokens = raw.split("[\\s,;，；]+");
        for (String token : tokens) {
            String packageName = token.trim();
            if (packageName.isEmpty()) {
                continue;
            }
            if (!PACKAGE_PATTERN.matcher(packageName).matches() || isProtected(packageName)) {
                return new ParseResult(false, Collections.emptySet());
            }
            packages.add(packageName);
        }
        return new ParseResult(true, Collections.unmodifiableSet(packages));
    }

    public static String format(Set<String> packageNames) {
        StringBuilder builder = new StringBuilder();
        for (String packageName : packageNames) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(packageName);
        }
        return builder.toString();
    }

    public static boolean isProtected(String packageName) {
        String lower = packageName.toLowerCase(Locale.ROOT);
        String ownPackage = BuildConfig.APPLICATION_ID.toLowerCase(Locale.ROOT);
        return lower.equals("android")
                || lower.equals("system")
                || lower.startsWith("com.android.")
                || lower.equals("com.google.android.gms")
                || lower.startsWith("com.google.android.gms.")
                || lower.equals(ownPackage)
                || lower.startsWith(ownPackage + ".");
    }

    public static final class ParseResult {
        public final boolean valid;
        public final Set<String> packages;

        private ParseResult(boolean valid, Set<String> packages) {
            this.valid = valid;
            this.packages = packages;
        }
    }
}
