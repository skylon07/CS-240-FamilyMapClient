package org.skylon07.familymapclient.utilities;

import android.content.Context;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import models.Event;
import models.Person;

public class SearchManager {
    public Person[] searchPeople(String query) throws IOException {
        if (query == null) {
            return null;
        }

        String queryRegex = ".*" + query.toLowerCase() + ".*";
        DataCache data = DataCache.getInstance();

        PersonStringCallback[] dataCallbacksToSearch = {
                Person::getFirstName,
                Person::getLastName
        };

        Set<Person> filteredPeople = new HashSet<>();
        for (Person person : data.getAllPersons()) {
            boolean shouldAdd = false;
            for (PersonStringCallback dataGetter : dataCallbacksToSearch) {
                String personData = dataGetter.call(person);
                if (personData.toLowerCase().matches(queryRegex)) {
                    shouldAdd = true;
                    break;
                }
            }
            if (shouldAdd) {
                filteredPeople.add(person);
            }
        }
        return filteredPeople.toArray(new Person[filteredPeople.size()]);
    }

    public Event[] searchEvents(String query, Context activity) throws IOException {
        if (query == null) {
            return null;
        }

        String queryRegex = ".*" + query.toLowerCase() + ".*";
        EventManager data = new EventManager();

        EventStringCallback[] dataCallbacksToSearch = {
                Event::getEventType,
                Event::getCountry,
                Event::getCity,
                (Event event) -> {
                    return Integer.toString(event.getYear());
                }
        };

        Set<Event> filteredEvents = new HashSet<>();
        for (Event event : data.getEventsWithSettingsFilter(activity)) {
            boolean shouldAdd = false;
            for (EventStringCallback dataGetter : dataCallbacksToSearch) {
                String eventData = dataGetter.call(event);
                if (eventData.toLowerCase().matches(queryRegex)) {
                    shouldAdd = true;
                    break;
                }
            }
            if (shouldAdd) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents.toArray(new Event[filteredEvents.size()]);
    }

    private interface PersonStringCallback {
        public String call(Person person);
    }

    private interface EventStringCallback {
        public String call(Event event);
    }
}
