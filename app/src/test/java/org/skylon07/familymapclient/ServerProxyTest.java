package org.skylon07.familymapclient;

import org.junit.*;
import org.skylon07.familymapclient.server.ServerProxy;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Random;

import services.responses.EventResponse;
import services.responses.LoginResponse;
import services.responses.PersonResponse;
import services.responses.RegisterResponse;

/**
 * Contains tests for the ServerProxy class. This ensures methods like logging in, getting
 * events/persons from the server, etc. work correctly
 */
public class ServerProxyTest {
    /** The hostname to use with the proxy */
    static public String HOSTNAME = "127.0.0.1";
    /** The port to use with the proxy */
    static public String PORT = "8080";

    /**
     * Returns a new proxy given the above data (hostname/port)
     */
    public ServerProxy createProxy() {
        ServerProxy proxy = new ServerProxy(HOSTNAME, PORT);
        // ensures a user is registered
        try {
            proxy.register(
                    "validUsername", "validPassword", "valid@email.net",
                    "validFirstName", "validLastName", "m"
            );
        } catch (IOException err) {
            // user already registered; don't need to do anything
        }
        return proxy;
    }

    /**
     * Ensures the login() function works correctly for valid inputs
     */
    @Test
    public void testValidLogin() {
        ServerProxy proxy = this.createProxy();
        try {
            LoginResponse response = proxy.login("validUsername", "validPassword");
            assertTrue(response.success);
            assertEquals("validUsername", response.username);
            assertNotNull(response.authtoken);
            assertNotNull(response.personID);
        } catch (IOException err) {
            fail("Login should not throw with valid data");
        }
    }

    /**
     * Ensures the login() function works correctly for invalid inputs
     */
    @Test
    public void testBadLogin() {
        ServerProxy proxy = this.createProxy();
        try {
            LoginResponse response = proxy.login("invalidFirstName", "invalidPassword");
            assertFalse(response.success);
            assertNotNull(response.message);
        } catch (IOException err) {
            fail("Login should not throw with invalid data");
        }
    }

    /**
     * Ensures register() successfully creates a new user
     */
    @Test
    public void testValidRegister() {
        // ideally there would be a proxy delete function,
        // that ensures a user is deleted, but I don't have time
        // to implement that right now... this is the next best thing
        Random rand = new Random();
        String username = "user";
        for (int i = 0; i < 3; ++i) {
            // on average, should have at least five digits;
            // repeated three times, this is plenty of randomness
            Integer randInt = rand.nextInt();
            username += randInt.toString();
        }
        String password = "password";
        String email = "email@email.email";
        String firstName = "firstname";
        String lastName = "lastname";
        String gender = "m";

        ServerProxy proxy = this.createProxy();
        try {
            RegisterResponse response = proxy.register(username, password, email, firstName, lastName, gender);
            assertTrue(response.success);
            assertEquals(username, response.username);
            assertNotNull(response.authtoken);
            assertNotNull(response.personID);
        } catch (IOException err) {
            fail("Register should not throw with valid data");
        }
    }

    /**
     * Ensures register() correctly fails to create duplicate users
     */
    @Test
    public void testBadRegister() {
        // ideally there would be a proxy delete function,
        // that ensures a user is deleted, but I don't have time
        // to implement that right now... this is the next best thing
        Random rand = new Random();
        String username = "user";
        for (int i = 0; i < 10; ++i) {
            Integer randInt = rand.nextInt();
            username += randInt.toString();
        }
        String password = "password";
        String email = "email@email.email";
        String firstName = "firstname";
        String lastName = "lastname";
        String gender = "m";

        ServerProxy proxy = this.createProxy();
        try {
            proxy.register(username, password, email, firstName, lastName, gender);
            RegisterResponse response = proxy.register(username, password, email, firstName, lastName, gender);
            assertFalse(response.success);
            assertNotNull(response.message);
        } catch (IOException err) {
            fail("Register should not throw with invalid/duplicate data");
        }
    }

    /**
     * Ensures proxy can get single people by ID
     * (user person, user's father person, etc)
     */
    @Test
    public void testGetSinglePerson() {
        ServerProxy proxy = this.createProxy();
        LoginResponse response = null;
        try {
            response = proxy.login("validUsername", "validPassword");
        } catch (IOException err) {
            fail("Could not get auth token; login failed");
        }

        try {
            PersonResponse response2 = proxy.getPerson(response.personID, response.authtoken);
            assertTrue(response2.success);
            assertEquals(response.personID, response2.personID);
            assertEquals(response.username, response2.associatedUsername);
            assertNotNull(response2.fatherID);
            assertNotNull(response2.motherID);
            assertNotNull(response2.firstName);
            assertNotNull(response2.lastName);
            assertNotNull(response2.gender);

            PersonResponse response3 = proxy.getPerson(response2.fatherID, response.authtoken);
            assertTrue(response3.success);
            assertEquals(response2.fatherID, response3.personID);
            assertEquals(response.username, response3.associatedUsername);
            assertNotNull(response3.fatherID);
            assertNotNull(response3.motherID);
            assertNotNull(response3.firstName);
            assertNotNull(response3.lastName);
            assertNotNull(response3.gender);
        } catch (IOException err) {
            fail("Getting person should not throw errors");
        }
    }

    /**
     * Ensures the proxy can provide a list of all people tied to a user
     */
    @Test
    public void testGetAllPersons() {
        ServerProxy proxy = this.createProxy();
        LoginResponse response = null;
        try {
            response = proxy.login("validUsername", "validPassword");
        } catch (IOException err) {
            fail("Could not get auth token; login failed");
        }

        try {
            PersonResponse response2 = proxy.getAllPersons(response.authtoken);
            assertNotNull(response2.data);
            // ensures at least the user and the parents were returned
            // (mostly just want to ensure SOMETHING was returned)
            assertTrue(response2.data.length >= 3);
        } catch (IOException err) {
            fail("Getting all persons should not throw errors");
        }
    }

    /**
     * Ensures the proxy can provide a list of all events tied to a user
     */
    @Test
    public void testGetAllEvents() {
        ServerProxy proxy = this.createProxy();
        LoginResponse response = null;
        try {
            response = proxy.login("validUsername", "validPassword");
        } catch (IOException err) {
            fail("Could not get auth token; login failed");
        }

        try {
            EventResponse response2 = proxy.getAllEvents(response.authtoken);
            assertNotNull(response2.data);
            // ensures that at elast both parents have a birth, death, and marriage event each
            assertTrue(response2.data.length >= 6);
        } catch (IOException err) {
            fail("Getting all persons should not throw errors");
        }
    }
}
