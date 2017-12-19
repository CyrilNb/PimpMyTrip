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
import fr.univtln.cniobechoudayer.pimpmytrip.interfaces.ItemTouchHelperAdaptable;
import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;


/**
 * A custom adapter to use with the RecyclerView widget.
 */
public class RecyclerViewAdapterReferenceTrip extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdaptable {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context mContext;
    private TripController mTripController;
    private MapFragment mMapFragment;

    private List<Trip> mModelList;

    private String mHeaderTitle;
    private Trip mSwipedTrip;
    private OnHeaderClickListener mHeaderClickListener;
    private FragmentManager mFragmentManager;

    private OnItemClickListener mItemClickListener;

    public RecyclerViewAdapterReferenceTrip(Context context, List<Trip> modelList, String headerTitle, FragmentManager fragmentManager) {
        this.mContext = context;
        this.mModelList = modelList;
        this.mHeaderTitle = headerTitle;
        this.mFragmentManager = fragmentManager;
        mTripController = TripController.getInstance();
        mMapFragment = MapFragment.getInstance();
    }

    public void updateList(List<Trip> modelList) {
        this.mModelList = modelList;
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
            genericViewHolder.mItemTxtTitle.setText(model.getName());
            genericViewHolder.mItemTxtDistance.setText(Utils.formatTripDistance(model.getDistance()));
            if(model.getColor() != null)
                ((ViewHolder) holder).mImgColorItem.setBackgroundColor(Color.parseColor(model.getColor()));
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

        return mModelList.size() + 1;
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void SetOnHeaderClickListener(final OnHeaderClickListener headerClickListener) {
        this.mHeaderClickListener = headerClickListener;
    }

    private Trip getItem(int position) {
        return mModelList.get(position);
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
                Collections.swap(mModelList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mModelList, i, i - 1);
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
        mSwipedTrip = mModelList.get(position);
        mModelList.remove(mSwipedTrip);
        notifyItemRemoved(position);
        mTripController.deleteTrip(mSwipedTrip);
    }

    /**
     * To reinsert a previously deleted item
     * @param position
     */
    public void restoreItem(final int position){
        mModelList.add(position, mSwipedTrip);
        notifyItemInserted(position);
        mTripController.insertTrip(mSwipedTrip);
    }

    /**
     * Method to display a the map fragment with selected trips displayed
     */
    public void displaySelectedTripOnMap(final int position){
        try{
            mSwipedTrip = mModelList.get(position);
            ArrayList<Trip> listTripToBePassed = new ArrayList<>(); //Declare as ArrayList because of putParcelableArrayList() method
            listTripToBePassed.add(mSwipedTrip);
            System.out.println("swipedlist from recycler: "+listTripToBePassed.get(0).getName() );
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("listSwipedTrips",listTripToBePassed);
            mMapFragment.setArguments(bundle);
            mFragmentManager.beginTransaction().replace(R.id.mainContent, mMapFragment, MapFragment.getInstance().getClass().getSimpleName()).commit();

        } catch (ClassCastException e) {
            Log.d("fragment", "Can't get the fragment manager with this");
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImgColorItem;
        private TextView mItemTxtTitle;
        private TextView mItemTxtDistance;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.mImgColorItem = (ImageView) itemView.findViewById(R.id.img_user);
            this.mItemTxtTitle = (TextView) itemView.findViewById(R.id.item_txt_title);
            this.mItemTxtDistance = (TextView) itemView.findViewById(R.id.item_txt_distance);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mItemClickListener.onItemClick(itemView, getAdapterPosition(), mModelList.get(getAdapterPosition() - 1));

                }
            });

        }
    }

}

