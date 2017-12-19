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

    private static final String TAG = "DATABASE";

    private List<Trip> mRefTripsList;

    private static RefTripsFragment sInstance;
    private RecyclerView mRecyclerView;
    private RecyclerAdapterRefTrip mRecyclerAdapterRefTrip;

    private DatabaseReference mDatabase;

    public RefTripsFragment() {
        // Required empty public constructor
    }

    public static RefTripsFragment getInstance() {
        if (sInstance == null) {
            sInstance = new RefTripsFragment();
        }

        return sInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRefTripsList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_ref_trips, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewRefTrips);
        mRecyclerAdapterRefTrip = new RecyclerAdapterRefTrip(mRefTripsList, getActivity());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRecyclerAdapterRefTrip);

        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleRefTrips));

        return rootView;
    }

    /**
     * Setting up mDatabase listeners
     * when fragment starts
     */
    @Override
    public void onStart() {
        super.onStart();

        mDatabase = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //This method is called once with the initial value and again whenever data at this location is updated
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    Log.d("listreftrips", String.valueOf(currentTrip.getName()));
                    if (currentTrip.isReference()) {
                        mRefTripsList.add(currentTrip);
                    }
                }
                Log.d("listreftrips", String.valueOf(mRefTripsList.size()));
                mRecyclerAdapterRefTrip.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Failed to read value
                Log.d(TAG, "Failed to read value of a trip in RefTripsFargment", databaseError.toException());
            }
        });

    }

}
