package com.bigsing.fakemap.ui;

import android.app.Activity;
import android.os.Bundle;

import com.bigsing.fakemap.R;

public final class HelpActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        SystemBarInsets.applyToContentRoot(this);
        findViewById(R.id.button_help_close).setOnClickListener(view -> finish());
    }
}
