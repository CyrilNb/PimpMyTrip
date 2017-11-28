package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        Trip trip = new Trip("F2F2F2", Calendar.getInstance().getTime(), "trip", true, null, null, null);
        Trip trip2 = new Trip("F2F2F2", Calendar.getInstance().getTime(), "trip", true, null, null, null);
        Trip trip3 = new Trip("F2F2F2", Calendar.getInstance().getTime(), "trip", true, null, null, null);
        Trip trip4 = new Trip("F2F2F2", Calendar.getInstance().getTime(), "trip", true, null, null, null);
        Trip trip5 = new Trip("F2F2F2", Calendar.getInstance().getTime(), "trip", true, null, null, null);
        Trip trip6 = new Trip("F2F2F2", Calendar.getInstance().getTime(), "trip", true, null, null, null);
        tripsList.add(trip);
        tripsList.add(trip2);
        tripsList.add(trip3);
        tripsList.add(trip4);
        tripsList.add(trip5);
        tripsList.add(trip6);

        /*database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This method is called once with the initial value and again whenever data at this location is updated
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    tripsList.clear();
                    Trip value = dataSnapshot1.getValue(Trip.class);
                    //System.out.println(value.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.d(TAG, "Failed to read value of a trip in RefTripsFargment", databaseError.toException());
            }
        });*/
        System.out.println(tripsList.size());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewTrips);
        RecyclerAdapterTrip adapterTrip = new RecyclerAdapterTrip(tripsList, getActivity());
        adapterTrip.notifyDataSetChanged();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterTrip);
        System.out.println("reftrips:" + tripsList);
        return rootView;
    }

}
