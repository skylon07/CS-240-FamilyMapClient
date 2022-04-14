package Examples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import org.skylon07.familymapclient.R;

public class FragmentEmbed extends AppCompatActivity {
    public class MyFragment extends androidx.fragment.app.Fragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // ...onCreate() things
        }

        // this is a little different from Activities;
        // the View must be created and returned
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // would normally put corresponding fragment xml here, not "activity_main"
            int layout = R.layout.activity_main;
            View view = inflater.inflate(layout, container, false);

            // do layout things here

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // pass arguments to fragment
        FragmentManager manager = this.getSupportFragmentManager();
        // the ID here would be the id assigned to the <fragment />
        MyFragment fragment = (MyFragment) manager.findFragmentById(R.id.mainFrameLayout);
        if (fragment == null) {
            fragment = new MyFragment();
            Bundle fragArgs = new Bundle();
            fragArgs.putString("arg key", "arg val");
            fragment.setArguments(fragArgs);

            manager.beginTransaction()
                    .add(R.id.mainFrameLayout, fragment)
                    .commit();
        }
    }
}
