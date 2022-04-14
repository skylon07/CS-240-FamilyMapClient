package Examples;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class Lifecycles extends AppCompatActivity {
    private LifecyclesViewModel getViewModel() {
        return new ViewModelProvider(this).get(LifecyclesViewModel.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // executes when the system creates the activity

        // an example of an off-loaded object in memory, which won't
        // be destroyed (when the activity is destroyed, at least)
        String savedInRam = this.getViewModel().getStateVal();

        // an example of writing to disk; more persistent, and stays
        // intact across app instances
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        String savedInDisk = prefs.getString("state key", "default val if key no exist");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // executes when the activity is partially loaded
        // and visible to the user
        // (usually no need to implement)
    }

    @Override
    protected void onResume() {
        super.onResume();
        // executes when the activity is in the foreground
        // and ready for the user to interact with it
    }

    @Override
    protected void onPause() {
        super.onPause();
        // executes when the user is leaving the activity, and
        // the activity is no longer in the foreground
    }

    @Override
    protected void onStop() {
        super.onStop();
        // executes when the activity is no longer visible
        // to the user
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // executes when the activity was stopped but not
        // destroyed, and onStart() is about to be called again
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // executes when the activity is about to be destroyed
        // (activity is finished, device rotates or some other
        // config change, app is killed, etc)
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // executes between onPause() and onStop(), and allows
        // an opportunity to save state before the activity
        // loses data

        // an example of an off-loaded object in memory, which won't
        // be destroyed (when the activity is destroyed, at least)
        this.getViewModel().setStateVal("some val");

        // an example of writing to disk; more persistent, and stays
        // intact across app instances
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString("state key", "state value");
    }
}
