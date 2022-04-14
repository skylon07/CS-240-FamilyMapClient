package org.skylon07.familymapclient.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import models.Event;
import models.Person;

/**
 * Need to get a list of events? How about information about the events?
 * The EventManager has you covered!
 */
public class EventManager {
    /** a map containing event colors given their event types */
    private static HashMap<String, Float> eventColors = new HashMap<>();
    private static SharedPreferences mockedPrefs;

    public static void mockSharedPreferences(boolean fathersEvents, boolean mothersEvents, boolean maleEvents, boolean femaleEvents) {
        // ideally I'd abstract out the preferences from this class entirely to make it testable
        // (by adding some kind of intermediate "EventFilter" class or something, then making a
        // utility/factory class that creates them from preferences), but alas, time is running
        // short and I don't have time to refactor this (and it works now; I'd only need to do
        // that abstraction for testing purposes, so it's a shortcut that needs to be taken)
        EventManager.mockedPrefs = new SharedPreferences() {
            @Override
            public Map<String, ?> getAll() { return null; }

            @Nullable
            @Override
            public String getString(String s, @Nullable String s1) { return null; }

            @Nullable
            @Override
            public Set<String> getStringSet(String s, @Nullable Set<String> set) { return null; }

            @Override
            public int getInt(String s, int i) { return 0; }

            @Override
            public long getLong(String s, long l) { return 0; }

            @Override
            public float getFloat(String s, float v) { return 0; }

            @Override
            public boolean getBoolean(String s, boolean b) {
                if ("fathersEvents".equals(s)) {
                    return fathersEvents;
                } else if ("mothersEvents".equals(s)) {
                    return mothersEvents;
                } else if ("maleEvents".equals(s)) {
                    return maleEvents;
                } else if ("femaleEvents".equals(s)) {
                    return femaleEvents;
                } else {
                    throw new RuntimeException("Boolean key was not caught; either it isn't accounted for or there's a typo");
                }
            }

            @Override
            public boolean contains(String s) { return false; }

            @Override
            public Editor edit() { return null; }

            @Override
            public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {}

            @Override
            public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {}
        };
    }

    /**
     * Returns a new EventManager, while also making sure some predefined colors are defined
     */
    public EventManager() {
        // I already implemented EventManager and didn't want
        // to make it a singleton so don't judge me
        EventManager.eventColors.put("birth", BitmapDescriptorFactory.HUE_BLUE);
        EventManager.eventColors.put("marriage", BitmapDescriptorFactory.HUE_GREEN);
        EventManager.eventColors.put("death", BitmapDescriptorFactory.HUE_MAGENTA);
    }

    /**
     * This function is used by the map to obtain only the events the user wishes to display
     *
     * @return
     */
    public Event[] getEventsWithSettingsFilter(Context context) {
        SharedPreferences prefs = EventManager.mockedPrefs;
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        try {
            Event[] allEvents = DataCache.getInstance().getAllEvents();
            HashSet<Event> eventSet = new HashSet<>(Arrays.asList(allEvents));

            this.filterFatherEvents(prefs, eventSet);
            this.filterMotherEvents(prefs, eventSet);
            this.filterMaleEvents(prefs, eventSet);
            this.filterFemaleEvents(prefs, eventSet);

            return eventSet.toArray(new Event[eventSet.size()]);
        } catch (IOException err) {
            throw new RuntimeException("Server couldn't get events");
        }
    }

    /**
     * Removes events that are on the father's side of the userd (according to preferences screen)
     *
     * @param prefs is the SharedPreferences from the preferences screen
     * @param eventSet is the set of all Events to filter from
     * @throws IOException when the server just doesn't feel good
     */
    private void filterFatherEvents(SharedPreferences prefs, Set<Event> eventSet) throws IOException {
        boolean showFatherEvents = prefs.getBoolean("fathersEvents", false);
        if (!showFatherEvents) {
            DataCache data = DataCache.getInstance();

            Person userPerson = data.getPersonByID(data.getCurrUserPersonID());
            Person fatherPerson = data.getPersonByID(userPerson.getFatherID());
            Set<Person> personsOnFathersSide = Relationships.getAllParentsOf(fatherPerson);
            personsOnFathersSide.add(fatherPerson);
            Set<String> personIDsOnFathersSide = Relationships.mapToIDs(personsOnFathersSide);

            ArrayList<Event> eventsToRemove = new ArrayList<>();
            for (Event event : eventSet) {
                if (personIDsOnFathersSide.contains(event.getPersonID())) {
                    eventsToRemove.add(event);
                }
            }
            for (Event eventToRemove : eventsToRemove) {
                eventSet.remove(eventToRemove);
            }
        }
    }

    /**
     * Removes events that are on the mother's side of the user (according to preferences screen)
     *
     * @param prefs is the SharedPreferences from the preferences screen
     * @param eventSet is the set of all Events to filter from
     * @throws IOException when the server just doesn't feel good
     */
    private void filterMotherEvents(SharedPreferences prefs, Set<Event> eventSet) throws IOException {
        boolean showMotherEvents = prefs.getBoolean("mothersEvents", false);
        if (!showMotherEvents) {
            DataCache data = DataCache.getInstance();
            Person userPerson = data.getPersonByID(data.getCurrUserPersonID());
            Person motherPerson = data.getPersonByID(userPerson.getMotherID());
            Set<Person> personsOnMothersSide = Relationships.getAllParentsOf(motherPerson);
            personsOnMothersSide.add(motherPerson);
            Set<String> personIDsOnMothersSide = Relationships.mapToIDs(personsOnMothersSide);

            ArrayList<Event> eventsToRemove = new ArrayList<>();
            for (Event event : eventSet) {
                if (personIDsOnMothersSide.contains(event.getPersonID())) {
                    eventsToRemove.add(event);
                }
            }
            for (Event eventToRemove : eventsToRemove) {
                eventSet.remove(eventToRemove);
            }
        }
    }

    /**
     * Removes events that are associated with males (according to preferences screen)
     *
     * @param prefs is the SharedPreferences from the preferences screen
     * @param eventSet is the set of all Events to filter from
     * @throws IOException when the server just doesn't feel good
     */
    private void filterMaleEvents(SharedPreferences prefs, Set<Event> eventSet) throws IOException {
        boolean showMaleEvents = prefs.getBoolean("maleEvents", false);
        if (!showMaleEvents) {
            DataCache data = DataCache.getInstance();
            Person userPerson = data.getPersonByID(data.getCurrUserPersonID());
            Set<Person> allMalePersons = Relationships.getAllMaleAncestors(userPerson);
            if ("m".equals(userPerson.getGender())) {
                allMalePersons.add(userPerson);
            }
            Set<String> allMalePersonIDs = Relationships.mapToIDs(allMalePersons);

            ArrayList<Event> eventsToRemove = new ArrayList<>();
            for (Event event : eventSet) {
                if (allMalePersonIDs.contains(event.getPersonID())) {
                    eventsToRemove.add(event);
                }
            }
            for (Event eventToRemove : eventsToRemove) {
                eventSet.remove(eventToRemove);
            }
        }
    }

    /**
     * Removes events that are associated with females (according to preferences screen)
     *
     * @param prefs is the SharedPreferences from the preferences screen
     * @param eventSet is the set of all Events to filter from
     * @throws IOException when the server just doesn't feel good
     */
    private void filterFemaleEvents(SharedPreferences prefs, Set<Event> eventSet) throws IOException {
        boolean showFemaleEvents = prefs.getBoolean("femaleEvents", false);
        if (!showFemaleEvents) {
            DataCache data = DataCache.getInstance();
            Person userPerson = data.getPersonByID(data.getCurrUserPersonID());
            Set<Person> allFemalePersons = Relationships.getAllFemaleAncestors(userPerson);
            if ("f".equals(userPerson.getGender())) {
                allFemalePersons.add(userPerson);
            }
            Set<String> allFemalePersonIDs = Relationships.mapToIDs(allFemalePersons);

            ArrayList<Event> eventsToRemove = new ArrayList<>();
            for (Event event : eventSet) {
                if (allFemalePersonIDs.contains(event.getPersonID())) {
                    eventsToRemove.add(event);
                }
            }
            for (Event eventToRemove : eventsToRemove) {
                eventSet.remove(eventToRemove);
            }
        }
    }

    /**
     * Returns all events (sorted!) associated with a certain person
     *
     * @param personID is the ID of the person to match events for
     * @return a list of events belonging to that person (sorted, too!)
     */
    public Event[] getSortedEventsForPerson(String personID) {
        if (personID == null) {
            return null;
        }

        ArrayList<Event> eventsForPerson = new ArrayList<>();
        try {
            for (Event event : DataCache.getInstance().getAllEvents()) {
                if (event.getPersonID().equals(personID)) {
                    eventsForPerson.add(event);
                }
            }
        } catch (IOException err) {
            throw new RuntimeException("Server couldn't get events");
        }

        Event[] eventsArr = eventsForPerson.toArray(new Event[eventsForPerson.size()]);
        // sort first by event type... (secondary)
        Arrays.sort(eventsArr, (Event event1, Event event2) -> {
            String type1 = event1.getEventType().toLowerCase();
            String type2 = event2.getEventType().toLowerCase();
            // special case: birth/death events
            if (type1.equals("birth") || type2.equals("death")) {
                return -1;
            } else if (type1.equals("death") || type2.equals("birth")) {
                return 1;
            }
            for (int idx = 0; idx <= type1.length(); ++idx) {
                if (idx == type1.length() && idx < type2.length()) {
                    return -1;
                } else if (idx == type2.length() && idx < type1.length()) {
                    return 1;
                } else if (idx == type1.length() && idx == type2.length()) {
                    return 0;
                } else if (type1.charAt(idx) < type2.charAt(idx)) {
                    return -1;
                } else if (type1.charAt(idx) > type2.charAt(idx)) {
                    return 1;
                } else if (type1.charAt(idx) == type2.charAt(idx)) {
                    continue;
                }
            }
            return 0;
        });
        // then sort by year (primary; same year should still be sorted by event type)
        Arrays.sort(eventsArr, (Event event1, Event event2) -> event1.getYear() - event2.getYear());
        return eventsArr;
    }

    public Event[] getSortedEventsForPerson(Person person) {
        if (person == null) {
            return null;
        }

        return this.getSortedEventsForPerson(person.getPersonID());
    }

    /**
     * Returns the color a given event should be. This function guarantees the same color is
     * returned for all events of the same event type. If a new event type is encountered,
     * a new color will be assigned and returned for that type.
     *
     * @param event is the event to get a color for
     * @return a float, reperesenting the color to use for the event
     */
    public float getEventColor(Event event) {
        if (event == null) {
            return 0;
        }

        String type = event.getEventType().toLowerCase();
        if (EventManager.eventColors.containsKey(type)) {
            return EventManager.eventColors.get(type);
        } else {
            float newColor = this.generateNewEventColor();
            EventManager.eventColors.put(type, newColor);
            return newColor;
        }
    }

    /**
     * Helper function that returns the next valid event color
     *
     * @return a float representing a new event color which can be used
     */
    private float generateNewEventColor() {
        // TODO: put as a static value
        Float[] allColorsArr = {
                BitmapDescriptorFactory.HUE_RED,
                BitmapDescriptorFactory.HUE_ORANGE,
                BitmapDescriptorFactory.HUE_YELLOW,
                BitmapDescriptorFactory.HUE_GREEN,
                BitmapDescriptorFactory.HUE_CYAN,
                BitmapDescriptorFactory.HUE_AZURE,
                BitmapDescriptorFactory.HUE_VIOLET,
                BitmapDescriptorFactory.HUE_MAGENTA,
                BitmapDescriptorFactory.HUE_ROSE
        };
        // TODO: can probably save this state instead of generating it every time...
        HashSet<Float> allColorsSet = new HashSet<>(Arrays.asList(allColorsArr));
        for (Float color : EventManager.eventColors.values()) {
            allColorsSet.remove(color);
        }

        if (allColorsSet.isEmpty()) {
            // return any random color (don't know why this is the specification... but whatever)
            int randIdx = new Random().nextInt(allColorsArr.length);
            return allColorsArr[randIdx];
        } else {
            // return a random unused color
            ArrayList<Float> allColorsList = new ArrayList<>(allColorsSet);
            int randIdx = new Random().nextInt(allColorsList.size());
            return allColorsList.get(randIdx);
        }
    }
}
