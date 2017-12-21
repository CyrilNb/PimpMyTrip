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

import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerViewAdapterReferenceTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.callbacks.SimpleItemTouchHelperCallback;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * Fragment representing the Trip Fragment
 */
public class TripsFragment extends Fragment {

    private static TripsFragment sInstance;
    private List<Trip> mTripsList;
    private RecyclerView mRecyclerView;
    private RecyclerAdapterTrip mAdapterTrip;
    private FragmentManager mFragmentManager;

    private ValueEventListener mListenerDbMyTrips;

    private final DatabaseReference fDbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private final DatabaseReference fDbMyTrips = (DatabaseReference) fDbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    /**
     * Constructor
     */
    public TripsFragment() {
        // Required empty public constructor
    }

    /**
     * Get singleton instance
     *
     * @return
     */
    public static TripsFragment getInstance() {
        if (sInstance == null) {
            sInstance = new TripsFragment();
        }

        return sInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTripsList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewTrips);
        mAdapterTrip = new RecyclerAdapterTrip(mTripsList, getActivity(), mFragmentManager);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapterTrip);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapterTrip, mRecyclerView);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mListenerDbMyTrips = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTripsList.clear();
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    if (currentTrip != null) {
                        currentTrip.setId(tripSnapshot.getKey());
                        if (!currentTrip.isReference())
                            mTripsList.add(currentTrip);
                    }
                }
                mAdapterTrip.notifyDataSetChanged();
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
        fDbMyTrips.addValueEventListener(mListenerDbMyTrips);
    }

    @Override
    public void onStop() {
        super.onStop();
        fDbMyTrips.removeEventListener(mListenerDbMyTrips);
    }
}
