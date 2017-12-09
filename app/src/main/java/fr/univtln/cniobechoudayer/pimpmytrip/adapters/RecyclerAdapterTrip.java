package fr.univtln.cniobechoudayer.pimpmytrip.adapters;

import android.content.Context;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.Fragments.MapFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.Utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.interfaces.ItemTouchHelperAdapter;

public class RecyclerAdapterTrip extends RecyclerView.Adapter<RecyclerAdapterTrip.MyHolder> implements ItemTouchHelperAdapter {
    private List<Trip> listTrips;
    private Context context;
    private Trip swipedTrip;
    private android.support.v4.app.FragmentManager fragmentManager;
    private TripController tripController;
    private MapFragment mapFragment;

    public RecyclerAdapterTrip(List<Trip> list, Context context, android.support.v4.app.FragmentManager fragmentManager) {
        this.listTrips = list;
        this.context = context;
        this.fragmentManager = fragmentManager;
        tripController = TripController.getInstance();
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_trip, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Trip trip = listTrips.get(position);
        holder.nameTrip.setText(trip.getName());
        holder.distanceTrip.setText(Utils.formatedTripDistance(trip.getDistance()));
        if(trip.getColor() != null)
            holder.imgView.setBackgroundColor(Color.parseColor(trip.getColor()));
        else{
            Log.d("listreftrips","trip.color is null");
        }
    }

    @Override
    public int getItemCount() {
        if (listTrips == null) {
            return 0;
        } else
            return listTrips.size();
    }

    public void addTrip(List<Trip> list) {
        listTrips.addAll(0, list);
        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(listTrips, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(listTrips, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
        swipedTrip = listTrips.get(position);
        listTrips.remove(swipedTrip);
        notifyItemRemoved(position);
        tripController.deleteTrip(swipedTrip);
    }

    public void restoreItem(final int position){
        listTrips.add(position, swipedTrip);
        notifyItemInserted(position);
        tripController.insertTrip(swipedTrip);
    }

    /**
     * Method to display a the map fragment with selected trips displayed
     */
    public void displaySelectedTripOnMap(final int position){
        try{
            swipedTrip = listTrips.get(position);
            ArrayList<Trip> listTripToBePassed = new ArrayList<>(); //Declare as ArrayList because of putParcelableArrayList() method
            listTripToBePassed.add(swipedTrip);
            System.out.println("swipedlist from recycler: "+listTripToBePassed.get(0).getName() );
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("listSwipedTrips",listTripToBePassed);

            MapFragment mapFragment = MapFragment.getInstance();
            mapFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.mainContent, mapFragment, MapFragment.getInstance().getClass().getSimpleName()).commit();

        } catch (ClassCastException e) {
            Log.d("fragment", "Can't get the fragment manager with this");
        }

    }

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
