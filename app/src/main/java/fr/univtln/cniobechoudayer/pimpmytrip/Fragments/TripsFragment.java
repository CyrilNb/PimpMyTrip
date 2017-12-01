package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterRefTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.callbacks.SimpleItemTouchHelperCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RefTripsFragment newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripsFragment extends Fragment {
    private static String TAG = "DATABASE";

    private static TripsFragment singleton;
    private DatabaseReference database;
    private List<Trip> tripsList;
    private RecyclerView recyclerView;
    private RecyclerAdapterTrip adapterTrip;

    public TripsFragment() {
        // Required empty public constructor
    }

    public static TripsFragment getInstance() {
        if (singleton == null) {
            singleton = new TripsFragment();
        }

        return singleton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripsList = new ArrayList<>();
        Trip trip = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 1", true, null, null,89, null);
        Trip trip2 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 2", true, null, null,89, null);
        Trip trip3 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 3", true, null, null,89, null);
        Trip trip4 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 4", true, null, null,89, null);
        Trip trip5 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 5", true, null, null,89, null);
        Trip trip6 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 6", true, null, null,89, null);
        Trip trip7 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 7", true, null, null,89, null);
        Trip trip8 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 8", true, null, null,89, null);
        Trip trip9 = new Trip("#7F7F00", Calendar.getInstance().getTime(), "Mon super trip 9", true, null, null,89, null);
        tripsList.add(trip);
        tripsList.add(trip2);
        tripsList.add(trip3);
        tripsList.add(trip4);
        tripsList.add(trip5);
        tripsList.add(trip6);
        tripsList.add(trip7);
        tripsList.add(trip8);
        tripsList.add(trip9);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //tripsList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewTrips);
        adapterTrip = new RecyclerAdapterTrip(tripsList, getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterTrip);

        //For drag and swipe with recyclerView
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapterTrip,recyclerView);
        ItemTouchHelper touchHelper =  new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        /*database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This method is called once with the initial value and again whenever data at this location is updated
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    Log.d("listreftrips", String.valueOf(currentTrip.getName()));
                    if (!currentTrip.isReference()) {
                        tripsList.add(currentTrip);
                    }
                }

                Log.d("listreftrips", String.valueOf(tripsList.size()));
                adapterTrip.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.d(TAG, "Failed to read value of a trip in RefTripsFargment", databaseError.toException());
            }
        });*/

    }

}
