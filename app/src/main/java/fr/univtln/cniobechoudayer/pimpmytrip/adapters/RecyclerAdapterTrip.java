package fr.univtln.cniobechoudayer.pimpmytrip.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Activities.MainActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.Authentication.LoginActivity;
import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.Utils.Utils;
import fr.univtln.cniobechoudayer.pimpmytrip.interfaces.ItemTouchHelperAdapter;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;

public class RecyclerAdapterTrip extends RecyclerView.Adapter<RecyclerAdapterTrip.MyHolder> implements ItemTouchHelperAdapter {

    List<Trip> listTrips;
    Context context;
    Trip tripToRemoved;

    public RecyclerAdapterTrip(List<Trip> list, Context context) {
        this.listTrips = list;
        this.context = context;
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
        String distanceToDisplay = Double.toString(trip.getDistanceInMeters()) + " kms";
        holder.distanceTrip.setText(distanceToDisplay);
        holder.imgView.setBackgroundColor(Color.parseColor(trip.getColor()));
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
        tripToRemoved = listTrips.get(position);
        listTrips.remove(tripToRemoved);
        notifyItemRemoved(position);
    }

    public void onItemUndoDismiss(final int position){
        listTrips.add(position,tripToRemoved);
        notifyItemInserted(position);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView nameTrip, distanceTrip;
        ImageView imgView;

        private MyHolder(View itemView) {
            super(itemView);
            nameTrip = (TextView) itemView.findViewById(R.id.txtView_name_trip);
            distanceTrip = (TextView) itemView.findViewById(R.id.txtView_distance_trip);
            imgView = (ImageView) itemView.findViewById(R.id.thumbnail_cardview_trip);
        }
    }

}
