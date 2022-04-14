package org.skylon07.familymapclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.skylon07.familymapclient.R;

/**
 * Renders the main settings screen for the app.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.settingsActivityLabel);
        this.setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsFrame, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // apparently trying to set the callback in onCreate() causes errors;
        // the fragment can't find its preference manager
        SettingsFragment settingsFragment = (SettingsFragment)
                this.getSupportFragmentManager().findFragmentById(R.id.settingsFrame);
        if (this.getIntent() != null) {
            settingsFragment.setOnLogoutListener(() -> {
                Intent intent = new Intent();
                intent.putExtra("shouldLogout", true);
                this.setResult(Activity.RESULT_OK, intent);
                this.finish();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem button) {
        if (button.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra("shouldLogout", false);
            this.setResult(Activity.RESULT_OK, intent);
            this.finish();
        }
        return true;
    }

    /**
     * A basic wrapper to include the settings XML in this activity
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            this.setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        public void setOnLogoutListener(Runnable listener) {
            PreferenceManager manager = this.getPreferenceManager();
            Preference logoutPref = manager.findPreference("logoutPref");
            logoutPref.setOnPreferenceClickListener((Preference pref) -> {
                listener.run();
                return true;
            });
        }
    }
}