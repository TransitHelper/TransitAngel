package com.transitangel.transitangel.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.transitangel.transitangel.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * author yvastavaus.
 */
public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    @BindView(R.id.tvStopName)
    TextView tvStopName;
    View parent;
    SearchAdapter.OnItemClickListener onItemClickListener;

    public SearchViewHolder(View itemView, SearchAdapter.OnItemClickListener onItemClickListener) {
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
