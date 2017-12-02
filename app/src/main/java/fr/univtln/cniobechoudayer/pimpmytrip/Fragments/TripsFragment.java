package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Activities.MainActivity;
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
    private List<Trip> tripsList;
    private RecyclerView recyclerView;
    private RecyclerAdapterTrip adapterTrip;
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
        adapterTrip = new RecyclerAdapterTrip(tripsList, getActivity(), fragmentManager);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterTrip);

        //For drag and swipe with recyclerView
        //Bitmap iconDelete = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_delete_white_48dp);
        //Bitmap iconDisplay = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_;

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

        return rootView;
    }

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
