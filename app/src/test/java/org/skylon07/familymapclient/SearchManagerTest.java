package org.skylon07.familymapclient;

import org.junit.*;
import org.skylon07.familymapclient.server.ServerProxy;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.EventManager;
import org.skylon07.familymapclient.utilities.SearchManager;

import static org.junit.Assert.*;

import android.content.Context;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import models.Event;
import models.Person;

public class SearchManagerTest {
    public SearchManager createSearchManager() {
        return this.createSearchManager(true, true, true, true);
    }

    public SearchManager createSearchManager(boolean searchFatherEvents, boolean searchMotherEvents, boolean searchMaleEvents, boolean searchFemaleEvents) {
        DataCache.setTestMode();
        DataCache.setTestInstance(
                TestData.persons[0].getPersonID(),
                TestData.persons,
                TestData.events
        );
        EventManager.mockSharedPreferences(searchFatherEvents, searchMotherEvents, searchMaleEvents, searchFemaleEvents);
        return new SearchManager();
    }

    // even though I only had time to write things as two functions, there are multiple
    // test cases per function, including negative ones

    @Test
    public void testSearchingPeople() throws IOException {
        SearchManager searcher = this.createSearchManager();

        Person[] allPeople = DataCache.getInstance().getAllPersons();
        Person[] allParents = searcher.searchPeople("OfRoot");
        assertEquals(allPeople.length - 1, allParents.length);
        Person[] allParentsBuTwEiRd = searcher.searchPeople("oFrOoT");
        assertArrayEquals(allParents, allParentsBuTwEiRd);

        Person[] grammyArr = {DataCache.getInstance().getPersonByID("mother-mother-mother-mother-root")};
        Person[] onlyOneGrammy = searcher.searchPeople("MotherOfMotherOfMotherOfMotherOfRoot");
        assertArrayEquals(grammyArr, onlyOneGrammy);

        Person[] validGrampysArr = {
                DataCache.getInstance().getPersonByID("father-father-father-root"),
                DataCache.getInstance().getPersonByID("father-father-father-father-root"),
                DataCache.getInstance().getPersonByID("mother-father-father-father-root")
        };
        Set<Person> validGrampys = new HashSet<>(Arrays.asList(validGrampysArr));
        Person[] aFewGrampys = searcher.searchPeople("FatherOfFatherOfFatherOfRoot");
        assertEquals(3, aFewGrampys.length);
        for (Person grampy : aFewGrampys) {
            assertTrue(validGrampys.contains(grampy));
        }

        Person[] noPeople = searcher.searchPeople("NOBODY WILL MATCH THIS");
        assertEquals(0, noPeople.length);
        assertNull(searcher.searchPeople(null));
    }

    @Test
    public void testSearchingEvents() throws IOException {
        SearchManager searcher = this.createSearchManager();
        // required parameter for EventManager, however we are mocking preferences so
        // the activity context will never be used
        Context nullActivity = null;

        Event[] allOfThemBecauseUSA = searcher.searchEvents("a", nullActivity);
        Event[] allOfThemBecauseUSAUpper = searcher.searchEvents("A", nullActivity);
        assertArrayEquals(allOfThemBecauseUSA, allOfThemBecauseUSAUpper);
        Set<Event> allOfThemButAsSet = new HashSet<>(Arrays.asList(allOfThemBecauseUSA));
        for (Event event : DataCache.getInstance().getAllEvents()) {
            assertTrue(allOfThemButAsSet.contains(event));
        }

        Event[] birthOnly = searcher.searchEvents("irt", nullActivity);
        Set<Event> birthsAsSet = new HashSet<>(Arrays.asList(birthOnly));
        for (Event event : DataCache.getInstance().getAllEvents()) {
            if ("Birth".equals(event.getEventType())) {
                assertTrue(birthsAsSet.contains(event));
            } else {
                assertFalse(birthsAsSet.contains(event));
            }
        }

        searcher = this.createSearchManager(true, true, true, false);
        Event[] maleDeathOnly = searcher.searchEvents("ath", nullActivity);
        Set<Event> maleDeathsAsSet = new HashSet<>(Arrays.asList(maleDeathOnly));
        for (Event event : DataCache.getInstance().getAllEvents()) {
            Person person = DataCache.getInstance().getPersonByID(event.getPersonID());
            if ("m".equals(person.getGender()) && "Death".equals(event.getEventType())) {
                assertTrue(maleDeathsAsSet.contains(event));
            } else {
                assertFalse(maleDeathsAsSet.contains(event));
            }
        }

        searcher = this.createSearchManager(false, false, false, false);
        Event[] noEventsBecauseFilter = searcher.searchEvents("USA", nullActivity);
        assertEquals(0, noEventsBecauseFilter.length);

        assertNull(searcher.searchEvents(null, nullActivity));
    }
}
