package com.example.familymapclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.List;

import Data.DataCache;
import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        DataCache instance = DataCache.getInstance();

        TextView firstView = findViewById(R.id.firstNameTextView);
        TextView lastView = findViewById(R.id.lastNameTextView);
        TextView genderView = findViewById(R.id.genderTextView);

        String firstName = instance.getCurrPerson().getFirstName();
        String lastName = instance.getCurrPerson().getLastName();
        firstView.setText(firstName);
        lastView.setText(lastName);

        if (instance.getCurrPerson().getGender().equals("m")) {
            genderView.setText(R.string.male);
        }
        else if (instance.getCurrPerson().getGender().equals("f")) {
            genderView.setText(R.string.female);
        }

        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        instance.createPersonFamily();
        List<Event> personEvents = instance.getPersonEvents().get(instance.getCurrPerson().getPersonID());
        List<Person> personFamily = instance.getPersonFamily().get(instance.getCurrPerson().getPersonID());
        expandableListView.setAdapter(new ExpandableListAdapter(personEvents, personFamily));
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

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENT_GROUP_POSITION = 0;
        private static final int FAMILY_GROUP_POSITION = 1;

        private final List<Event> personEvents;
        private final List<Person> personFamily;
        private DataCache instance;

        ExpandableListAdapter(List<Event> personEvents, List<Person> personFamily) {
            this.personEvents = personEvents;
            this.personFamily = personFamily;
            this.instance = DataCache.getInstance();
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return personEvents.size();
                case FAMILY_GROUP_POSITION:
                    return personFamily.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            // Not used
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // Not used
            return null;
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
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.person_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.eventTitle);
                    break;
                case FAMILY_GROUP_POSITION:
                    titleView.setText(R.string.familyTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_list_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                case FAMILY_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.family_list_item, parent, false);
                    initializeFamilyView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        private void initializeEventView(View eventItemView, final int childPosition) {
            TextView eventNameView = eventItemView.findViewById(R.id.eventTitle);
            eventNameView.setText(instance.eventToText(personEvents.get(childPosition)));
            Drawable eventIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.map_marker_icon).sizeDp(40);
            eventNameView.setCompoundDrawables(eventIcon, null, null, null);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instance.setCurrEvent(personEvents.get(childPosition));
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    startActivity(intent);
                }
            });
        }

        private void initializeFamilyView(View familyItemView, final int childPosition) {
            TextView famNameView = familyItemView.findViewById(R.id.familyTitle);
            famNameView.setText(instance.personToTextPersonRelationship(personFamily.get(childPosition)));
            Drawable genderIcon;
            if (personFamily.get(childPosition).getGender().equals("m")) {
                genderIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_male).
                        colorRes(R.color.male_icon).sizeDp(40);
                famNameView.setCompoundDrawables(genderIcon, null, null, null);
            }
            else if (personFamily.get(childPosition).getGender().equals("f")) {
                genderIcon = new IconDrawable(PersonActivity.this, FontAwesomeIcons.fa_female).
                        colorRes(R.color.female_icon).sizeDp(40);
                famNameView.setCompoundDrawables(genderIcon, null, null, null);
            }
            familyItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instance.setCurrPerson(personFamily.get(childPosition));
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
