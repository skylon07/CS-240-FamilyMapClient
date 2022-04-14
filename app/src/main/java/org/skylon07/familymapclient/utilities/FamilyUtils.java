package org.skylon07.familymapclient.utilities;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import models.Person;

public class FamilyUtils {
    public Person getMotherOf(Person person) throws IOException {
        if (person == null) {
            return null;
        }

        String motherID = person.getMotherID();
        if (motherID == null) {
            return null;
        }
        return DataCache.getInstance().getPersonByID(motherID);
    }

    public Person getFatherOf(Person person) throws IOException {
        if (person == null) {
            return null;
        }

        String fatherID = person.getFatherID();
        if (fatherID == null) {
            return null;
        }
        return DataCache.getInstance().getPersonByID(fatherID);
    }

    public Person getSpouseOf(Person person) throws IOException {
        if (person == null) {
            return null;
        }

        String spouseID = person.getSpouseID();
        if (spouseID == null) {
            return null;
        }
        return DataCache.getInstance().getPersonByID(spouseID);
    }

    public Person[] getChildrenOf(Person parent) throws IOException {
        if (parent == null) {
            return null;
        }

        Set<Person> children = new HashSet<>();
        String parentID = parent.getPersonID();
        for (Person person : DataCache.getInstance().getAllPersons()) {
            if (parentID.equals(person.getMotherID()) || parentID.equals(person.getFatherID())) {
                children.add(person);
            }
        }
        return children.toArray(new Person[children.size()]);
    }
}
