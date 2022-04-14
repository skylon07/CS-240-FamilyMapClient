package org.skylon07.familymapclient;

import org.junit.*;
import org.skylon07.familymapclient.server.ServerProxy;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.FamilyUtils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import models.Event;
import models.Person;

public class FamilyUtilsTest {
    public FamilyUtils createFamilyUtils() {
        DataCache.setTestMode();
        DataCache.setTestInstance(
                TestData.persons[0].getPersonID(),
                TestData.persons,
                TestData.events
        );

        FamilyUtils familyUtils = new FamilyUtils();
        return familyUtils;
    }

    @Test
    public void testGettingParents() throws IOException {
        // this test mashes up a few cases all at once, but they kind of rely on each other...
        FamilyUtils utils = this.createFamilyUtils();
        Person root = TestData.persons[0];
        assertEquals("root", root.getPersonID());

        Person mother = utils.getMotherOf(root);
        assertEquals("mother-root", mother.getPersonID());

        Person father = utils.getFatherOf(root);
        assertEquals("father-root", father.getPersonID());

        Person grandmother1 = utils.getMotherOf(mother);
        assertEquals("mother-mother-root", grandmother1.getPersonID());

        Person grandfather1 = utils.getFatherOf(mother);
        assertEquals("father-mother-root", grandfather1.getPersonID());

        Person grandmother2 = utils.getMotherOf(father);
        assertEquals("mother-father-root", grandmother2.getPersonID());

        Person grandfather2 = utils.getFatherOf(father);
        assertEquals("father-father-root", grandfather2.getPersonID());

        // a bit of a proof-by-contradiction case... but it proves that attempting to get a
        // mother of someone who doesn't have it recorded does return null!
        Person lastMother = mother;
        int iters = 0;
        while (lastMother != null) {
            lastMother = utils.getMotherOf(lastMother);
            iters += 1;
            if (iters == 99999999) {
                fail("Mother loop took too many times; recursion error?");
            }
        }
        Person lastFather = father;
        iters = 0;
        while (lastFather != null) {
            lastFather = utils.getMotherOf(lastFather);
            iters += 1;
            if (iters == 99999999) {
                fail("Father loop took too many times; recursion error?");
            }
        }

        assertNull(utils.getFatherOf(null));
        assertNull(utils.getMotherOf(null));
    }

    @Test
    public void testGettingSpouse() throws IOException {
        Person root = TestData.persons[0];
        assertEquals("root", root.getPersonID());
        this.assertParentsAreSpouses(root);

        assertNull(this.createFamilyUtils().getSpouseOf(null));
    }
    private void assertParentsAreSpouses(Person person) throws IOException {
        FamilyUtils utils = this.createFamilyUtils();
        Person father = utils.getFatherOf(person);
        Person mother = utils.getMotherOf(person);
        if (father != null && mother != null) {
            Person fatherSpouse = utils.getSpouseOf(father);
            assertEquals(mother, fatherSpouse);
            Person motherSpouse = utils.getSpouseOf(mother);
            assertEquals(father, motherSpouse);

            this.assertParentsAreSpouses(father);
            this.assertParentsAreSpouses(mother);
        }
    }

    @Test
    public void testGettingChildren() throws IOException {
        FamilyUtils utils = this.createFamilyUtils();
        Person root = TestData.persons[0];
        assertEquals("root", root.getPersonID());

        Person mother = utils.getMotherOf(root);
        Person father = utils.getFatherOf(root);
        Person[] motherChildren = utils.getChildrenOf(mother);
        Person[] fatherChildren = utils.getChildrenOf(father);
        assertArrayEquals(motherChildren, fatherChildren);
        assertEquals(1, fatherChildren.length);
        assertEquals(root, fatherChildren[0]);

        Person someone = utils.getFatherOf(utils.getMotherOf(root));
        Person grandma = utils.getMotherOf(someone);
        Person grandpa = utils.getFatherOf(someone);
        Person[] grandmaChildren = utils.getChildrenOf(grandma);
        Person[] grandpaChildren = utils.getChildrenOf(grandpa);
        assertArrayEquals(grandmaChildren, grandpaChildren);
        assertEquals(1, grandmaChildren.length);
        assertEquals(someone, grandmaChildren[0]);

        // but that's booooring... I want a REAL family!
        // well okay, here's mine!
        Person david = new Person(
                "david",
                "rootUsername",
                "David",
                "Brown",
                "m",
                null,
                null,
                "tara"
        );
        Person tara = new Person(
                "tara",
                "rootUsername",
                "Tara",
                "Brown",
                "f",
                null,
                null,
                "david"
        );
        // hey, it's me!
        Person taylor = new Person(
                "taylor",
                "rootUsername",
                "Taylor",
                "Brown",
                "m",
                "david",
                "tara",
                "arryanna"
        );
        Person arryanna = new Person(
                "arryanna",
                "rootUsername",
                "Arryanna",
                "Brown",
                "m",
                null,
                null,
                "taylor"
        );
        Person ashlyn = new Person(
                "ashlyn",
                "rootUsername",
                "Ashlyn",
                "Brown",
                "f",
                "david",
                "tara",
                null
        );
        Person adam = new Person(
                "adam",
                "rootUsername",
                "Adam",
                "Brown",
                "m",
                "david",
                "tara",
                null
        );
        Person[] allPeople = {david, tara, taylor, arryanna, ashlyn, adam};
        Event[] allEvents = {};
        DataCache.setTestInstance("taylor", allPeople, allEvents);

        Person[] mySiblingsAndMe = utils.getChildrenOf(david);
        Person[] mySiblingsAndMeAccordingToMom = utils.getChildrenOf(tara);
        assertArrayEquals(mySiblingsAndMe, mySiblingsAndMeAccordingToMom);
        assertEquals(3, mySiblingsAndMe.length);
        Set<Person> siblingSet = new HashSet<>(Arrays.asList(mySiblingsAndMe));
        assertTrue(siblingSet.contains(taylor));
        assertTrue(siblingSet.contains(ashlyn));
        assertTrue(siblingSet.contains(adam));
        assertFalse(siblingSet.contains(david));
        assertFalse(siblingSet.contains(tara));
        assertFalse(siblingSet.contains(arryanna));

        assertNull(utils.getChildrenOf(null));
    }
}
