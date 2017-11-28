package fr.univtln.cniobechoudayer.pimpmytrip.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.Entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;

public class RecyclerAdapterRefTrip extends RecyclerView.Adapter<RecyclerAdapterRefTrip.MyHolder> {

    List<Trip> listRefTrips;
    Context context;

    public RecyclerAdapterRefTrip(List<Trip> list, Context context) {
        this.listRefTrips = list;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_reftrip, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Trip trip = listRefTrips.get(position);
        holder.nameRefTrip.setText(trip.getName());
        String distanceToDisplay = Double.toString(trip.getDistanceInMeters()) + " kms";
        holder.distanceRefTrip.setText(distanceToDisplay);
        holder.imgView.setBackgroundColor(Color.parseColor(trip.getColor()));
    }

    @Override
    public int getItemCount() {
        if(listRefTrips == null){
            return 0;
        }else
            return listRefTrips.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView nameRefTrip, distanceRefTrip;
        ImageView imgView;

        public MyHolder(View itemView) {
            super(itemView);
            nameRefTrip = (TextView) itemView.findViewById(R.id.txtView_name_reftrip);
            distanceRefTrip = (TextView) itemView.findViewById(R.id.txtView_distance_reftrip);
            imgView = (ImageView) itemView.findViewById(R.id.thumbnail_cardview_reftrip);

        }
    }

}
