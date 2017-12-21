package fr.univtln.cniobechoudayer.pimpmytrip.adapters;

import android.content.Context;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.MapFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.interfaces.ItemTouchHelperAdaptable;

public class RecyclerAdapterTrip extends RecyclerView.Adapter<RecyclerAdapterTrip.MyHolder> implements ItemTouchHelperAdaptable {

    private Context mContext;
    private TripController mTripController;

    private List<Trip> mListTrips;

    private Trip mSwipedTrip;
    private FragmentManager mFragmentManager;
    private MapFragment mMapFragment;

    /**
     * Constructor for recycler adapter
     * @param list
     * @param context
     * @param fragmentManager
     */
    public RecyclerAdapterTrip(List<Trip> list, Context context, android.support.v4.app.FragmentManager fragmentManager) {
        this.mListTrips = list;
        this.mContext = context;
        this.mFragmentManager = fragmentManager;
        mTripController = TripController.getInstance();
        mMapFragment = MapFragment.getInstance();
    }

    /**
     * Inflating the view with a custom card view layout
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_trip, parent, false);
        return new MyHolder(view);
    }

    /**
     * Binding the cardview with data
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Trip trip = mListTrips.get(position);
        holder.nameTrip.setText(trip.getName());
        holder.distanceTrip.setText(Utils.formatTripDistance(trip.getDistance()));
        if(trip.getColor() != null)
            holder.imgView.setBackgroundColor(Color.parseColor(trip.getColor()));
        else{
            Log.d("listreftrips","trip.color is null");
        }
    }

    /**
     * Get total number of items in passed list
     * @return
     */
    @Override
    public int getItemCount() {
        if (mListTrips == null) {
            return 0;
        } else
            return mListTrips.size();
    }

    public void addTrip(List<Trip> list) {
        mListTrips.addAll(0, list);
        notifyDataSetChanged();
    }

    /**
     * Method that handles gesture of user
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then resolved position of the moved item.
     *
     * @return
     */
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mListTrips, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mListTrips, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    /**
     * Method that handles delete of an item
     * @param position The position of the item dismissed.
     *
     */
    @Override
    public void onItemDismiss(final int position) {
        mSwipedTrip = mListTrips.get(position);
        mListTrips.remove(mSwipedTrip);
        notifyItemRemoved(position);
        mTripController.deleteTrip(mSwipedTrip);
    }

    /**
     * To reinsert a previously deleted item
     * @param position
     */
    public void restoreItem(final int position){
        mListTrips.add(position, mSwipedTrip);
        notifyItemInserted(position);
        mTripController.insertTrip(mSwipedTrip);
    }

    /**
     * Method to display the map fragment with selected trips displayed
     */
    public void displaySelectedTripOnMap(final int position){
        try{
            mSwipedTrip = mListTrips.get(position);
            ArrayList<Trip> listTripToBePassed = new ArrayList<>(); //Declared as ArrayList because of putParcelableArrayList() method
            listTripToBePassed.add(mSwipedTrip);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("listSwipedTrips",listTripToBePassed);
            mMapFragment.setArguments(bundle);
            mFragmentManager.beginTransaction().replace(R.id.mainContent, mMapFragment, MapFragment.getInstance().getClass().getSimpleName()).commit();

        } catch (ClassCastException e) {
            Log.d("fragment", "Can't get the fragment manager with this");
        }

    }

    /**
     * Retrieving graphic elements from layout view
     */
    public class MyHolder extends RecyclerView.ViewHolder {
        TextView nameTrip, distanceTrip;
        ImageView imgView;
        public LinearLayout foreground, background;

        private MyHolder(final View itemView) {
            super(itemView);
            nameTrip = (TextView) itemView.findViewById(R.id.txtView_name_trip);
            distanceTrip = (TextView) itemView.findViewById(R.id.txtView_distance_trip);
            imgView = (ImageView) itemView.findViewById(R.id.thumbnail_cardview_trip);
            foreground = (LinearLayout) itemView.findViewById(R.id.view_foreground);

        }
    }

}
