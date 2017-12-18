package fr.univtln.cniobechoudayer.pimpmytrip.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerViewAdapterReferenceTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.callbacks.SimpleItemTouchHelperCallback;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * Fragment representing the Trip Fragment
 */
public class TripsFragment extends Fragment {
    private static String TAG = "DATABASE";

    private static TripsFragment singleton;
    private List<Trip> tripsList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapterReferenceTrip adapterTrip;
    private FragmentManager fragmentManager;

    private ValueEventListener listenerDbMyTrips;

    //TODO refactor it in Tripcontroller
    private DatabaseReference dbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private DatabaseReference dbMyTrips = (DatabaseReference) dbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

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
        fragmentManager = getActivity().getSupportFragmentManager();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tripsList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewTrips);
        adapterTrip = new RecyclerViewAdapterReferenceTrip(getActivity(),tripsList, "MY TRIPS", fragmentManager);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterTrip);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapterTrip, recyclerView);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        listenerDbMyTrips = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tripsList.clear();
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    if (currentTrip != null) {
                        currentTrip.setId(tripSnapshot.getKey());
                        if (!currentTrip.isReference())
                            tripsList.add(currentTrip);
                    }
                }
                adapterTrip.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleMyTrips));

        return rootView;
    }

    /**
     * Handling life cycle methods
     */

    @Override
    public void onStart() {
        super.onStart();
        dbMyTrips.addValueEventListener(listenerDbMyTrips);
    }

    @Override
    public void onStop() {
        super.onStop();
        dbMyTrips.removeEventListener(listenerDbMyTrips);
    }
}
