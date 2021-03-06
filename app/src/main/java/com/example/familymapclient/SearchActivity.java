package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Data.DataCache;
import model.Event;
import model.Person;

public class SearchActivity extends AppCompatActivity {

    private static final int EVENT_ITEM_VIEW_TYPE = 0;
    private static final int FAMILY_ITEM_VIEW_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        SearchView searchView = findViewById(R.id.searchBar);
        DataCache instance = DataCache.getInstance();

        List<Event> events = new ArrayList<>();
        events.addAll(instance.getEvents().values());
        List<Person> people = new ArrayList<>();
        people.addAll(instance.getPeople().values());
        List<Event> keepEvents = new ArrayList<>();
        List<Person> keepPeople = new ArrayList<>();
        SearchAdapter adapter = new SearchAdapter(keepEvents, keepPeople);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
               return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                keepEvents.clear();
                keepPeople.clear();
                instance.search(s, keepEvents, keepPeople);
                adapter.updateStuff(keepEvents, keepPeople);
                return true;
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    private String eventToText(Event event) {
        DataCache instance = DataCache.getInstance();
        String eventString;
        Person currPerson = instance.getPersonById(event.getPersonId());
        eventString = event.getEventType().toUpperCase() + ": " + event.getCity() + ", " + event.getCountry() + " (" +
                event.getYear() + ")\n" + currPerson.getFirstName() + " " + currPerson.getLastName();
        return eventString;
    }

    private String personToText(Person person) {
        String personString;
        personString = person.getFirstName() + " " + person.getLastName();
        return personString;
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        private List<Event> eventList;
        private List<Person> peopleList;

        SearchAdapter(List<Event> eventList, List<Person> peopleList) {
            this.eventList = eventList;
            this.peopleList = peopleList;
        }

        public void updateStuff(List<Event> events, List<Person> people) {
            this.eventList = events;
            this.peopleList = people;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return position < peopleList.size() ? FAMILY_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            System.out.println("View Holder");
            View view;

            if(viewType == FAMILY_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.family_search, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.event_search, parent, false);
            }

            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            System.out.println(" on bind View Holder");
            if(position < peopleList.size()) {
                holder.bind(peopleList.get(position));
            } else {
                holder.bind(eventList.get(position - peopleList.size()));
            }
        }

        @Override
        public int getItemCount() {
            return eventList.size() + peopleList.size();
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView name;

        private final int viewType;
        private Event event;
        private Person person;
        private DataCache instance;

        SearchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if(viewType == FAMILY_ITEM_VIEW_TYPE) {
                name = itemView.findViewById(R.id.familyTitleSearch);
            } else {
                name = itemView.findViewById(R.id.eventTitleSearch);
            }
            this.instance = DataCache.getInstance();
        }

        private void bind(Event event) {
            this.event = event;
            name.setText(instance.eventToText(event));
            Drawable eventIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.map_marker_icon).sizeDp(40);
            name.setCompoundDrawables(eventIcon, null, null, null);

        }

        private void bind(Person person) {
            this.person = person;
            name.setText(instance.personToText(person));
            Drawable genderIcon;
            if (person.getGender().equals("m")) {
                genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_male)
                        .colorRes(R.color.male_icon).sizeDp(40);
                name.setCompoundDrawables(genderIcon, null, null, null);
            }
            else if (person.getGender().equals("f")) {
                genderIcon = new IconDrawable(SearchActivity.this, FontAwesomeIcons.fa_female)
                        .colorRes(R.color.female_icon).sizeDp(40);
                name.setCompoundDrawables(genderIcon, null, null, null);
            }
        }

        @Override
        public void onClick(View view) {
            if (viewType == FAMILY_ITEM_VIEW_TYPE) {
                instance.setCurrPerson(person);
                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                startActivity(intent);
            } else {
                instance.setCurrEvent(event);
                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                startActivity(intent);
            }
        }
    }
}
