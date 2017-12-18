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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.fragments.MapFragment;
import fr.univtln.cniobechoudayer.pimpmytrip.interfaces.ItemTouchHelperAdapter;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;


/**
 * A custom adapter to use with the RecyclerView widget.
 */
public class RecyclerViewAdapterReferenceTrip extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private String mHeaderTitle;
    private Trip swipedTrip;
    private OnHeaderClickListener mHeaderClickListener;

    private Context mContext;
    private List<Trip> modelList;
    private TripController tripController;
    private MapFragment mapFragment;
    private android.support.v4.app.FragmentManager fragmentManager;


    private OnItemClickListener mItemClickListener;


    public RecyclerViewAdapterReferenceTrip(Context context, List<Trip> modelList, String headerTitle, FragmentManager fragmentManager) {
        this.mContext = context;
        this.modelList = modelList;
        this.mHeaderTitle = headerTitle;
        this.fragmentManager = fragmentManager;
        tripController = TripController.getInstance();
        mapFragment = MapFragment.getInstance();
    }

    public void updateList(List<Trip> modelList) {
        this.modelList = modelList;
        notifyDataSetChanged();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_header, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_trip, parent, false);
            return new ViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.txtTitleHeader.setText(mHeaderTitle);

        } else if (holder instanceof ViewHolder) {
            final Trip model = getItem(position - 1);
            ViewHolder genericViewHolder = (ViewHolder) holder;
            genericViewHolder.itemTxtTitle.setText(model.getName());
            genericViewHolder.itemTxtDistance.setText(Utils.formatTripDistance(model.getDistance()));
            if(model.getColor() != null)
                ((ViewHolder) holder).imgColorItem.setBackgroundColor(Color.parseColor(model.getColor()));
            else{
                Log.d("listreftrips","trip.color is null");
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    @Override
    public int getItemCount() {

        return modelList.size() + 1;
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void SetOnHeaderClickListener(final OnHeaderClickListener headerClickListener) {
        this.mHeaderClickListener = headerClickListener;
    }

    private Trip getItem(int position) {
        return modelList.get(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Trip model);
    }

    public interface OnHeaderClickListener {
        void onHeaderClick(View view, String headerTitle);
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitleHeader;

        public HeaderViewHolder(final View itemView) {
            super(itemView);
            this.txtTitleHeader = (TextView) itemView.findViewById(R.id.txt_header);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mHeaderClickListener.onHeaderClick(itemView, mHeaderTitle);
                }
            });

        }
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
                Collections.swap(modelList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(modelList, i, i - 1);
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
        swipedTrip = modelList.get(position);
        modelList.remove(swipedTrip);
        notifyItemRemoved(position);
        tripController.deleteTrip(swipedTrip);
    }

    /**
     * To reinsert a previously deleted item
     * @param position
     */
    public void restoreItem(final int position){
        modelList.add(position, swipedTrip);
        notifyItemInserted(position);
        tripController.insertTrip(swipedTrip);
    }

    /**
     * Method to display a the map fragment with selected trips displayed
     */
    public void displaySelectedTripOnMap(final int position){
        try{
            swipedTrip = modelList.get(position);
            ArrayList<Trip> listTripToBePassed = new ArrayList<>(); //Declare as ArrayList because of putParcelableArrayList() method
            listTripToBePassed.add(swipedTrip);
            System.out.println("swipedlist from recycler: "+listTripToBePassed.get(0).getName() );
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("listSwipedTrips",listTripToBePassed);
            mapFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.mainContent, mapFragment, MapFragment.getInstance().getClass().getSimpleName()).commit();

        } catch (ClassCastException e) {
            Log.d("fragment", "Can't get the fragment manager with this");
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgColorItem;
        private TextView itemTxtTitle;
        private TextView itemTxtDistance;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.imgColorItem = (ImageView) itemView.findViewById(R.id.img_user);
            this.itemTxtTitle = (TextView) itemView.findViewById(R.id.item_txt_title);
            this.itemTxtDistance = (TextView) itemView.findViewById(R.id.item_txt_distance);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mItemClickListener.onItemClick(itemView, getAdapterPosition(), modelList.get(getAdapterPosition() - 1));

                }
            });

        }
    }

}

