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
    public TextView tvTripTo;
    @BindView(R.id.ivIcon)
    public ImageView ivIcon;
    @BindView(R.id.tvTrainInfo)
    public TextView tvTrainInfo;
    @BindView(R.id.more)
    public ImageView more;

    public View parent;

    private final OnMoreMenuClickListener onMoreMenuClickListener;
    private final OnItemClickListener onItemClickListener;

    public RecentTripItemViewHolder(View itemView, OnItemClickListener onItemClickListener, OnMoreMenuClickListener onMoreMenuClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.parent = itemView;
        this.onItemClickListener = onItemClickListener;
        this.onMoreMenuClickListener = onMoreMenuClickListener;
        more.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.more) {
            onMoreMenuClickListener.onMenuItemClicked(getLayoutPosition(), view);
        } else {
            onItemClickListener.onItemClick(tvTrainInfo, getLayoutPosition());
        }
    }
}
