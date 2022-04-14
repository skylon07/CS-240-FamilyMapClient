package org.skylon07.familymapclient.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.skylon07.familymapclient.R;

import models.Event;

public class EventActivity extends AppCompatActivity {
    private String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        this.setTitle(R.string.eventActivityLabel);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.eventID = this.getIntent().getStringExtra("eventID");

        FragmentManager manager = this.getSupportFragmentManager();
        MapFragment fragment = MapFragment.createNewMap(this.eventID);
        fragment.setOnDetailsClickCallback((Event focusedEvent) -> {
            if (focusedEvent != null) {
                Intent intent = new Intent(this, PersonActivity.class);
                intent.putExtra("personID", focusedEvent.getPersonID());
                this.startActivity(intent);
            }
        });
        manager.beginTransaction()
                .replace(R.id.mapFrameLayout, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem button) {
        if (button.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }
}