package com.example.familymapclient;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import Data.DataCache;
import model.Event;
import model.Person;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;



public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;
    private final Map<String, Float> markerColors = new HashMap<>();
    private final float[] colorChoices = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN,
    BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET,
    BitmapDescriptorFactory.HUE_YELLOW};
    private TextView eventView;
    private View view;
    private final Set<Integer> usedIndexes = new TreeSet<>();
    private DataCache instance = DataCache.getInstance();
    private String currGender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity().getClass() == MainActivity.class) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(layoutInflater, container, savedInstanceState);
        view = layoutInflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==  R.id.search_button) {
            Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
            startActivity(searchIntent);
            return true;
        }
        else if (item.getItemId() == R.id.settings_button) {
            Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        instance = DataCache.getInstance();
        eventView = view.findViewById(R.id.mapTextView);
        addEventMarkers();
        if (getActivity().getClass() == EventActivity.class) {
           Event currEvent = instance.getCurrEvent();
           LatLng position = new LatLng(currEvent.getLatitude(), currEvent.getLongitude());
           displayEvent(currEvent, position);
        }
        else {
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    if (marker != null) {
                        Event currEvent = (Event) marker.getTag();
                        displayEvent(currEvent, marker.getPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
        eventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayEvent(Event currEvent, LatLng position) {
        eventView.setText(instance.eventToText(currEvent));
        Drawable genderIcon;
        if (currGender.equals("m")) {
            genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).
                    colorRes(R.color.male_icon).sizeDp(40);
            eventView.setCompoundDrawables(genderIcon, null, null, null);
        }
        else if (currGender.equals("f")) {
            genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).
                    colorRes(R.color.female_icon).sizeDp(40);
            eventView.setCompoundDrawables(genderIcon, null, null, null);
        }
        map.animateCamera(CameraUpdateFactory.newLatLng(position));
        instance.setCurrPerson(instance.getPersonById(currEvent.getPersonId()));
    }

    private void addEventMarkers() {
    instance = DataCache.getInstance();
        Map<String, Event> events = instance.getEvents();
        for (Event e : events.values()) {
            LatLng currPosition = new LatLng(e.getLatitude(), e.getLongitude());
            Marker newMarker = map.addMarker(new MarkerOptions().position(currPosition).title(e.getEventType()));
            newMarker.setTag(e);
            setMarkerColor(newMarker, e);
        }
    }

    private void setMarkerColor(Marker currMarker, Event currEvent) {
        if (markerColors.isEmpty() || !markerColors.containsKey(currEvent.getEventType())) {
            int index = new Random().nextInt(colorChoices.length);
            if (usedIndexes.size() != colorChoices.length) {
                while (usedIndexes.contains(index)) {
                    index = new Random().nextInt(colorChoices.length);
                }
            }
            float newColor = colorChoices[index];
            usedIndexes.add(index);
            markerColors.put(currEvent.getEventType(), newColor);
        }
        currMarker.setIcon(BitmapDescriptorFactory.defaultMarker(markerColors.get(currEvent.getEventType())));
    }


    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.
    }
}
