package com.transitangel.transitangel.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrainStop;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author yvastavaus.
 */
public class StationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_STATION_START = 1;
    private static final int ITEM_TYPE_STATION_MIDDLE = 2;
    private static final int ITEM_TYPE_STATION_END = 3;

    private ArrayList<TrainStop> visibleStopsList;
    private ArrayList<TrainStop> allStopItemList;
    private HashMap<String, Stop> stopHashMap;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public StationsAdapter(Context context, ArrayList<TrainStop> stationStopItemList, HashMap<String, Stop> stopHashMap) {
        this.allStopItemList = stationStopItemList;
        this.visibleStopsList = new ArrayList<>(allStopItemList);
        this.stopHashMap = stopHashMap;
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
        } else if (position == visibleStopsList.size() - 1) {
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
                viewHolder.tvStopName.setText(stopHashMap.get(visibleStopsList.get(position).getStopId()).getName());
                viewHolder.tvStopTime.setText(visibleStopsList.get(position).getDepartureTime());
                break;
            case ITEM_TYPE_STATION_END:
                viewHolder.tvStopName.setText(stopHashMap.get(visibleStopsList.get(position).getStopId()).getName());
                viewHolder.tvStopTime.setText(visibleStopsList.get(position).getDepartureTime());
                break;
            case ITEM_TYPE_STATION_MIDDLE:
            default:
                viewHolder.tvStopName.setText(stopHashMap.get(visibleStopsList.get(position).getStopId()).getName());
                viewHolder.tvStopTime.setText(visibleStopsList.get(position).getDepartureTime());
        }
    }


    @Override
    public int getItemCount() {
        return visibleStopsList.size();
    }

    public TrainStop getItem(int position) {
        return visibleStopsList.get(position);
    }

    public void setFilter(String queryText) {
        if(TextUtils.isEmpty(queryText)){
            visibleStopsList = new ArrayList<>(allStopItemList);
            return;
        }
        visibleStopsList = new ArrayList<>();
        for (TrainStop item: allStopItemList) {
            if (stopHashMap.get(item.getStopId()).getName().toLowerCase().contains(queryText)) {
                visibleStopsList.add(item);
            }
        }
        notifyDataSetChanged();
    }
}
