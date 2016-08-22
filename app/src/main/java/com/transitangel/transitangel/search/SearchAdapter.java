package com.transitangel.transitangel.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;

import java.util.ArrayList;

/**
 * @author yvastavaus.
 */
public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Stop> visibleStopsList;
    private ArrayList<Stop> allStopItemList;

    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public SearchAdapter(Context context, ArrayList<Stop> stationStopItemList) {
        this.allStopItemList = stationStopItemList;
        this.visibleStopsList = new ArrayList<>(allStopItemList);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_search_stops, parent, false);
        RecyclerView.ViewHolder viewHolder = new SearchViewHolder(view, onItemClickListener);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchViewHolder viewHolder = (SearchViewHolder) holder;
        viewHolder.tvStopName.setText(visibleStopsList.get(position).getName());
    }


    @Override
    public int getItemCount() {
        return visibleStopsList.size();
    }

    public void flushFilter(){
        visibleStopsList = new ArrayList<>(allStopItemList);
        notifyDataSetChanged();
    }

    public Stop getItem(int position) {
        return visibleStopsList.get(position);
    }

    public void setFilter(String queryText) {
        if(TextUtils.isEmpty(queryText)){
            visibleStopsList = new ArrayList<>(allStopItemList);
            return;
        }
        visibleStopsList = new ArrayList<>();
        for (Stop item: allStopItemList) {
            if (item.getName().toLowerCase().contains(queryText))
                visibleStopsList.add(item);
        }
        notifyDataSetChanged();
    }
}
