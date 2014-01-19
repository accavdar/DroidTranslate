package net.cavdar.android.droidtranslate.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.cavdar.android.droidtranslate.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}