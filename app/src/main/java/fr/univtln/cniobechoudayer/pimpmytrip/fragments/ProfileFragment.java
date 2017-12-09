package fr.univtln.cniobechoudayer.pimpmytrip.fragments;


import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import fr.univtln.cniobechoudayer.pimpmytrip.R;


public class ProfileFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener{

    private ImageView mProfileImage;
    private int mMaxScrollSize;
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;

    private static ProfileFragment singleton;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment getInstance() {
        if(singleton == null){
            singleton = new ProfileFragment();

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

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        //mProfileImage = (ImageView) rootView.findViewById(R.id.profilePicture);

        //Picasso.with(getContext()).load(R.drawable.test_profile).transform(new CircleTransform()).into(mProfileImage);
        // Inflate the layout for this fragment

        AppBarLayout appbarLayout = (AppBarLayout) rootView.findViewById(R.id.materialup_appbar);
        mProfileImage = (ImageView) rootView.findViewById(R.id.materialup_profile_image);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.materialup_toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    //TODO BACK
                }
            });
        }

        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        return rootView;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            mProfileImage.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            mProfileImage.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }



}
