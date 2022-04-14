package org.skylon07.familymapclient;

import org.junit.*;
import org.skylon07.familymapclient.server.ServerProxy;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.Relationships;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import models.Person;

public class RelationshipsTest {
    static int NUM_GENERATIONS = 5;

    @Before
    public void setupDataCache() {
        DataCache.setTestMode();
        DataCache.setTestInstance(
                TestData.persons[0].getPersonID(),
                TestData.persons,
                TestData.events
        );
    }

    // since I was running out of time, I just made one test function per method.
    // HOWEVER each of these tests has multiple test cases within them

    @Test
    public void testGetAllParents() throws IOException {
        Person grammy = DataCache.getInstance().getPersonByID("mother-mother-mother-root");
        Person grammyMom = DataCache.getInstance().getPersonByID("mother-mother-mother-mother-root");
        Person grammyDad = DataCache.getInstance().getPersonByID("father-mother-mother-mother-root");
        Set<Person> grammyParents = Relationships.getAllParentsOf(grammy);
        assertEquals(2, grammyParents.size());
        assertTrue(grammyParents.contains(grammyMom));
        assertTrue(grammyParents.contains(grammyDad));

        Person root = DataCache.getInstance().getPersonByID("root");
        Set<Person> allParents = Relationships.getAllParentsOf(root);
        assertEquals(2 + 4 + 8 + 16, allParents.size());
        for (Person person : DataCache.getInstance().getAllPersons()) {
            if ("root".equals(person.getPersonID())) {
                assertFalse(allParents.contains(person));
            } else {
                assertTrue(allParents.contains(person));
            }
        }

        Person doesNotExist = new Person(
                "someIdThatDoesNotExist",
                "someUser",
                "firstname",
                "lastname",
                "f",
                null,
                null,
                null
        );
        Set<Person> noPeople = Relationships.getAllParentsOf(doesNotExist);
        assertTrue(noPeople.isEmpty());

        assertNull(Relationships.getAllParentsOf(null));
    }

    @Test
    public void testGettingMaleAncestors() throws IOException {
        Person grammy = DataCache.getInstance().getPersonByID("mother-father-mother-root");
        Person grammyMom = DataCache.getInstance().getPersonByID("mother-mother-father-mother-root");
        Person grammyDad = DataCache.getInstance().getPersonByID("father-mother-father-mother-root");
        Set<Person> grammyMaleParents = Relationships.getAllMaleAncestors(grammy);
        assertEquals(1, grammyMaleParents.size());
        assertFalse(grammyMaleParents.contains(grammyMom));
        assertTrue(grammyMaleParents.contains(grammyDad));

        Person root = DataCache.getInstance().getPersonByID("root");
        Set<Person> allMaleParents = Relationships.getAllMaleAncestors(root);
        assertEquals((2 + 4 + 8 + 16) / 2, allMaleParents.size());
        for (Person person : DataCache.getInstance().getAllPersons()) {
            if ("root".equals(person.getPersonID())) {
                assertFalse(allMaleParents.contains(person));
            } else if ("m".equals(person.getGender())) {
                assertTrue(allMaleParents.contains(person));
            } else {
                assertFalse(allMaleParents.contains(person));
            }
        }

        Person doesNotExist = new Person(
                "someIdThatDoesNotExist",
                "someUser",
                "firstname",
                "lastname",
                "f",
                null,
                null,
                null
        );
        Set<Person> noPeople = Relationships.getAllMaleAncestors(doesNotExist);
        assertTrue(noPeople.isEmpty());

        assertNull(Relationships.getAllMaleAncestors(null));
    }

    @Test
    public void testGettingFemaleAncestors() throws IOException {
        Person grammy = DataCache.getInstance().getPersonByID("mother-mother-father-root");
        Person grammyMom = DataCache.getInstance().getPersonByID("mother-mother-mother-father-root");
        Person grammyDad = DataCache.getInstance().getPersonByID("father-mother-mother-father-root");
        Set<Person> grammyFemaleParents = Relationships.getAllFemaleAncestors(grammy);
        assertEquals(1, grammyFemaleParents.size());
        assertTrue(grammyFemaleParents.contains(grammyMom));
        assertFalse(grammyFemaleParents.contains(grammyDad));

        Person root = DataCache.getInstance().getPersonByID("root");
        Set<Person> allFemaleParents = Relationships.getAllFemaleAncestors(root);
        assertEquals((2 + 4 + 8 + 16) / 2, allFemaleParents.size());
        for (Person person : DataCache.getInstance().getAllPersons()) {
            if ("root".equals(person.getPersonID())) {
                assertFalse(allFemaleParents.contains(person));
            } else if ("f".equals(person.getGender())) {
                assertTrue(allFemaleParents.contains(person));
            } else {
                assertFalse(allFemaleParents.contains(person));
            }
        }

        Person doesNotExist = new Person(
                "someIdThatDoesNotExist",
                "someUser",
                "firstname",
                "lastname",
                "f",
                null,
                null,
                null
        );
        Set<Person> noPeople = Relationships.getAllFemaleAncestors(doesNotExist);
        assertTrue(noPeople.isEmpty());

        assertNull(Relationships.getAllFemaleAncestors(null));
    }

    @Test
    public void testMapToIDs() throws IOException {
        Person[] allPersonsArr = DataCache.getInstance().getAllPersons();
        Set<Person> allPersons = new HashSet<>(Arrays.asList(allPersonsArr));
        Set<String> allPersonIDs = Relationships.mapToIDs(allPersons);
        for (Person person : allPersons) {
            assertTrue(allPersonIDs.contains(person.getPersonID()));
        }

        Person[] threePeopleArr = {
                DataCache.getInstance().getPersonByID("mother-root"),
                DataCache.getInstance().getPersonByID("mother-father-father-root"),
                DataCache.getInstance().getPersonByID("father-mother-root"),
        };
        Set<Person> threePeople = new HashSet<>(Arrays.asList(threePeopleArr));
        Set<String> threePeopleIDs = Relationships.mapToIDs(threePeople);
        for (Person person : allPersons) {
            if (threePeople.contains(person)) {
                assertTrue(threePeopleIDs.contains(person.getPersonID()));
            } else {
                assertFalse(threePeopleIDs.contains(person.getPersonID()));
            }
        }

        Set<Person> noPeople = new HashSet<>();
        Set<String> noPeopleIDs = Relationships.mapToIDs(noPeople);
        assertTrue(noPeopleIDs.isEmpty());

        assertThrows(NullPointerException.class, () -> {
            Relationships.mapToIDs(null);
        });
    }
}
