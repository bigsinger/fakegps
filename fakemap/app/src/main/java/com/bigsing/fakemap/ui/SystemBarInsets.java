package com.bigsing.fakemap.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

/** Keeps content controls outside mandatory system-bar and display-cutout areas. */
final class SystemBarInsets {
    private SystemBarInsets() {
    }

    static void applyToContentRoot(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return;
        }
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }
        View root = content.getChildAt(0);
        Api35.apply(
                root,
                root.getPaddingLeft(),
                root.getPaddingTop(),
                root.getPaddingRight(),
                root.getPaddingBottom());
    }

    @SuppressLint("NewApi")
    private static final class Api35 {
        private Api35() {
        }

        static void apply(
                View root,
                int baseLeft,
                int baseTop,
                int baseRight,
                int baseBottom) {
            root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
                Insets safeInsets = windowInsets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(
                        baseLeft + safeInsets.left,
                        baseTop + safeInsets.top,
                        baseRight + safeInsets.right,
                        baseBottom + safeInsets.bottom);
                return windowInsets;
            });
            root.requestApplyInsets();
        }
    }
}
