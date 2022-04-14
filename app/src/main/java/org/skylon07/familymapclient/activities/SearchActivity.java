package org.skylon07.familymapclient.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.skylon07.familymapclient.R;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.SearchManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Event;
import models.Person;

public class SearchActivity extends AppCompatActivity {
    private static int PERSON_VIEW_TYPE = 1;
    private static int EVENT_VIEW_TYPE = 2;
    private static int SEARCH_DELAY = 300;

    private CountDownTimer currentTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.searchActivityLabel);
        this.setContentView(R.layout.activity_search);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView searchResults = this.findViewById(R.id.searchResults);
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        searchResults.setAdapter(null);

        EditText input = this.findViewById(R.id.searchInput);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                SearchActivity.this.cancelLastSearch();
                SearchActivity.this.delayThenSearch(input.getText().toString());
            }
        };
        input.addTextChangedListener(watcher);

        View searchClearButton = this.findViewById(R.id.searchClearButton);
        searchClearButton.setOnClickListener((View v) -> {
            input.setText("");
        });
    }

    private void delayThenSearch(String searchQuery) {
        CountDownTimer timer = new CountDownTimer(SEARCH_DELAY, SEARCH_DELAY) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {
                    SearchActivity.this.searchAndDisplayResults(searchQuery);
                });
            }
        };
        timer.start();
        this.currentTimer = timer;
    }

    private void cancelLastSearch() {
        if (this.currentTimer != null) {
            this.currentTimer.cancel();
            this.currentTimer = null;
        }
    }

    private void searchAndDisplayResults(String searchQuery) {
        if (searchQuery == null || searchQuery.equals("")) {
            this.runOnUiThread(() -> {
                RecyclerView searchResults = this.findViewById(R.id.searchResults);
                searchResults.setAdapter(null);
            });
            return;
        }

        SearchManager searcher = new SearchManager();
        try {
            Person[] people = searcher.searchPeople(searchQuery);
            Event[] events = searcher.searchEvents(searchQuery, this);

            this.runOnUiThread(() -> {
                RecyclerView searchResults = this.findViewById(R.id.searchResults);
                searchResults.setAdapter(new SearchAdapter(people, events));
            });
        } catch (IOException err) {
            this.runOnUiThread(() -> {
                Toast.makeText(this, "Search failed due to an error", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem button) {
        if (button.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        private Person[] personResults;
        private Event[] eventResults;

        public SearchAdapter(Person[] personResults, Event[] eventResults) {
            this.personResults = personResults;
            this.eventResults = eventResults;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            // guess this if wasn't necessary....
            if (viewType == PERSON_VIEW_TYPE) {
                view = SearchActivity.this.getLayoutInflater().inflate(R.layout.search_item, parent, false);
            } else {
                view = SearchActivity.this.getLayoutInflater().inflate(R.layout.search_item, parent, false);
            }

            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if (position < this.personResults.length) {
                holder.bind(this.personResults[position]);
            } else {
                holder.bind(this.eventResults[position - this.personResults.length]);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < this.personResults.length) {
                return PERSON_VIEW_TYPE;
            } else {
                return EVENT_VIEW_TYPE;
            }
        }

        @Override
        public int getItemCount() {
            return this.personResults.length + this.eventResults.length;
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder {
        /** contains the type of view this is holding */
        private int viewType;
        private View itemView;
        private Person person;
        private Event event;

        public SearchViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            itemView.setOnClickListener(this::onItemClick);
            this.itemView = itemView;
        }

        public void bind(Person person) {
            assert this.viewType == PERSON_VIEW_TYPE : "Tried to bind to person with event view type";
            this.person = person;

            ImageView icon = this.itemView.findViewById(R.id.resultIcon);
            if (person.getGender().equals("m")) {
                icon.setImageResource(R.drawable.male);
            } else {
                icon.setImageResource(R.drawable.female);
            }
            TextView result = this.itemView.findViewById(R.id.resultFirstLine);
            result.setText(String.format("%s %s", person.getFirstName(), person.getLastName()));;
        }

        public void bind(Event event) {
            assert this.viewType == EVENT_VIEW_TYPE : "Tried to bind to event with person view type";
            this.event = event;

            ImageView icon = this.itemView.findViewById(R.id.resultIcon);
            icon.setImageResource(R.drawable.marker);
            TextView result1 = this.itemView.findViewById(R.id.resultFirstLine);
            result1.setText(String.format(
                    "%s: %s %s (%d)",
                    event.getEventType(),
                    event.getCity(),
                    event.getCountry(),
                    event.getYear()
            ));

            // yes, I normally would abstract this into different functions but I am
            // RUNNING OUT OF TIME
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    Person person = DataCache.getInstance().getPersonByID(event.getPersonID());
                    SearchActivity.this.runOnUiThread(() -> {
                        TextView result2 = this.itemView.findViewById(R.id.resultSecondLine);
                        result2.setText(String.format(
                                "%s %s",
                                person.getFirstName(),
                                person.getLastName()
                        ));
                    });
                } catch (IOException err) {
                    SearchActivity.this.runOnUiThread(() -> {
                        Toast.makeText(SearchActivity.this, "An event couldn't get a person", Toast.LENGTH_LONG).show();
                    });
                }
            });
        }

        private void onItemClick(View view) {
            if (this.viewType == PERSON_VIEW_TYPE) {
                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra("personID", this.person.getPersonID());
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                SearchActivity.this.startActivity(intent);
                SearchActivity.this.finish();
            } else {
                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra("eventID", this.event.getEventID());
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                SearchActivity.this.startActivity(intent);
                SearchActivity.this.finish();
            }
        }
    }
}