package com.transitangel.transitangel.alltrips;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.home.OnItemClickListener;
import com.transitangel.transitangel.home.OnMoreMenuClickListener;
import com.transitangel.transitangel.home.RecentHeaderViewHolder;
import com.transitangel.transitangel.home.RecentTripItemViewHolder;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.List;

/**
 * author yvastavaus.
 */
public class SeeAllTripsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int RECENT_TRIP_ITEM_TYPE = 1;
    public static final int RECENT_TRIP_ITEM_HEADER_TYPE = 2;

    private int RECENT_TRIP_LIST;
    private int RECENT_TRIP_HEADER;

    private List<Trip> recentTripItemList;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private OnMoreMenuClickListener onMoreMenuClickListener;

    public SeeAllTripsAdapter(Context context, @NonNull List<Trip> recentTripItemList){
        this.recentTripItemList = recentTripItemList;
        updateTotalCounts();
        this.context = context;
    }

    public int getTripsCount() {
        return RECENT_TRIP_HEADER + RECENT_TRIP_LIST;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnMoreMenuClickListener(OnMoreMenuClickListener onMoreMenuClickListener) {
        this.onMoreMenuClickListener = onMoreMenuClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;
        if(viewType == RECENT_TRIP_ITEM_HEADER_TYPE) {
            View view = inflater.inflate(R.layout.item_recent_header, parent, false);
            viewHolder = new RecentHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_recents_trip, parent, false);
            viewHolder = new RecentTripItemViewHolder(view, onItemClickListener, onMoreMenuClickListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((RecentTripItemViewHolder)viewHolder).tvTrainInfo.setTransitionName(context.getString(R.string.transition_details));
            }
        }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return RECENT_TRIP_ITEM_HEADER_TYPE;
        } else {
            return RECENT_TRIP_ITEM_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if(viewType == RECENT_TRIP_ITEM_HEADER_TYPE) {
            RecentHeaderViewHolder headerViewHolder = (RecentHeaderViewHolder) holder;
            headerViewHolder.header.setText((context.getString(R.string.recent_trips)));
            headerViewHolder.parent.setContentDescription(context.getString(R.string.content_description_recent_trip_header));
        } else  if(viewType == RECENT_TRIP_ITEM_TYPE) {
            RecentTripItemViewHolder recentItemViewHolder = (RecentTripItemViewHolder)holder;
            position = getRecentTripPosition(position);
            Trip currentTrip = recentTripItemList.get(position);
            Stop fromStop = currentTrip.getFromStop();
            Stop toStop = currentTrip.getToStop();
            Train selectedTrain = currentTrip.getSelectedTrain();
            recentItemViewHolder.tvTripTo.setText(context.getString(R.string.trip_from_to, fromStop.getName(), toStop.getName()));

            recentItemViewHolder.parent.setContentDescription(context.getString(R.string.contentdescription_recent_trip, fromStop.getName(), toStop.getName(),selectedTrain.getNumber(), selectedTrain.getTrainStop(fromStop.getId()).getArrrivalTime()));
            if(currentTrip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                recentItemViewHolder.tvTrainInfo.setText(context.getString(R.string.trip_train_without_number, selectedTrain.getTrainStop(fromStop.getId()).getArrrivalTime()));
                recentItemViewHolder.ivIcon.setImageResource(R.drawable.train_blue);
            } else {
                recentItemViewHolder.tvTrainInfo.setText(context.getString(R.string.trip_train_number, selectedTrain.getNumber(), selectedTrain.getTrainStop(fromStop.getId()).getArrrivalTime()));
                recentItemViewHolder.ivIcon.setImageResource(R.drawable.train_red);
            }
        }
    }

    public int getRecentTripPosition(int position) {
        // Subtract the header
        position = position -1;
        return position;
    }

    @Override
    public int getItemCount() {
        int total = RECENT_TRIP_HEADER + RECENT_TRIP_LIST;
        return total;
     }


    public void updateData(@NonNull List<Trip> recentTripItemList) {
        this.recentTripItemList = recentTripItemList;
        updateTotalCounts();
        notifyDataSetChanged();
    }

    private void updateTotalCounts() {
        RECENT_TRIP_HEADER = recentTripItemList.size() != 0 ? 1 : 0;
        RECENT_TRIP_LIST = recentTripItemList.size();
    }
}
