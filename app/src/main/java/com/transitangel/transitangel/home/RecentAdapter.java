package com.transitangel.transitangel.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Trip;

import java.util.List;

/**
 * author yvastavaus.
 */
public class RecentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int RECENT_ITEM_TYPE = 1;

    private List<Trip> recentsItemList;
    private Context context;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public RecentAdapter(Context context, List<Trip> recentsItemList) {
        this.recentsItemList = recentsItemList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        if(viewType == RECENT_ITEM_TYPE) {
            View view = inflater.inflate(R.layout.item_recent_item, parent, false);
            viewHolder = new RecentItemViewHolder(view, onItemClickListener);
        }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        // TODO: Future use that will support more types.
        return RECENT_ITEM_TYPE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecentItemViewHolder) {
            RecentItemViewHolder recentItemViewHolder = (RecentItemViewHolder)holder;
            recentItemViewHolder.tvFrom.setText(recentsItemList.get(position).getFromStop().getName());
            recentItemViewHolder.tvTo.setText(recentsItemList.get(position).getToStop().getName());
            // Setting up content description for accessibility.
            recentItemViewHolder.parent.setContentDescription(context.getString(R.string.contentdescription_from_to, recentsItemList.get(position).getFromStop().getName(), recentsItemList.get(position).getToStop().getName()));
        }
    }

    @Override
    public int getItemCount() {
        if ( recentsItemList != null ) {
            return recentsItemList.size();
        }
        return 0;
     }
}
