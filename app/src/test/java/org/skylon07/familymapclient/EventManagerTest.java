package org.skylon07.familymapclient;

import org.junit.*;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.EventManager;

import static org.junit.Assert.*;

import java.io.IOException;

import models.Event;
import models.Person;

public class EventManagerTest {
    /**
     * Creates a new EventManager with a given set of settings mocked into the manager
     *
     * @param preset is a binary mask indicating, in order, to use father, mother, male, and female events
     * @return the new EventManager
     */
    public EventManager createEventManager(int preset) {
        DataCache.setTestMode();
        DataCache.setTestInstance(
                TestData.persons[0].getPersonID(),
                TestData.persons,
                TestData.events
        );

        boolean fathersEvents =     (preset & 0b1000) > 0;
        boolean mothersEvents =     (preset & 0b0100) > 0;
        boolean maleEvents =        (preset & 0b0010) > 0;
        boolean femaleEvents =      (preset & 0b0001) > 0;
        EventManager.mockSharedPreferences(fathersEvents, mothersEvents, maleEvents, femaleEvents);
        return new EventManager();
    }

    @Test
    public void testFatherFilter() {
        EventManager manager = this.createEventManager(0b1011);
        Event[] events = manager.getEventsWithSettingsFilter(null);
        boolean rootWasIncluded = false;
        for (Event event : events) {
            String eventID = event.getEventID();
            if ("root".equals(eventID)) {
                rootWasIncluded = true;
                continue;
            }

            // we want anything EXCEPT mother-root; ie father-root and birth-root are okay
            String badSlice = "mother-root";
            int sliceStart = eventID.length() - badSlice.length();
            if (sliceStart < 0) {
                return;
            }
            int sliceEnd = eventID.length();
            assertNotEquals(badSlice, eventID.substring(sliceStart, sliceEnd));
        }
        assertTrue(rootWasIncluded);
    }

    @Test
    public void testMotherFilter() {
        EventManager manager = this.createEventManager(0b0111);
        Event[] events = manager.getEventsWithSettingsFilter(null);
        boolean rootWasIncluded = false;
        for (Event event : events) {
            String eventID = event.getEventID();
            if ("root".equals(eventID)) {
                rootWasIncluded = true;
                continue;
            }

            // we want anything EXCEPT mother-root; ie father-root and birth-root are okay
            String badSlice = "father-root";
            int sliceStart = eventID.length() - badSlice.length();
            if (sliceStart < 0) {
                return;
            }
            int sliceEnd = eventID.length();
            assertNotEquals(badSlice, eventID.substring(sliceStart, sliceEnd));
        }
        assertTrue(rootWasIncluded);
    }

    @Test
    public void testMaleFilter() throws IOException {
        EventManager manager = this.createEventManager(0b1110);
        Event[] events = manager.getEventsWithSettingsFilter(null);
        for (Event event : events) {
            // fun fact: can't throw IOException here
            Person person = DataCache.getInstance().getPersonByID(event.getPersonID());
            assertEquals("m", person.getGender());
        }
    }

    @Test
    public void testFemaleFilter() throws IOException {
        EventManager manager = this.createEventManager(0b1101);
        Event[] events = manager.getEventsWithSettingsFilter(null);
        for (Event event : events) {
            // fun fact: can't throw IOException here
            Person person = DataCache.getInstance().getPersonByID(event.getPersonID());
            assertEquals("f", person.getGender());
        }
    }

    @Test
    public void testSortedEvents() {
        EventManager manager = this.createEventManager(0);
        String[] personIDsToTest = {
                "father-root",
                "mother-father-root",
                "father-mother-mother-root",
                "father-father-father-root"
        };
        for (String personID : personIDsToTest) {
            Event[] events = manager.getSortedEventsForPerson(personID);
            assertEquals("Birth", events[0].getEventType());
            assertEquals("Marriage", events[1].getEventType());
            assertEquals("Death", events[2].getEventType());
            assertTrue(events[0].getYear() < events[1].getYear());
            assertTrue(events[1].getYear() < events[2].getYear());
        }

        assertNull(manager.getSortedEventsForPerson((String) null));
        assertNull(manager.getSortedEventsForPerson((Person) null));
    }

    @Test
    public void testEventColors() {
        EventManager manager = this.createEventManager(0);
        Event event1OfType1 = new Event(
                "type1-1",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type1",
                100
        );
        Event event2OfType1 = new Event(
                "type1-2",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type1",
                100
        );
        Event event3OfType1 = new Event(
                "type1-3",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type1",
                100
        );
        Event event1OfType2 = new Event(
                "type2-1",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type2",
                100
        );
        Event event2OfType2 = new Event(
                "type2-2",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type2",
                100
        );
        Event event1OfType3 = new Event(
                "type3-1",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type3",
                100
        );
        Event event2OfType3 = new Event(
                "type3-2",
                "rootUsername",
                "root",
                10,
                20,
                "USA",
                "New York",
                "type3",
                100
        );

        float typeColor1_first = manager.getEventColor(event1OfType1);
        float typeColor1_second = manager.getEventColor(event2OfType1);
        float typeColor1_third = manager.getEventColor(event3OfType1);
        float typeColor2_first = manager.getEventColor(event1OfType2);
        float typeColor2_second = manager.getEventColor(event2OfType2);
        float typeColor3_first = manager.getEventColor(event1OfType3);
        float typeColor3_second = manager.getEventColor(event2OfType3);
        double delta = 0.001;
        assertEquals(typeColor1_first, typeColor1_second, delta);
        assertEquals(typeColor1_first, typeColor1_third, delta);
        assertEquals(typeColor2_first, typeColor2_second, delta);
        assertEquals(typeColor3_first, typeColor3_second, delta);
        assertNotEquals(typeColor1_first, typeColor2_first, delta);
        assertNotEquals(typeColor2_first, typeColor3_first, delta);
        assertNotEquals(typeColor1_first, typeColor3_first, delta);

        assertEquals(0, manager.getEventColor(null), 0);
    }
}
