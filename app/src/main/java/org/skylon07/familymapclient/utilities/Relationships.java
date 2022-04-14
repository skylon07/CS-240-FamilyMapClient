package org.skylon07.familymapclient.utilities;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import models.Person;

public class Relationships {
    /**
     * Returns all parent ancestors for a given person
     *
     * @param person is the person to get a list of parents for
     * @return a Set of parents
     * @throws IOException when the DataCache brokens
     */
    public static Set<Person> getAllParentsOf(Person person) throws IOException {
        if (person == null) {
            return null;
        }

        Set<Person> parents = new HashSet<>();
        Relationships.addAncestorsFor(person, parents);
        return parents;
    }

    /**
     * Takes a person and a Set of parents and adds each person's parents to the set recursively
     *
     * @param person is the person whose parents should be added to the set
     * @param parents is the Set of parents to add to
     * @throws IOException whenever DataCache brokens
     */
    private static void addAncestorsFor(Person person, Set<Person> parents) throws IOException {
        DataCache data = DataCache.getInstance();

        if (person.getFatherID() != null) {
            Person father = data.getPersonByID(person.getFatherID());
            parents.add(father);
            Relationships.addAncestorsFor(father, parents);
        }

        if (person.getMotherID() != null) {
            Person mother = data.getPersonByID(person.getMotherID());
            parents.add(mother);
            Relationships.addAncestorsFor(mother, parents);
        }
    }

    /**
     * Takes a person and a Set of parents and adds each person's parents to the set recursively.
     * This method will also check and only add family members of a certian gender.
     *
     * @param person is the person whose parents should be added to the set
     * @param parents is the Set of parents to add to
     * @param genderFilter is the gender to match for adding people to the set
     * @throws IOException whenever DataCache brokens
     */
    private static void addAncestorsFor(Person person, Set<Person> parents, String genderFilter) throws IOException {
        DataCache data = DataCache.getInstance();

        if (person.getFatherID() != null) {
            Person father = data.getPersonByID(person.getFatherID());
            if (genderFilter.equals("m")) {
                parents.add(father);
            }
            Relationships.addAncestorsFor(father, parents, genderFilter);
        }

        if (person.getMotherID() != null) {
            Person mother = data.getPersonByID(person.getMotherID());
            if (genderFilter.equals("f")) {
                parents.add(mother);
            }
            Relationships.addAncestorsFor(mother, parents, genderFilter);
        }
    }

    /**
     * Returns a Set of all male ancestors for a given person
     *
     * @param person is the person to return male parents for
     * @return a Set of male parents for the person
     * @throws IOException whenever DataCache brokens
     */
    public static Set<Person> getAllMaleAncestors(Person person) throws IOException {
        if (person == null) {
            return null;
        }

        Set<Person> ancestors = new HashSet<>();
        Relationships.addAncestorsFor(person, ancestors, "m");
        return ancestors;
    }

    /**
     * Returns a Set of all female ancestors for a given person
     *
     * @param person is the person to return female parents for
     * @return a Set of female parents for the person
     * @throws IOException whenever DataCache brokens
     */
    public static Set<Person> getAllFemaleAncestors(Person person) throws IOException {
        if (person == null) {
            return null;
        }

        Set<Person> ancestors = new HashSet<>();
        Relationships.addAncestorsFor(person, ancestors, "f");
        return ancestors;
    }

    /**
     * Maps a set of Persons to a Set of their IDs
     *
     * @param persons is the set of Persons to convert
     * @return a Set of Strings representing the Persons IDs
     */
    public static Set<String> mapToIDs(Set<Person> persons) {
        if (persons == null) {
            throw new NullPointerException("Persons set cannot be null");
        }

        Set<String> personIDs = new HashSet<>();
        for (Person person : persons) {
            personIDs.add(person.getPersonID());
        }
        return personIDs;
    }
}
