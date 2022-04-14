package org.skylon07.familymapclient.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.skylon07.familymapclient.R;

/**
 * Collection of data representing the state of the form. It is used to provide callbacks
 * with information they need about the state of the LoginFragment form.
 *
 * All getters for properties should return strings, as if the form consisted of only
 * text fields. This also involves converting null values to empty strings.
 */
public class LoginFormState {
    private View view;
    protected String serverHost;
    protected String serverPort;
    protected String userName;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String gender;

    public LoginFormState(View view) {
        this.view = view;
    }

    public String getServerHost() {
        EditText serverHost = this.view.findViewById(R.id.serverHost);
        return serverHost.getText().toString();
    }

    public String getServerPort() {
        EditText serverPort = this.view.findViewById(R.id.serverPort);
        return serverPort.getText().toString();
    }

    public String getUserName() {
        EditText userName = this.view.findViewById(R.id.userName);
        return userName.getText().toString();
    }

    public String getPassword() {
        EditText password = this.view.findViewById(R.id.password);
        return password.getText().toString();
    }

    public String getFirstName() {
        EditText firstName = this.view.findViewById(R.id.firstName);
        return firstName.getText().toString();
    }

    public String getLastName() {
        EditText lastName = this.view.findViewById(R.id.lastName);
        return lastName.getText().toString();
    }

    public String getEmail() {
        EditText email = this.view.findViewById(R.id.email);
        return email.getText().toString();
    }

    public String getGender() {
        RadioGroup genderGroup = view.findViewById(R.id.genderGroup);
        RadioButton genderButton = view.findViewById(genderGroup.getCheckedRadioButtonId());
        if (genderButton == null) {
            return "";
        }
        return genderButton.getText().toString();
    }
}
