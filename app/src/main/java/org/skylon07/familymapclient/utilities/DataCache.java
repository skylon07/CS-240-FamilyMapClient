package org.skylon07.familymapclient.utilities;

import org.skylon07.familymapclient.server.ServerProxy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import models.*;

import services.responses.EventResponse;
import services.responses.PersonResponse;

/**
 * Provides a basic interface for getting data, while also implementing caching optimizations
 * to allow data retrieval to operate faster
 */
public class DataCache {
    /** The singleton DataCache instance */
    private static DataCache instance = new DataCache();
    private static boolean TEST_MODE = false;

    static public DataCache getInstance() {
        return DataCache.instance;
    }
    static public DataCache getInstance(ServerProxy server) {
        DataCache.instance.useServer(server);
        return DataCache.instance;
    }

    static public void setTestMode() {
        TEST_MODE = true;
    }

    static public void setTestInstance(String currPersonID, Person[] allPersons, Event[] allEvents) {
        if (!TEST_MODE) {
            throw new RuntimeException("DataCache cannot set instances unless TEST_MODE is set with setTestMode()");
        }

        DataCache data = new DataCache();

        data.currUserPersonID = currPersonID;
        for (Person person : allPersons) {
            data.personsByID.put(person.getPersonID(), person);
        }
        data.allPersonsCalled = true;

        for (Event event : allEvents) {
            data.eventsByID.put(event.getEventID(), event);
        }
        data.allEventsCalled = true;

        DataCache.instance = data;
    }

    /** The currently active proxy to use */
    private ServerProxy server;
    /** The current user's login token */
    private AuthToken currAuthToken;
    /** The current user's username */
    private String currUserPersonID;
    /** A map containing Person objects by their Person ID */
    private Map<String, Person> personsByID;
    /** A map containing Event objects by their Event ID */
    private Map<String, Event> eventsByID;
    /** A value indicating if getAllPersons() has ever been invoked */
    private boolean allPersonsCalled;
    /** A value indicating if getAllEvents() has ever been invoked */
    private boolean allEventsCalled;

    /**
     * The private constructor for the DataCache, enforcing the singleton policy
     */
    private DataCache() {
        this.server = null;
        // initialize the cache variables
        this.invalidateCache();
    }

    /**
     * Sets the current server to use
     *
     * @param server is the ServerProxy to use in getting data (if ever needed)
     */
    public void useServer(ServerProxy server) {
        this.server = server;
    }

    /**
     * Records a successful login attempt for a user
     *
     * @param currUserPersonID is the user who just logged in
     * @param loginAuthToken is the login token received
     */
    public void recordLogin(String currUserPersonID, AuthToken loginAuthToken) {
        this.currUserPersonID = currUserPersonID;
        this.currAuthToken = loginAuthToken;
    }

    /**
     * Resets the cache. This should be used when the cache is no longer holding relevant
     * information, like when the User logs out.
     */
    public void invalidateCache() {
        this.currAuthToken = null;
        this.currUserPersonID = null;
        this.personsByID = new HashMap<>();
        this.eventsByID = new HashMap<>();
        this.allPersonsCalled = false;
        this.allEventsCalled = false;
    }

    public String getCurrUserPersonID() {
        return this.currUserPersonID;
    }

    public AuthToken getCurrAuthToken() {
        return this.currAuthToken;
    }

    /**
     * Gets the cached Person given their personID. This requires a login to be recorded first.
     *
     * @param personID is the person ID of the person to get
     * @return the corresponding Person
     * @throws MalformedURLException when the ServerProxy chokes up on connecting to the server
     * @throws IOException when the ServerProxy craps out and throws the connection into the trash
     */
    public Person getPersonByID(String personID) throws MalformedURLException, IOException {
        this.ensureActiveLogin();
        Person person = this.personsByID.get(personID);
        boolean shouldRetrieveFromServer = person == null;
        if (shouldRetrieveFromServer) {
            PersonResponse response = this.server.getPerson(personID, this.currAuthToken.getAuthtoken());
            if (response.success) {
                person = new Person(
                        response.personID, response.associatedUsername, response.firstName,
                        response.lastName, response.gender, response.fatherID, response.motherID,
                        response.spouseID
                );
                this.personsByID.put(personID, person);
            }
        }
        return person;
    }

    /**
     * Gets the cached Event given their eventID. This requires a login to be recorded first.
     *
     * @param eventID is the event ID of the event to get
     * @return the corresponding Event
     * @throws MalformedURLException when the ServerProxy chokes up on connecting to the server
     * @throws IOException when the ServerProxy craps out and throws the connection into the trash
     */
    public Event getEventByID(String eventID) throws MalformedURLException, IOException {
        this.ensureActiveLogin();
        Event event = this.eventsByID.get(eventID);
        boolean shouldRetrieveFromServer = event == null;
        if (shouldRetrieveFromServer) {
            EventResponse response = this.server.getEvent(eventID, this.currAuthToken.getAuthtoken());
            if (response.success) {
                event = new Event(
                        response.eventID, response.associatedUsername, response.personID,
                        response.latitude, response.longitude, response.country, response.city,
                        response.eventType, response.year
                );
                this.eventsByID.put(eventID, event);
            }
        }
        return event;
    }

    /**
     * Returns a list of all persons corresponding to the user
     *
     * @return a cached list of all persons associated with a user
     * @throws MalformedURLException when the ServerProxy chokes up on connecting to the server
     * @throws IOException when the ServerProxy craps out and throws the connection into the trash
     */
    public Person[] getAllPersons() throws MalformedURLException, IOException {
        this.ensureActiveLogin();
        if (!this.allPersonsCalled) {
            PersonResponse response = this.server.getAllPersons(this.currAuthToken.getAuthtoken());
            for (Person person : response.data) {
                this.personsByID.put(person.getPersonID(), person);
            }

            this.allPersonsCalled = true;
            return response.data;
        } else {
            Collection<Person> values = this.personsByID.values();
            return values.toArray(new Person[values.size()]);
        }
    }

    /**
     * Returns a list of all events corresponding to the user
     *
     * @return a cached list of all events associated with a user
     * @throws MalformedURLException when the ServerProxy chokes up on connecting to the server
     * @throws IOException when the ServerProxy craps out and throws the connection into the trash
     */
    public Event[] getAllEvents() throws MalformedURLException, IOException {
        this.ensureActiveLogin();
        if (!this.allEventsCalled) {
            EventResponse response = this.server.getAllEvents(this.currAuthToken.getAuthtoken());
            for (Event event : response.data) {
                this.eventsByID.put(event.getEventID(), event);
            }

            this.allEventsCalled = true;
            return response.data;
        } else {
            Collection<Event> values = this.eventsByID.values();
            return values.toArray(new Event[values.size()]);
        }
    }

    /**
     * Provides assurances for functions that require an active login to work
     */
    private void ensureActiveLogin() {
        if (TEST_MODE) {
            return;
        }

        assert this.server != null
                : "DataCache was never given a ServerProxy() to use; call useServer() to fix this";
        assert this.currAuthToken != null
                : "DataCache was never called with the login method; call recordLogin() to fix this";
    }
}
