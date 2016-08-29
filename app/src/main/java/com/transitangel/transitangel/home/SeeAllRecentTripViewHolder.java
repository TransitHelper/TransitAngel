package com.transitangel.transitangel.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * author yvastavaus.
 */
public class SeeAllRecentTripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    RecentAdapter.OnItemClickListener onItemClickListener;

    public SeeAllRecentTripViewHolder(View itemView, RecentAdapter.OnItemClickListener onItemClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onItemClickListener.onItemClick(getLayoutPosition());
    }
}
