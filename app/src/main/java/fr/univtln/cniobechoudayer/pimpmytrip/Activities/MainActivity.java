package fr.univtln.cniobechoudayer.pimpmytrip.Activities;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import fr.univtln.cniobechoudayer.pimpmytrip.Fragments.MapFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.Fragments.RefTripsFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Listening to actions in the main menu (navigation drawer)
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**
         * Displaying default fragment once MainActivity is loaded
         */
        displayFragment(new MapFragment());

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
                //TODO
                break;
            case R.id.tabAR:
                //TODO
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Method to handle hardware button, when pressed -> closing the nav drawer
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displayFragment(Fragment fragToDisplay){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.mainContent, fragToDisplay).commit();
    }
}
