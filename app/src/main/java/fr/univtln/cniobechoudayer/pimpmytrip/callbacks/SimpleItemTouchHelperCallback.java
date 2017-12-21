package fr.univtln.cniobechoudayer.pimpmytrip.callbacks;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerViewAdapterReferenceTrip;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    public static final float ALPHA_FULL = 1.0f;

    private final RecyclerAdapterTrip fRecyclerAdapterTrip;
    private final RecyclerView fRecyclerView;
    private View mItemView;
    //private final Bitmap iconDelete, iconDisplay;


    public SimpleItemTouchHelperCallback(RecyclerAdapterTrip adapter, RecyclerView recyclerView) {
        this.fRecyclerAdapterTrip = adapter;
        this.fRecyclerView = recyclerView;
        //this.iconDelete = iconDelete;
        //this.iconDisplay = iconDelete;

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        fRecyclerAdapterTrip.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();

        if (direction == ItemTouchHelper.END) {
            fRecyclerAdapterTrip.displaySelectedTripOnMap(position);
        }
        if (direction == ItemTouchHelper.START) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Snackbar snackbar = make(fRecyclerView, "Trip removed from database", LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        fRecyclerAdapterTrip.restoreItem(position);

                        Snackbar snackbarUndo = make(view, "Trip is restored!", Snackbar.LENGTH_SHORT);
                        snackbarUndo.show();
                    }
                });
                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(fRecyclerView.getContext().getResources().getColor(R.color.colorPrimary));
                snackbar.show();

            }
            fRecyclerAdapterTrip.onItemDismiss(position);

        }

    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            mItemView = viewHolder.itemView;

            Paint p = new Paint();
            if (dX > 0) {
                p.setARGB(100, 51, 153, 255);
                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) mItemView.getLeft(), (float) mItemView.getTop(), dX,
                        (float) mItemView.getBottom(), p);
            } else {
                p.setARGB(150, 204, 0, 0);
                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                c.drawRect((float) mItemView.getRight() + dX, (float) mItemView.getTop(),
                        (float) mItemView.getRight(), (float) mItemView.getBottom(), p);
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    }

    /*
    @Override
    public void onChildDraw(Canvas c, RecyclerView fRecyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((RecyclerAdapterTrip.MyHolder) viewHolder).foreground;
        getDefaultUIUtil().onDraw(c, fRecyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder != null){
            final View foregroundView = ((RecyclerAdapterTrip.MyHolder) viewHolder).background;
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }*/



}
