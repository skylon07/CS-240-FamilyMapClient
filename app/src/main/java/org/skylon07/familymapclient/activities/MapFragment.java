package org.skylon07.familymapclient.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.skylon07.familymapclient.R;
import org.skylon07.familymapclient.utilities.DataCache;
import org.skylon07.familymapclient.utilities.EventManager;
import org.skylon07.familymapclient.utilities.FamilyUtils;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.Event;
import models.Person;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#createNewMap(String)} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {
    private static String NO_EVENT_SELECTED_TEXT = "Click a map marker to see event details";
    private static int SPOUSE_LINE_COLOR = 0xffaa0000;
    private static int MOTHER_LINE_COLOR = 0xff00aa00;
    private static int FATHER_LINE_COLOR = 0xff00aa00;
    private static int LIFE_LINE_COLOR = 0xff0000aa;
    private static float BASE_LINE_WIDTH = 15;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String EVENT_ID_PARAM = "param1";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventID is the ID of the event to focus (or null to avoid focusing)
     * @return A new instance of fragment MapFragment (focused on an event).
     */
    public static MapFragment createNewMap(String eventID) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(MapFragment.EVENT_ID_PARAM, eventID);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment createNewMap() {
        return MapFragment.createNewMap(null);
    }

    /** the currently active map object */
    private GoogleMap map;
    /** the event to focus on when the map loads */
    private String focusEventID;
    /** the event currently focused (not to be confused with the argument focusEventID */
    private Event focusedEvent;
    /** an array tracking all currently drawn Polylines on the map */
    private ArrayList<Polyline> drawnLines;
    private OnDetailsClicCallback onDetailsClickCallback;

    /**
     * Creates a new instance of a MapFragment.
     * Android Studio says this empty constructor is required for other functions
     */
    public MapFragment() {
        this.map = null;
        this.focusEventID = null;
        this.focusedEvent = null;
        this.drawnLines = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null) {
            this.focusEventID = this.getArguments().getString(EVENT_ID_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)
                this.getChildFragmentManager().findFragmentById(R.id.supportMap);
        mapFragment.getMapAsync((GoogleMap map) -> {
            this.map = map;
            map.setOnMapLoadedCallback(() -> {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                // this is required to avoid server-on-UI-thread exceptions
                executor.submit(this::loadMapMarkers);
                map.setOnMarkerClickListener(this::onMarkerClick);
                View mapBottom = this.getView().findViewById(R.id.mapBottomView);
                mapBottom.setOnClickListener(this::onDetailsClick);
            });
        });

        TextView details = (TextView) view.findViewById(R.id.mapBottomText);
        details.setText(MapFragment.NO_EVENT_SELECTED_TEXT);
        return view;
    }

    /**
     * This function takes the events from the data cache and creates map markers for them.
     * (This particular function gave me HOURS of pain... I eventually found out that google's
     * addMarker() function hangs completely if it is not run on the UI thread; lesson learned)
     */
    private void loadMapMarkers() {
        EventManager eventsManager = new EventManager();
        Event[] events = eventsManager.getEventsWithSettingsFilter(this.getContext());
        for (Event event : events) {
            LatLng eventPosition = new LatLng(event.getLatitude(), event.getLongitude());
            float eventColor = eventsManager.getEventColor(event);
            // running on UI thread is required to keep map.addMarker() from hanging
            this.getActivity().runOnUiThread(() -> {
                Marker newMarker = this.map.addMarker(
                        new MarkerOptions()
                        .title(event.getEventType())
                        .position(eventPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(eventColor))
                );
                newMarker.setTag(event);
            });
        }

        if (this.focusEventID != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    Event event = DataCache.getInstance().getEventByID(this.focusEventID);
                    this.getActivity().runOnUiThread(() -> {
                        this.focusOnEvent(event, true);
                    });
                } catch (IOException err) {
                    this.getActivity().runOnUiThread(() -> {
                        Toast.makeText(this.getActivity(), "Uh oh! Couldn't focus on event", Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    private void focusOnEvent(Event event, boolean shouldZoom) {
        this.focusedEvent = event;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Person person = DataCache.getInstance().getPersonByID(event.getPersonID());

                this.getActivity().runOnUiThread(() -> {
                    TextView text = this.getView().findViewById(R.id.mapBottomText);
                    text.setText(String.format(
                            "%s %s\n%s: %s, %s (%d)",
                            person.getFirstName(),
                            person.getLastName(),
                            event.getEventType(),
                            event.getCountry(),
                            event.getCity(),
                            event.getYear()
                    ));

                    ImageView icon = this.getView().findViewById(R.id.mapBottomIcon);
                    int drawable = 0;
                    if (person.getGender().equals("m")) {
                        drawable = R.drawable.male;
                    } else if (person.getGender().equals("f")) {
                        drawable = R.drawable.female;
                    }
                    icon.setImageResource(drawable);

                    if (shouldZoom) {
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(
                                new LatLng(event.getLatitude(), event.getLongitude()),
                                (float) 8
                        );
                        this.map.animateCamera(camera);
                    } else {
                        CameraUpdate camera = CameraUpdateFactory.newLatLng(
                                new LatLng(event.getLatitude(), event.getLongitude())
                        );
                        this.map.animateCamera(camera);
                    }
                });

                this.updatePolyLines(event, person);
            } catch (IOException err) {
                this.getActivity().runOnUiThread(() -> {
                    Toast.makeText(this.getActivity(), "Uh oh! Something went wrong", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updatePolyLines(Event event, Person associatedPerson) throws IOException {
        // first, clear all lines
        this.getActivity().runOnUiThread(() -> {
            for (Polyline line : this.drawnLines) {
                line.remove();
            }
        });

        if (event == null) {
            return;
        }

        // unfortunately, this is kind of messy tech debt...
        // ideally I'd have abstracted out the data-getting and business logic off of the UI
        // thread, then run updating functions on the ui thread, but I don't have time
        // to refactor it right now
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            LatLng origin = new LatLng(event.getLatitude(), event.getLongitude());
            EventManager eventManager = new EventManager();
            FamilyUtils familyUtils = new FamilyUtils();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            boolean useSpouseLines =        prefs.getBoolean("spouseLines",     false);
            boolean useFamilyTreeLines =    prefs.getBoolean("familyTreeLines", false);
            boolean useStoryLines =         prefs.getBoolean("storyLines",      false);

            try {
                // set spouse line
                if (useSpouseLines) {
                    Person spouse = familyUtils.getSpouseOf(associatedPerson);
                    Event[] spouseEvents = eventManager.getSortedEventsForPerson(spouse);
                    if (spouseEvents != null && spouseEvents.length > 0) {
                        Event spouseFirstEvent = spouseEvents[0];
                        LatLng spouseLocation = new LatLng(
                                spouseFirstEvent.getLatitude(),
                                spouseFirstEvent.getLongitude()
                        );
                        this.drawPolyLine(origin, spouseLocation, SPOUSE_LINE_COLOR);
                    }
                }

                // set family tree lines
                if (useFamilyTreeLines) {
                    this.drawAncestorLines(origin, associatedPerson);
                }

                // set life story lines
                if (useStoryLines) {
                    Event currEvent = null;
                    for (Event nextEvent : eventManager.getSortedEventsForPerson(associatedPerson)) {
                        if (currEvent != null) {
                            LatLng lineStart = new LatLng(currEvent.getLatitude(), currEvent.getLongitude());
                            LatLng lineEnd = new LatLng(nextEvent.getLatitude(), nextEvent.getLongitude());
                            this.drawPolyLine(lineStart, lineEnd, LIFE_LINE_COLOR);
                        }
                        currEvent = nextEvent;
                    }
                }
            } catch (IOException err) {
                this.getActivity().runOnUiThread(() -> {
                    Toast.makeText(this.getActivity(), "Drawing lines failed!", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void drawPolyLine(LatLng start, LatLng end, int color) {
        this.getActivity().runOnUiThread(() -> {
            Polyline newLine = this.map.addPolyline(
                    new PolylineOptions()
                    .add(start)
                    .add(end)
                    .color(color)
                    .width(BASE_LINE_WIDTH)
            );
            this.drawnLines.add(newLine);
        });
    }

    private void drawPolyLine(LatLng start, LatLng end, int color, float width) {
        this.getActivity().runOnUiThread(() -> {
            Polyline newLine = this.map.addPolyline(
                    new PolylineOptions()
                    .add(start)
                    .add(end)
                    .color(color)
                    .width(width)
            );
            this.drawnLines.add(newLine);
        });
    }

    private void drawAncestorLines(LatLng start, Person person) throws IOException {
        this.drawAncestorLines(start, person, BASE_LINE_WIDTH);
    }

    private void drawAncestorLines(LatLng start, Person person, float width) throws IOException {
        EventManager eventManager = new EventManager();
        FamilyUtils familyUtils = new FamilyUtils();

        Person mother = familyUtils.getMotherOf(person);
        if (mother != null) {
            Event[] motherEvents = eventManager.getSortedEventsForPerson(mother);
            if (motherEvents != null && motherEvents.length > 0) {
                Event motherEvent = motherEvents[0];
                LatLng motherLineEnd = new LatLng(motherEvent.getLatitude(), motherEvent.getLongitude());
                this.drawPolyLine(start, motherLineEnd, (int) (MOTHER_LINE_COLOR), width);
                this.drawAncestorLines(motherLineEnd, mother, width * (float) 0.7);
            }
        }

        Person father = familyUtils.getFatherOf(person);
        if (father != null) {
            Event[] fatherEvents = eventManager.getSortedEventsForPerson(father);
            if (fatherEvents != null && fatherEvents.length > 0) {
                Event fatherEvent = fatherEvents[0];
                LatLng fatherLineEnd = new LatLng(fatherEvent.getLatitude(), fatherEvent.getLongitude());
                this.drawPolyLine(start, fatherLineEnd, (int) (FATHER_LINE_COLOR), width);
                this.drawAncestorLines(fatherLineEnd, father, width * (float) 0.7);
            }
        }
    }

    /**
     * Performs an action when any given Marker is clicked
     *
     * @param marker is the marker that was just clicked
     * @return true; all clicks are consumed here
     */
    private boolean onMarkerClick(Marker marker) {
        Event event = (Event) marker.getTag();
        this.focusOnEvent(event, false);
        return true;
    }

    public interface OnDetailsClicCallback {
        public void run(Event focusedEvent);
    }
    public void setOnDetailsClickCallback(OnDetailsClicCallback onDetailsClickCallback) {
        this.onDetailsClickCallback = onDetailsClickCallback;
    }

    private boolean onDetailsClick(View v) {
        if (this.onDetailsClickCallback != null) {
            this.onDetailsClickCallback.run(this.focusedEvent);
        }
        return true;
    }
}