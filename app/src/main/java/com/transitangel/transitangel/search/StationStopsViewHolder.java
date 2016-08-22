package com.transitangel.transitangel.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author yvastavaus.
 */
public class StationStopsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    @BindView(R.id.vIcon)
    View vIcon;
    @BindView(R.id.tvStopName)
    TextView tvStopName;
    @BindView(R.id.tvStopTime)
    TextView tvStopTime;
    @BindView(R.id.ivNotificationIcon)
    ImageView ivNotificationIcon;

    View parent;

    StationsAdapter.OnItemClickListener onItemClickListener;

    public StationStopsViewHolder(View itemView, StationsAdapter.OnItemClickListener onItemClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.parent = itemView;
        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        onItemClickListener.onItemClick(getLayoutPosition());
    }
}
