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
public class RecentItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    @BindView(R.id.tvFrom)
    TextView tvFrom;
    @BindView(R.id.ivArrow)
    ImageView ivArrow;
    @BindView(R.id.tvTo)
    TextView tvTo;

    View parent;

    RecentAdapter.OnItemClickListener onItemClickListener;

    public RecentItemViewHolder(View itemView, RecentAdapter.OnItemClickListener onItemClickListener) {
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
