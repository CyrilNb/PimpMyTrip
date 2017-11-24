package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.univtln.cniobechoudayer.pimpmytrip.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RefTripsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RefTripsFragment extends Fragment {

    private static RefTripsFragment singleton;

    public RefTripsFragment() {
        // Required empty public constructor
    }


    public static RefTripsFragment getInstance() {
        if(singleton == null){
            singleton = new RefTripsFragment();
        }

        return singleton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ref_trips, container, false);
    }

}
