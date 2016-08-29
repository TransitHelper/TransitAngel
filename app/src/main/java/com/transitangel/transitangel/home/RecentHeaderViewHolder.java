package com.transitangel.transitangel.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.transitangel.transitangel.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author yvastavaus.
 */
public class RecentHeaderViewHolder extends RecyclerView.ViewHolder{
    @BindView(R.id.header)
    TextView header;

    public RecentHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
