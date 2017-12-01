package fr.univtln.cniobechoudayer.pimpmytrip.callbacks;

import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import fr.univtln.cniobechoudayer.pimpmytrip.R;
import fr.univtln.cniobechoudayer.pimpmytrip.adapters.RecyclerAdapterTrip;
import fr.univtln.cniobechoudayer.pimpmytrip.interfaces.ItemTouchHelperAdapter;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.make;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final RecyclerAdapterTrip recyclerAdapterTrip;
    private final RecyclerView recyclerView;


    public SimpleItemTouchHelperCallback(RecyclerAdapterTrip adapter,RecyclerView recyclerView) {
        this.recyclerAdapterTrip = adapter;
        this.recyclerView = recyclerView;

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
        recyclerAdapterTrip.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Snackbar snackbar = make(recyclerView, "Trip removed from database", LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                    System.out.println("pos to insert: "+ position);
                    recyclerAdapterTrip.onItemUndoDismiss(position);

                    Snackbar snackbarUndo = make(view, "Trip is restored!", Snackbar.LENGTH_SHORT);
                    snackbarUndo.show();
                }
            });
            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(recyclerView.getContext().getResources().getColor(R.color.colorPrimary));
            snackbar.show();

        }
        System.out.println("pos to delete: "+position);
        recyclerAdapterTrip.onItemDismiss(position);
    }
}
