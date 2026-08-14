package com.bigsing.fakemap.mock;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;

public final class MockLocationAppOps {
    private MockLocationAppOps() {
    }

    public static boolean isSelected(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(
                Context.APP_OPS_SERVICE);
        if (appOpsManager == null) {
            return false;
        }
        int mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_MOCK_LOCATION,
                Process.myUid(),
                context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
