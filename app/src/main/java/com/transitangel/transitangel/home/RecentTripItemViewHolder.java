package com.transitangel.transitangel.home;

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
public class RecentTripItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    @BindView(R.id.tvTripTo)
    TextView tvTripTo;
    @BindView(R.id.ivIcon)
    ImageView ivIcon;
    @BindView(R.id.tvTrainInfo)
    TextView tvTrainInfo;

    View parent;

    RecentAdapter.OnItemClickListener onItemClickListener;

    public RecentTripItemViewHolder(View itemView, RecentAdapter.OnItemClickListener onItemClickListener) {
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
