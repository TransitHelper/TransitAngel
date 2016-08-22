package com.transitangel.transitangel.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;

import java.util.ArrayList;

/**
 * author yvastavaus.
 */
public class StationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_STATION_START = 1;
    private static final int ITEM_TYPE_STATION_MIDDLE = 2;
    private static final int ITEM_TYPE_STATION_END = 3;

    private ArrayList<Stop> stationStopItemList;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public StationsAdapter(Context context, ArrayList<Stop> stationStopItemList) {
        this.stationStopItemList = stationStopItemList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == ITEM_TYPE_STATION_MIDDLE) {
            View view = inflater.inflate(R.layout.item_stop_first, parent, false);
            viewHolder = new StationStopsViewHolder(view, onItemClickListener);
        } else if (viewType == ITEM_TYPE_STATION_START) {
            View view = inflater.inflate(R.layout.item_stop_first, parent, false);
            viewHolder = new StationStopsViewHolder(view, onItemClickListener);
        } else if (viewType == ITEM_TYPE_STATION_END) {
            View view = inflater.inflate(R.layout.item_stop_first, parent, false);
            viewHolder = new StationStopsViewHolder(view, onItemClickListener);
        }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_STATION_START;
        } else if (position == stationStopItemList.size() - 1) {
            return ITEM_TYPE_STATION_END;
        }
        // Default is middle
        return ITEM_TYPE_STATION_MIDDLE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StationStopsViewHolder viewHolder = (StationStopsViewHolder) holder;
        // FUTURE USE TO SETUP THE ICONS ON SEARCH
        switch (getItemViewType(position)) {
            case ITEM_TYPE_STATION_START:
                viewHolder.tvStopName.setText(stationStopItemList.get(position).getName());
                break;
            case ITEM_TYPE_STATION_END:
                viewHolder.tvStopName.setText(stationStopItemList.get(position).getName());
                break;
            case ITEM_TYPE_STATION_MIDDLE:
            default:
                viewHolder.tvStopName.setText(stationStopItemList.get(position).getName());
        }
    }


    @Override
    public int getItemCount() {
        return stationStopItemList.size();
    }
}
