package fr.univtln.cniobechoudayer.pimpmytrip.fragments;


import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;

import android.support.v4.app.Fragment;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerViewAdapterReferenceTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.callbacks.SimpleItemTouchHelperCallback;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

import android.widget.Toast;

import android.view.Menu;
import android.support.v7.widget.SearchView;
import android.support.v4.view.MenuItemCompat;
import android.app.SearchManager;
import android.widget.EditText;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.Spanned;


import android.view.ViewGroup;
import android.view.MenuInflater;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReferenceTripsFragment#getInstance} factory method to
 * create an instance of this fragment.
 */


public class ReferenceTripsFragment extends Fragment {

    private static ReferenceTripsFragment sInstance;

    private List<Trip> mModelList;

    private RecyclerView mRecyclerView;
    private RecyclerViewAdapterReferenceTrip mAdapter;
    private ValueEventListener mListenerDbReferenceTrips;
    private FragmentManager mFragmentManager;

    private final DatabaseReference fDbTrips = FirebaseDatabase.getInstance().getReference("PimpMyTripDatabase").child("trips");

    public ReferenceTripsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReferenceTripsFragment.
     */
    public static ReferenceTripsFragment getInstance() {
        if (sInstance == null) {
            sInstance = new ReferenceTripsFragment();
        }

        return sInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getActivity().getSupportFragmentManager();
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mModelList = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_reference_trips, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mAdapter = new RecyclerViewAdapterReferenceTrip(getActivity(), mModelList, "REFERENCE TRIPS", mFragmentManager);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter, mRecyclerView);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        mListenerDbReferenceTrips = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mModelList.clear();
                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot : tripSnapshot.getChildren()) {
                        Trip currentTrip = snapshot.getValue(Trip.class);
                        System.out.println(currentTrip.getName());
                        currentTrip.setId(snapshot.getKey());
                        if (!currentTrip.isReference())
                            mModelList.add(currentTrip);
                    }

                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mAdapter.SetOnItemClickListener(new RecyclerViewAdapterReferenceTrip.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Trip model) {

                //handle item click events here
                Toast.makeText(getActivity(), "Hey " + model.getName(), Toast.LENGTH_SHORT).show();

            }
        });

        mAdapter.SetOnHeaderClickListener(new RecyclerViewAdapterReferenceTrip.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View view, String headerTitle) {
                //handle item click events here
                Toast.makeText(getActivity(), "REFERENCES TRIPS", Toast.LENGTH_SHORT).show();

            }
        });

        /**
         * Setting up fragment title
         */
        Utils.setActionBarTitle((AppCompatActivity) getActivity(), "Reference Trips");

        return rootView;

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(menu.findItem(R.id.action_search));

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        //changing edittext color
        EditText searchEdit = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        searchEdit.setTextColor(Color.WHITE);
        searchEdit.setHintTextColor(Color.WHITE);
        searchEdit.setBackgroundColor(Color.TRANSPARENT);
        searchEdit.setHint("Search");

        InputFilter[] fArray = new InputFilter[2];
        fArray[0] = new InputFilter.LengthFilter(40);
        fArray[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                for (int i = start; i < end; i++) {

                    if (!Character.isLetterOrDigit(source.charAt(i)))
                        return "";
                }

                return null;

            }
        };
        searchEdit.setFilters(fArray);
        View v = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        v.setBackgroundColor(Color.TRANSPARENT);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ArrayList<Trip> filterList = new ArrayList<>();
                if (s.length() > 0) {
                    for (int i = 0; i < mModelList.size(); i++) {
                        if (mModelList.get(i).getName().toLowerCase().contains(s.toString().toLowerCase())) {
                            filterList.add(mModelList.get(i));
                            mAdapter.updateList(filterList);
                        }
                    }

                } else {
                    mAdapter.updateList(mModelList);
                }
                return false;
            }
        });

    }


    /**
     * Handling life cycle methods
     */

    @Override
    public void onStart() {
        super.onStart();
        if (mListenerDbReferenceTrips != null)
            fDbTrips.addValueEventListener(mListenerDbReferenceTrips);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mListenerDbReferenceTrips != null)
            fDbTrips.removeEventListener(mListenerDbReferenceTrips);
    }
}

