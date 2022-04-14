package org.skylon07.familymapclient.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.skylon07.familymapclient.R;
import org.skylon07.familymapclient.server.ServerProxy;
import org.skylon07.familymapclient.utilities.DataCache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.AuthToken;
import models.Event;
import models.Person;
import services.responses.LoginResponse;
import services.responses.RegisterResponse;

public class MainActivity extends AppCompatActivity {
    /** The executor to run background tasks with */
    private ExecutorService executor;
    /** A launcher for starting the settings activity */
    ActivityResultLauncher<Intent> settingsLauncher;
    /** A boolean value indicating if the executor is handling a login callback currently */
    private boolean loginThreadActive;
    /** A boolean indicating if a user is logged in or not */
    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.setTitle(R.string.mainActivityLabel);
        this.loggedIn = false;

        this.executor = Executors.newSingleThreadExecutor();
        this.loginThreadActive = false;

        // pass arguments to fragment
        FragmentManager manager = this.getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.mainFrameLayout);
        if (fragment == null) {
            this.renderLogin();
        }

        this.settingsLauncher = this.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                // lambda functions work here!
                (ActivityResult result) -> {
                    this.renderMap();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        boolean shouldLogout = result.getData().getBooleanExtra(
                                "shouldLogout",
                                false
                        );
                        if (shouldLogout) {
                            DataCache.getInstance().invalidateCache();
                            this.loggedIn = false;
                            this.renderLogin();
                        }
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return this.loggedIn;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem button) {
        if (button.getItemId() == R.id.searchActionButton) {
            this.startSearchActivity();
        } else if (button.getItemId() == R.id.settingsActionButton) {
            this.startSettingsActivity();
        }
        return true;
    }

    /**
     * Indicates that the Sign In button should be enabled when LoginFragment state is
     * filled in enough
     *
     * @return if the Sign In button should be enabled or not
     */
    private boolean shouldSignInBeEnabled(LoginFormState state) {
        boolean serverHostEmpty = state.getServerHost().length() == 0;
        boolean serverPortEmpty = state.getServerPort().length() == 0;
        boolean userNameEmpty = state.getUserName().length() == 0;
        boolean passwordEmpty = state.getPassword().length() == 0;

        return !serverHostEmpty && !serverPortEmpty
                && !userNameEmpty && !passwordEmpty;
    }

    /**
     * Indicates that the Register button should be enabled when LoginFragment state is
     * filled in enough
     *
     * @return if the Register button should be enabled or not
     */
    private boolean shouldRegisterBeEnabled(LoginFormState state) {
        boolean serverHostEmpty = state.getServerHost().length() == 0;
        boolean serverPortEmpty = state.getServerPort().length() == 0;
        boolean userNameEmpty = state.getUserName().length() == 0;
        boolean passwordEmpty = state.getPassword().length() == 0;
        boolean firstNameEmpty = state.getFirstName().length() == 0;
        boolean lastNameEmpty = state.getLastName().length() == 0;
        boolean emailEmpty = state.getEmail().length() == 0;
        boolean genderEmpty = state.getGender().length() == 0;

        return !serverHostEmpty && !serverPortEmpty && !userNameEmpty
                && !passwordEmpty && !firstNameEmpty && !lastNameEmpty
                && !emailEmpty && !genderEmpty;
    }

    /**
     * Performs the action of signing in the user. This method assumes a valid/filled out state.
     *
     * @param state is the filled out state to grab sign in data from
     */
    private void signInUser(LoginFormState state) {
        this.executeLoginThread(() -> {
            ServerProxy server = this.makeServerProxy(state);
            String username = state.getUserName();
            String password = state.getPassword();

            DataCache data = DataCache.getInstance(server);
            try {
                LoginResponse loginResponse = server.login(username, password);
                if (loginResponse.success) {
                    AuthToken authToken = new AuthToken(loginResponse.authtoken, loginResponse.username);
                    data.recordLogin(loginResponse.personID, authToken);
                    Person person = data.getPersonByID(loginResponse.personID);
                    if (person != null) {
                        this.displayGoodLogin(person.getFirstName(), person.getLastName());
                        this.loggedIn = true;
                        this.renderMap();
                    } else {
                        this.displayBadLogin("(person was null)");
                    }
                } else {
                    this.displayBadLogin();
                }
            } catch (IOException err) {
                this.displayBadLogin("(" + err.getMessage() + ")");
            }
            // redundant code... apparently
//            catch (MalformedURLException err) {
//                this.displayBadLogin("(" + err.getMessage() + ")");
//            }
        });
    }

    /**
     * Performs the action of registering the user. This method assumes a valid/filled out state.
     *
     * @param state is the filled out state to grab register data from
     */
    private void registerUser(LoginFormState state) {
        this.executeLoginThread(() -> {
            ServerProxy server = this.makeServerProxy(state);
            String username = state.getUserName();
            String password = state.getPassword();
            String email = state.getEmail();
            String firstName = state.getFirstName();
            String lastName = state.getLastName();
            String gender = state.getGender().toLowerCase().substring(0, 1);

            DataCache data = DataCache.getInstance(server);
            try {
                RegisterResponse registerResponse = server.register(
                        username, password, email,
                        firstName, lastName, gender
                );
                if (registerResponse.success) {
                    AuthToken authToken = new AuthToken(registerResponse.authtoken, registerResponse.username);
                    data.recordLogin(registerResponse.personID, authToken);
                    this.displayGoodRegister(firstName, lastName);
                    this.loggedIn = true;
                    this.renderMap();
                } else {
                    this.displayBadRegister();
                }
            } catch (MalformedURLException err) {
                this.displayBadRegister("(" + err.getMessage() + ")");
            } catch (IOException err) {
                this.displayBadRegister("(" + err.getMessage() + ")");
            }
        });
    }

    /**
     * Creates the server proxy from a LoginFragments LoginFormState
     *
     * @param state is the LoginFormState to grab data from
     * @return a newly created ServerProxy
     */
    private ServerProxy makeServerProxy(LoginFormState state) {
        String serverHost = state.getServerHost();
        String serverPort = state.getServerPort();
        return new ServerProxy(serverHost, serverPort);
    }

    /**
     * Ensures only one login/register attempt runs at a time. This function does nothing if
     * a previously scheduled login/register thread has not yet finished
     *
     * @param callback is the specific login/register callback the executor should execute
     */
    private void executeLoginThread(Runnable callback) {
        if (!this.loginThreadActive) {
            this.loginThreadActive = true;
            this.executor.submit(() -> {
                callback.run();
                this.loginThreadActive = false;
            });
        }
    }

    /**
     * Displays a toast indicating a login was successful
     *
     * @param firstName is the first name of the newly logged in user
     * @param lastName is the last name of the newly logged in user
     */
    private void displayGoodLogin(String firstName, String lastName) {
        this.runOnUiThread(() -> {
            Toast.makeText(
                    this,
                    String.format("Login successful for %s %s!", firstName, lastName),
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    /**
     * Displays a toast indicating a login failed
     */
    private void displayBadLogin() {
        this.runOnUiThread(() -> {
            Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Displays a toast indicating a login failed (with extra text)
     *
     * @param extra is the extra text to include
     */
    private void displayBadLogin(String extra) {
        this.runOnUiThread(() -> {
            Toast.makeText(this, "Login failed " + extra, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Displays a toast indicating a register attempt was successful
     *
     * @param firstName is the first name of the newly registered user
     * @param lastName is the last name of the newly registered user
     */
    private void displayGoodRegister(String firstName, String lastName) {
        this.runOnUiThread(() -> {
            Toast.makeText(
                    this,
                    String.format("Register successful for %s %s!", firstName, lastName),
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    /**
     * Displays a toast indicating a register attempt failed
     */
    private void displayBadRegister() {
        this.runOnUiThread(() -> {
            Toast.makeText(this, "Register failed", Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Displays a toast indicating a register attempt failed (with extra text)
     *
     * @param extra is the extra text to include
     */
    private void displayBadRegister(String extra) {
        this.runOnUiThread(() -> {
            Toast.makeText(this, "Register failed " + extra, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Switches the activity to the LoginFragment
     */
    private void renderLogin() {
        FragmentManager manager = this.getSupportFragmentManager();
        LoginFragment fragment = LoginFragment.createEmptyFragment();

        fragment.setWhenSignInShouldBeEnabled(this::shouldSignInBeEnabled);
        fragment.setWhenRegisterShouldBeEnabled(this::shouldRegisterBeEnabled);
        fragment.setSignInCallback(this::signInUser);
        fragment.setRegisterCallback(this::registerUser);

        manager.beginTransaction()
                .replace(R.id.mainFrameLayout, fragment)
                .commit();
        this.invalidateOptionsMenu();
    }

    /**
     * Switches the activity to the MapFragment
     */
    private void renderMap() {
        FragmentManager manager = this.getSupportFragmentManager();
        MapFragment fragment = MapFragment.createNewMap();
        fragment.setOnDetailsClickCallback((Event focusedEvent) -> {
            if (focusedEvent != null) {
                Intent intent = new Intent(this, PersonActivity.class);
                intent.putExtra("personID", focusedEvent.getPersonID());
                this.startActivity(intent);
            }
        });
        manager.beginTransaction()
                .replace(R.id.mainFrameLayout, fragment)
                .commit();
        this.invalidateOptionsMenu();
    }

    /**
     * Opens the Settings page
     */
    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.settingsLauncher.launch(intent);
    }

    /**
     * Performs a search and takes the user to the Search page
     */
    private void startSearchActivity() {
        this.startActivity(new Intent(this, SearchActivity.class));
    }
}