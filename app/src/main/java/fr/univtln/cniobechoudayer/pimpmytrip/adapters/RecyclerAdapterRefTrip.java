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

import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.R;

/**
 * Adapter to get and display data in recycler view for references trips
 */
public class RecyclerAdapterRefTrip extends RecyclerView.Adapter<RecyclerAdapterRefTrip.MyHolder> {

    private List<Trip> mListRefTrips;
    private Context mContext;

    /**
     * Constructor using list and mContext
     * @param list
     * @param context
     */
    public RecyclerAdapterRefTrip(List<Trip> list, Context context) {
        this.mListRefTrips = list;
        this.mContext = context;
    }

    /**
     * Inflates the view with a custom layout for cardviews
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_reftrip, parent, false);
        return new MyHolder(view);
    }

    /**
     * Load data for specific card view
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Trip trip = mListRefTrips.get(position);
        holder.nameRefTrip.setText(trip.getName());
        String distanceToDisplay = Double.toString(trip.getDistance()) + " kms";
        holder.distanceRefTrip.setText(distanceToDisplay);
        holder.imgView.setBackgroundColor(Color.parseColor(trip.getColor()));
    }

    /**
     * Get number of items in passed list
     * @return
     */
    @Override
    public int getItemCount() {
        if(mListRefTrips == null){
            return 0;
        }else
            return mListRefTrips.size();
    }

    /**
     * Retrieving graphic elements from the view
     */
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
