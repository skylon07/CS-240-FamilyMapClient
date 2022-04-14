package org.skylon07.familymapclient.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import org.skylon07.familymapclient.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#createEmptyFragment()} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment createEmptyFragment() {
        return new LoginFragment();
    }

    private BooleanFormCallback shouldSignInBeEnabled;
    private BooleanFormCallback shouldRegisterBeEnabled;
    private FormCallback signInCallback;
    private FormCallback registerCallback;

    /**
     * Creates a new instance of a LoginFragment.
     * Android Studio says this empty constructor is required for other functions
     */
    public LoginFragment() {}

    /**
     * Registers a callback that defines when the Sign In button is enabled or not
     *
     * @param callback is the FormCallback returning whether the button should be enabled or not
     */
    public void setWhenSignInShouldBeEnabled(BooleanFormCallback callback) {
        this.shouldSignInBeEnabled = callback;
    }

    /**
     * Registers a callback that defines when the Register button is enabled or not
     *
     * @param callback is the FormCallback returning whether the button should be enabled or not
     */
    public void setWhenRegisterShouldBeEnabled(BooleanFormCallback callback) {
        this.shouldRegisterBeEnabled = callback;
    }

    /**
     * Registers a "Sign In" listener with this fragment
     *
     * @param callback is the FormCallback that is provided information about the sign in attempt
     */
    public void setSignInCallback(FormCallback callback) {
        this.signInCallback = callback;
    }

    /**
     * Registers a "Register" listener with this fragment
     *
     * @param callback is the FormCallback that is provided information about the register attempt
     */
    public void setRegisterCallback(FormCallback callback) {
        this.registerCallback = callback;
    }

    /**
     * Provides the interface for callbacks that need to know about the form state
     */
    public interface FormCallback {
        public void call(LoginFormState state);
    }

    /**
     * A FormCallback that returns a boolean value
     */
    public interface BooleanFormCallback {
        public boolean call(LoginFormState state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // handle sign in button
        Button signInButton = view.findViewById(R.id.signInButton);
        signInButton.setOnClickListener((View v) -> {
            if (this.signInCallback != null) {
                this.signInCallback.call(new LoginFormState(view));
            }
        });


        // handle register button
        Button registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener((View v) -> {
            if (this.registerCallback != null) {
                this.registerCallback.call(new LoginFormState(view));
            }
        });

        // handle enabling/disabling buttons
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                LoginFragment.this.checkToEnableSignIn(view);
                LoginFragment.this.checkToEnableRegister(view);
            }
        };
        this.forEachEditText(view, (EditText editText) -> {
            editText.addTextChangedListener(watcher);
        });
        RadioGroup genderGroup = view.findViewById(R.id.genderGroup);
        genderGroup.setOnCheckedChangeListener((RadioGroup g, int id) -> {
            this.checkToEnableSignIn(view);
            this.checkToEnableRegister(view);
        });

        return view;
    }

    private void forEachEditText(View view, ForEachEditTextCallback callback) {
        if (view instanceof EditText) {
            EditText foundEditText = (EditText) view;
            callback.call(foundEditText);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int childIdx = 0; childIdx < viewGroup.getChildCount(); ++childIdx) {
                View child = viewGroup.getChildAt(childIdx);
                this.forEachEditText(child, callback);
            }
        }
    }
    private interface ForEachEditTextCallback {
        public void call(EditText foundEditText);
    }

    private void checkToEnableSignIn(View view) {
        Button signIn = view.findViewById(R.id.signInButton);
        boolean signInEnabled = false;
        if (this.shouldSignInBeEnabled != null) {
            signInEnabled = this.shouldSignInBeEnabled.call(new LoginFormState(view));
        }
        signIn.setEnabled(signInEnabled);
    }

    private void checkToEnableRegister(View view) {
        Button register = view.findViewById(R.id.registerButton);
        boolean registerEnabled = false;
        if (this.shouldRegisterBeEnabled != null) {
            registerEnabled = this.shouldRegisterBeEnabled.call(new LoginFormState(view));
        }
        register.setEnabled(registerEnabled);
    }
}