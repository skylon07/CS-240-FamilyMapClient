package org.skylon07.familymapclient.server;

import services.requests.*;
import services.responses.*;

import org.skylon07.familymapclient.server.handlers.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides an interface for the FamilyMapServer endpoints.
 * Note that not all endpoints are needed by the client, so not all are implemented here
 */
public class ServerProxy {
    /** The host name to use on all API calls */
    private String hostname;
    /** The port to use on all API calls */
    private String port;

    /**
     * Creates a new ServerProxy instance, given a hostname/port to interact on
     *
     * @param hostname is the URL to the server to interact with
     * @param port is the port to use during the interactions
     */
    public ServerProxy(String hostname, String port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Logs a user in given their username and password
     *
     * @param username is the username to log in with
     * @param password is the password for the user
     * @return a LoginResponse, indicating success/failure and returning the auth token
     */
    public LoginResponse login(String username, String password) throws MalformedURLException, IOException {
        LoginRequest request = new LoginRequest();
        request.username = username;
        request.password = password;

        LoginHandler handler = new LoginHandler();
        return handler.handle(this.generateURL(), request);
    }

    /**
     * Registers a new user given a username, password, and other information
     *
     * @param username is the new username to register with
     * @param password is the password for the user
     * @param email is the user's email
     * @param firstName is the first name of the user
     * @param lastName is the last name of the user
     * @param gender is the gender of the user
     * @return a RegisterResponse, indicating success/failure, and other useful data
     */
    public RegisterResponse register(String username, String password, String email,
                                     String firstName, String lastName, String gender) throws MalformedURLException, IOException {
        RegisterRequest request = new RegisterRequest();
        request.username = username;
        request.password = password;
        request.email = email;
        request.firstName = firstName;
        request.lastName = lastName;
        request.gender = gender;

        RegisterHandler handler = new RegisterHandler();
        return handler.handle(this.generateURL(), request);
    }



    /**
     * Gets a person given their ID. This endpoint has an authorization
     * barrier, so it requires an auth token to work.
     *
     * @param personID is the person ID of the desired person to get
     * @param authToken is the login auth token given to the user
     * @return a PersonResponse, indicating success/failure and returning the desired person
     */
    public PersonResponse getPerson(String personID, String authToken) throws MalformedURLException, IOException {
        PersonRequest request = new PersonRequest();
        request.authtoken = authToken;
        request.personID = personID;

        PersonHandler handler = new PersonHandler();
        return handler.handle(this.generateURL(), request);
    }

    /**
     * Gets all persons for a user. This endpoint has an authorization barrier,
     * so it requires an auth token to work.
     *
     * @param authToken is the login auth token given to the user
     * @return a PersonResponse, indicating success/failure and returning the list of Persons
     */
    public PersonResponse getAllPersons(String authToken) throws MalformedURLException, IOException {
        PersonRequest request = new PersonRequest();
        request.authtoken = authToken;
        request.all = true;

        PersonHandler handler = new PersonHandler();
        return handler.handle(this.generateURL(), request);
    }

    /**
     * Gets an event given their ID. This endpoint has an authorization
     * barrier, so it requires an auth token to work.
     *
     * @param eventID is the event ID of the desired event to get
     * @param authToken is the login auth token given to the user
     * @return an EventRepsonse, indicating success/failure and returning the desired event
     */
    public EventResponse getEvent(String eventID, String authToken) throws MalformedURLException, IOException {
        EventRequest request = new EventRequest();
        request.authtoken = authToken;
        request.eventID = eventID;

        EventHandler handler = new EventHandler();
        return handler.handle(this.generateURL(), request);
    }

    /**
     * Gets all events for a user. This endpoint has an authorization barrier,
     * so it requires an auth token to work.
     *
     * @param authToken is the login auth token given to the user
     * @return an EventResponse, indicating success/failure and returning the list of Events
     */
    public EventResponse getAllEvents(String authToken) throws MalformedURLException, IOException {
        EventRequest request = new EventRequest();
        request.authtoken = authToken;
        request.all = true;

        EventHandler handler = new EventHandler();
        return handler.handle(this.generateURL(), request);
    }

    private String generateURL() {
        return "http://" + this.hostname + ":" + this.port;
    }
}
