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
import fr.univtln.cniobechoudayer.pimpmytrip.callbacks.SimpleItemTouchHelperCallback;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RefTripsFragment newInstance} factory method to
 * create an instance of this fragment.
 */
public class RefTripsFragment extends Fragment {

    private List<Trip> mRefTripsList;

    private static RefTripsFragment sInstance;
    private RecyclerView mRecyclerView;
    private RecyclerAdapterTrip mRecyclerAdapterRefTrip;
    private FragmentManager mFragmentManager;

    private ValueEventListener mListenerDbRefTrips;

    private final DatabaseReference fDbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");
    private final DatabaseReference fDbMyRefTrips = (DatabaseReference) fDbTrips.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    /**
     * Constructor
     */
    public RefTripsFragment() {
        // Required empty public constructor
    }

    /**
     * Get singleton instance
     *
     * @return singleton ReftripsFragment
     */
    public static RefTripsFragment getInstance() {
        if (sInstance == null) {
            sInstance = new RefTripsFragment();
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
        mRefTripsList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_trips, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerViewTrips);
        mRecyclerAdapterRefTrip = new RecyclerAdapterTrip(mRefTripsList, getActivity(), mFragmentManager);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRecyclerAdapterRefTrip);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mRecyclerAdapterRefTrip, mRecyclerView);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mListenerDbRefTrips = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRefTripsList.clear();
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip currentTrip = tripSnapshot.getValue(Trip.class);
                    if (currentTrip != null) {
                        currentTrip.setId(tripSnapshot.getKey());
                        if (currentTrip.isReference()) {
                            mRefTripsList.add(currentTrip);
                        }
                    }
                }
                mRecyclerAdapterRefTrip.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), getString(R.string.titleRefTrips));

        return rootView;
    }

    /**
     * Setting up mDatabase listenersæ
     * when fragment starts
     */
    @Override
    public void onStart() {
        super.onStart();
        fDbMyRefTrips.addValueEventListener(mListenerDbRefTrips);
    }

    @Override
    public void onStop() {
        super.onStop();
        fDbMyRefTrips.removeEventListener(mListenerDbRefTrips);
    }

}
