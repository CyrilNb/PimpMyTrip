package fr.univtln.cniobechoudayer.pimpmytrip.activities;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import fr.univtln.cniobechoudayer.pimpmytrip.controllers.UserController;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.ManagerTripFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.MapFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.ProfileFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.RefTripsFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.TripsFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.CircleTransform;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private Toolbar toolbar;
    private DrawerLayout drawer;

    private ImageView imageViewProfile;
    private TextView textViewPseudoUser;
    private TextView textViewStats;
    private StatisticsController statsController;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userController = UserController.getInstance();
        statsController = StatisticsController.getInstance();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if(imageViewProfile != null)
                    imageViewProfile.setImageBitmap(new CircleTransform().transform(userController.getConnectedUser().getConvertedPhoto()));
                if(userController.getConnectedUser() != null)
                    textViewPseudoUser.setText(userController.getConnectedUser().getPseudo());
                if(statsController.getUserStats() != null)
                    textViewStats.setText(String.valueOf(statsController.getUserStats().getNbTripsCreated()) + " " + getString(R.string.tripsLabel));

            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Listening to actions in the main menu (navigation drawer)
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**
         * Checking if user is a trip manager
         */
        if(!checkIfUserIsManager()){
            Menu navMenu = navigationView.getMenu();
            navMenu.findItem(R.id.managementTripsCateg).setVisible(false);
        }

        /**
         * Displaying default fragment once MainActivity is loaded
         */
        displayFragment(new MapFragment());

        /**
         * Getting header and loading picture
         */
        View header = navigationView.getHeaderView(0);
        imageViewProfile = (ImageView) header.findViewById(R.id.imageView);
        textViewPseudoUser = (TextView) header.findViewById(R.id.textViewPseudoUser);
        textViewStats = (TextView) header.findViewById(R.id.textViewStats);

        if(imageViewProfile != null)
            imageViewProfile.setImageBitmap(new CircleTransform().transform(userController.getConnectedUser().getConvertedPhoto()));
        if(userController.getConnectedUser() != null)
            textViewPseudoUser.setText(userController.getConnectedUser().getPseudo());
        if(statsController.getUserStats() != null)
            textViewStats.setText(String.valueOf(statsController.getUserStats().getNbTripsCreated()) + " " + getString(R.string.tripsLabel));
        /**
         * Setting listener to display from on nav header click
         */
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                displayFragment(ProfileFragment.getInstance());
            }
        });

    }


    /**
     * Method to get selected item in nav drawer
     * @param item selected
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int idSelectedItem = item.getItemId();

        switch(idSelectedItem){
            case R.id.tabMap:
                displayFragment(MapFragment.getInstance());
                break;
            case R.id.tabRefTrips:
                displayFragment(RefTripsFragment.getInstance());
                break;
            case R.id.tabMyTrips:
                displayFragment(TripsFragment.getInstance());
                break;
            case R.id.tabAR:
                //TODO
                break;
            case R.id.titleTripsManagement:
                displayFragment(ManagerTripFragment.getInstance());
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Method to handle hardware button, when pressed -> closing the nav drawer
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        UserController.getInstance().setUserAsDisconnected();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        UserController.getInstance().setUserAsDisconnected();
        super.onDestroy();
    }

    /**
     * When item of toolbar is selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to display a fragment in the mainContent
     * @param fragToDisplay
     */
    private void displayFragment(Fragment fragToDisplay){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.mainContent, fragToDisplay, fragToDisplay.getClass().getSimpleName()).commit();
    }

    /**
     * Methods that returns if the user is a manager or not
     * @return
     */
    private boolean checkIfUserIsManager(){
        return true;
        //TODO verifier si l'utilisateur qui se connecte est un manager
    }

}
