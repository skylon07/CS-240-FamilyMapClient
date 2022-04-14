package org.skylon07.familymapclient.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.skylon07.familymapclient.R;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.EventManager;
import org.skylon07.familymapclient.utilities.FamilyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Event;
import models.Person;

public class PersonActivity extends AppCompatActivity {
    private static int LIFE_EVENTS_LIST_TYPE = 0;
    private static int FAMILY_LIST_TYPE = 1;
    public static int CHILD_RELATION_TYPE = 2;
    public static int FATHER_RELATION_TYPE = 3;
    public static int MOTHER_RELATION_TYPE = 4;
    public static int SPOUSE_RELATION_TYPE = 5;

    private Person shownPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        this.setTitle(R.string.personActivityLabel);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String personID = this.getIntent().getStringExtra("personID");
        this.loadPerson(personID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem button) {
        if (button.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    private void loadPerson(String personID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Person person = DataCache.getInstance().getPersonByID(personID);
                this.shownPerson = person;

                EventManager eventManager = new EventManager();
                Event[] lifeEvents = eventManager.getSortedEventsForPerson(person);

                FamilyUtils familyUtils = new FamilyUtils();
                Person father = familyUtils.getFatherOf(person);
                Person mother = familyUtils.getMotherOf(person);
                Person spouse = familyUtils.getSpouseOf(person);
                Person[] children = familyUtils.getChildrenOf(person);
                ArrayList<FamilyPerson> familyMembersList = new ArrayList<>();
                if (father != null) {
                    familyMembersList.add(new FamilyPerson(father, FATHER_RELATION_TYPE));
                }
                if (mother != null) {
                    familyMembersList.add(new FamilyPerson(mother, MOTHER_RELATION_TYPE));
                }
                if (spouse != null) {
                    familyMembersList.add(new FamilyPerson(spouse, SPOUSE_RELATION_TYPE));
                }
                for (Person child : children) {
                    familyMembersList.add(new FamilyPerson(child, CHILD_RELATION_TYPE));
                }
                FamilyPerson[] familyMembers = familyMembersList.toArray(new FamilyPerson[familyMembersList.size()]);

                this.runOnUiThread(() -> {
                    ((TextView) this.findViewById(R.id.firstName)).setText(person.getFirstName());
                    ((TextView) this.findViewById(R.id.lastName)).setText(person.getLastName());
                    if (person.getGender().equals("m")) {
                        ((TextView) this.findViewById(R.id.gender)).setText("Male");
                    } else {
                        ((TextView) this.findViewById(R.id.gender)).setText("Female");
                    }
                    ExpandableListView expandableList = this.findViewById(R.id.detailsList);
                    expandableList.setAdapter(new ListAdapter(lifeEvents, familyMembers));
                });
            } catch (IOException err) {
                this.runOnUiThread(() -> {
                    Toast.makeText(this, "Uh oh, the person could not be loaded!", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private class ListAdapter extends BaseExpandableListAdapter {
        private FamilyPerson[] personList;
        private Event[] eventList;

        public ListAdapter(Event[] events, FamilyPerson[] persons) {
            this.eventList = events;
            this.personList = persons;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (groupPosition == LIFE_EVENTS_LIST_TYPE) {
                return this.eventList.length;
            } else {
                return this.personList.length;
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            if (groupPosition == LIFE_EVENTS_LIST_TYPE) {
                return PersonActivity.this.getString(R.string.lifeEventsTitle);
            } else {
                return PersonActivity.this.getString(R.string.familyTitle);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            if (groupPosition == LIFE_EVENTS_LIST_TYPE) {
                return this.eventList[childPosition];
            } else {
                return this.personList[childPosition];
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View titleView, ViewGroup parent) {
            titleView = PersonActivity.this.getLayoutInflater().inflate(R.layout.person_list_view, parent, false);
            TextView titleText = titleView.findViewById(R.id.listTitle);
            if (groupPosition == LIFE_EVENTS_LIST_TYPE) {
                titleText.setText(R.string.lifeEventsTitle);
            } else {
                titleText.setText(R.string.familyTitle);
            }
            return titleView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View dunnoWatThisIs, ViewGroup parent) {
            View itemView = PersonActivity.this.getLayoutInflater().inflate(R.layout.search_item, parent, false);
            if (groupPosition == LIFE_EVENTS_LIST_TYPE) {
                Event lifeEvent = this.eventList[childPosition];
                ((ImageView) itemView.findViewById(R.id.resultIcon)).setImageResource(R.drawable.marker);
                // TODO: this string formatting is reused... should probably be abstracted to a utility
                ((TextView) itemView.findViewById(R.id.resultFirstLine)).setText(String.format(
                        "%s: %s %s (%d)",
                        lifeEvent.getEventType(),
                        lifeEvent.getCity(),
                        lifeEvent.getCountry(),
                        lifeEvent.getYear()
                ));
                ((TextView) itemView.findViewById(R.id.resultSecondLine)).setText(String.format(
                        "%s %s",
                        PersonActivity.this.shownPerson.getFirstName(),
                        PersonActivity.this.shownPerson.getLastName()
                ));

                itemView.setOnClickListener((View v) -> {
                    PersonActivity.this.onEventClick(lifeEvent);
                });
            } else {
                FamilyPerson familyMember = this.personList[childPosition];
                if (familyMember.person.getGender().equals("m")) {
                    ((ImageView) itemView.findViewById(R.id.resultIcon)).setImageResource(R.drawable.male);
                } else {
                    ((ImageView) itemView.findViewById(R.id.resultIcon)).setImageResource(R.drawable.female);
                }
                // TODO: this string formatting is reused... should probably be abstracted to a utility
                ((TextView) itemView.findViewById(R.id.resultFirstLine)).setText(String.format(
                        "%s %s",
                        familyMember.person.getFirstName(),
                        familyMember.person.getLastName()));
                // TODO: should put these as strings in resource file...
                TextView relationResult = itemView.findViewById(R.id.resultSecondLine);
                if (familyMember.relationType == FATHER_RELATION_TYPE) {
                    relationResult.setText("Father");
                } else if (familyMember.relationType == MOTHER_RELATION_TYPE) {
                    relationResult.setText("Mother");
                } else if (familyMember.relationType == SPOUSE_RELATION_TYPE) {
                    relationResult.setText("Spouse");
                } else if (familyMember.relationType == CHILD_RELATION_TYPE) {
                    relationResult.setText("Child");
                } else {
                    // TODO: could probably do something better than this...
                    throw new RuntimeException("Bad relation type");
                }

                itemView.setOnClickListener((View v) -> {
                    PersonActivity.this.onPersonClick(familyMember.person);
                });
            }
            return itemView;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }

    private class FamilyPerson {
        // getters and setters not needed; this is purely meant to batch data together
        public Person person;
        public int relationType;

        public FamilyPerson(Person person, int relationType) {
            this.person = person;
            this.relationType = relationType;
        }
    }

    private void onPersonClick(Person person) {
        Intent intent = new Intent(this, PersonActivity.class);
        intent.putExtra("personID", person.getPersonID());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        this.startActivity(intent);
        this.finish();
    }

    private void onEventClick(Event event) {
        Intent intent = new Intent(this, EventActivity.class);
        intent.putExtra("eventID", event.getEventID());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        this.startActivity(intent);
        this.finish();
    }
}