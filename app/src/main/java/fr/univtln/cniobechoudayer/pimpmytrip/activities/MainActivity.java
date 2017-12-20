package fr.univtln.cniobechoudayer.pimpmytrip.activities;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.AccountFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.ManagerTripFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.MapFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.ProfileFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.ReferenceTripsFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.TripsFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.opengl.OpenGLActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.services.ConnectedUserLocationService;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.CircleTransform;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private StatisticsController mStatsController;
    private UserController mUserController;

    private ImageView mImageViewProfile;
    private TextView mTextViewPseudoUser, mTextViewStats;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUserController = UserController.getsInstance();
        mStatsController = StatisticsController.getInstance();

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        /**
         * Handling the navigation mDrawer slide to display user data in real time
         */
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (mUserController != null) {
                    if (mImageViewProfile != null)
                        if (mUserController.getmConnectedUser().getConvertedPhoto() != null)
                            mImageViewProfile.setImageBitmap(new CircleTransform().transform(mUserController.getmConnectedUser().getConvertedPhoto()));
                    if (mUserController.getmConnectedUser() != null)
                        mTextViewPseudoUser.setText(mUserController.getmConnectedUser().getPseudo());
                    if (mStatsController.getUserStats() != null)
                        mTextViewStats.setText(String.valueOf(mStatsController.getUserStats().getNbTripsCreated()) + " " + getString(R.string.tripsLabel));
                }
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        //Listening to actions in the main menu (navigation mDrawer)
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        /**
         * Displaying default fragment once MainActivity is loaded
         */
        displayFragment(new MapFragment());

        /**
         * Getting header and loading picture
         */
        View header = mNavigationView.getHeaderView(0);
        mImageViewProfile = (ImageView) header.findViewById(R.id.imageView);
        mTextViewPseudoUser = (TextView) header.findViewById(R.id.textViewPseudoUser);
        mTextViewStats = (TextView) header.findViewById(R.id.textViewStats);

        if (mUserController.getmConnectedUser() != null) {
            if (mImageViewProfile != null)
                if (mUserController.getmConnectedUser().getConvertedPhoto() != null)
                    mImageViewProfile.setImageBitmap(new CircleTransform().transform(mUserController.getmConnectedUser().getConvertedPhoto()));
            if (mUserController.getmConnectedUser() != null)
                mTextViewPseudoUser.setText(mUserController.getmConnectedUser().getPseudo());
            if (mStatsController.getUserStats() != null)
                mTextViewStats.setText(String.valueOf(mStatsController.getUserStats().getNbTripsCreated()) + " " + getString(R.string.tripsLabel));

            /**
             * Checking if user is a trip manager
             */
            if (!checkIfUserIsManager()) {
                Menu navMenu = mNavigationView.getMenu();
                navMenu.findItem(R.id.managementTripsCateg).setVisible(false);
            }

        }


        /**
         * Setting listener to display from on nav header click
         */
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(GravityCompat.START);
                displayFragment(ProfileFragment.getInstance());
            }
        });

    }


    /**
     * Method to get selected item in nav mDrawer
     *
     * @param item selected
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int idSelectedItem = item.getItemId();

        switch (idSelectedItem) {
            case R.id.tabMap:
                displayFragment(MapFragment.getInstance());
                break;
            case R.id.tabRefTrips:
                displayFragment(ReferenceTripsFragment.getInstance());
                break;
            case R.id.tabMyTrips:
                displayFragment(TripsFragment.getInstance());
                break;
            case R.id.tabAR:
                Intent ARintent = new Intent(this, OpenGLActivity.class);
                startActivity(ARintent);
                break;
            case R.id.titleTripsManagement:
                displayFragment(ManagerTripFragment.getInstance());
                break;
            case R.id.titleSettings:
                displayFragment(AccountFragment.getInstance());
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Method to handle hardware button, when pressed -> closing the nav mDrawer
     */
    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        UserController.getsInstance().setUserAsDisconnected();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        UserController.getsInstance().setUserAsDisconnected();
        stopService(new Intent(this, ConnectedUserLocationService.class));
        super.onDestroy();
    }

    /**
     * When item of mToolbar is selected
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to display a fragment in the mainContent
     *
     * @param fragToDisplay
     */
    private void displayFragment(Fragment fragToDisplay) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.mainContent, fragToDisplay, fragToDisplay.getClass().getSimpleName()).commit();
    }

    /**
     * Methods that returns if the user is a manager or not
     *
     * @return
     */
    private boolean checkIfUserIsManager() {
        if (mUserController.getmConnectedUser() != null) {
            Log.d("getmConnectedUser", "isNotNull");
            if (mUserController.getmConnectedUser().isManager())
                return true;
            else
                return false;
        } else {
            return false;
        }
    }

}
