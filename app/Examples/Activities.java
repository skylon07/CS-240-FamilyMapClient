package Examples;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class Activities extends AppCompatActivity {
    public void callOtherAvtivity() {
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // since this is usually done in event handlers, which are
                // anonymous classes, we don't want to use the "this" of
                // the handler, so we specify the "this" referring to the
                // "Activities" instance
                Intent intent = new Intent(Activities.this, OtherActivity.class);
                getIntent().putExtra("data key", "value to pass to OtherActivity");
                Activities.this.startActivity(intent);
            }
        };

        // no need to do the weird "this" stuff above for lambdas
        this.useCallback(() -> {
            Intent intent = new Intent(this, OtherActivity.class);
            getIntent().putExtra("data key", "value to pass to OtherActivity");
            this.startActivity(intent);
        });
    }

    public void useCallback(EmptyCallback callback) {}
    public interface EmptyCallback {
        void call();
    }

    public class OtherActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Intent intent = this.getIntent();
            String dataValue = intent.getStringExtra("data key");

            // this is for the "response" example
            Intent responseData = new Intent();
            responseData.putExtra("response key", "response data value");
            this.setResult(Activity.RESULT_OK, responseData);
        }
    }

    public void callActivityAndGetResult() {
        ActivityResultLauncher<Intent> launcher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent resultData = result.getData();
                            if (resultData != null) {
                                String response = resultData.getStringExtra("response key");
                            }
                        }
                    }
                }
        );
        this.useCallback(new EmptyCallback() {
            @Override
            public void call() {
                Intent intent = new Intent(Activities.this, OtherActivity.class);
                launcher.launch(intent);
            }
        });

        // lambdas rule!!!
        ActivityResultLauncher<Intent> launcherWithLambda = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                // lambda functions work here!
                (ActivityResult result) -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent resultData = result.getData();
                        if (resultData != null) {
                            String response = resultData.getStringExtra("response key");
                        }
                    }
                }
        );
        this.useCallback(() -> {
            Intent intent = new Intent(this, OtherActivity.class);
            launcher.launch(intent);
        });
    }
}
