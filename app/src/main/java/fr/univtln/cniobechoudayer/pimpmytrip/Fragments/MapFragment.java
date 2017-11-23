package fr.univtln.cniobechoudayer.pimpmytrip.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.univtln.cniobechoudayer.pimpmytrip.R;


public class MapFragment extends Fragment {

    public static MapFragment singleton;

    public MapFragment() {
        // Required empty public constructor
    }


    public static MapFragment getInstance() {

        if(singleton == null){
            singleton = new MapFragment();
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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

}
