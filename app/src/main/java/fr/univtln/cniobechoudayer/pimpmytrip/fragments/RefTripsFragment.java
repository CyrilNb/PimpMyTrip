package fr.univtln.cniobechoudayer.pimpmytrip.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterRefTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RefTripsFragment newInstance} factory method to
 * create an instance of this fragment.
 */
public class RefTripsFragment extends Fragment {
    private static String TAG = "DATABASE";

    private static RefTripsFragment singleton;
    private DatabaseReference database;
    private List<Trip> refTripsList;
    private RecyclerView recyclerView;
    private RecyclerAdapterRefTrip recyclerAdapterRefTrip;

    public RefTripsFragment() {
        // Required empty public constructor
    }

    public static RefTripsFragment getInstance() {
        if (singleton == null) {
            singleton = new RefTripsFragment();
        }

        return singleton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        refTripsList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_ref_trips, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewRefTrips);
        recyclerAdapterRefTrip = new RecyclerAdapterRefTrip(refTripsList, getActivity());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapterRefTrip);

        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleRefTrips));

        return rootView;
    }

    /**
     * Setting up database listeners
     * when fragment starts
     */
    @Override
    public void onStart() {
        super.onStart();

        database = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This method is called once with the initial value and again whenever data at this location is updated
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    Log.d("listreftrips", String.valueOf(currentTrip.getName()));
                    if (currentTrip.isReference()) {
                        refTripsList.add(currentTrip);
                    }
                }
                Log.d("listreftrips", String.valueOf(refTripsList.size()));
                recyclerAdapterRefTrip.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.d(TAG, "Failed to read value of a trip in RefTripsFargment", databaseError.toException());
            }
        });

    }

}
