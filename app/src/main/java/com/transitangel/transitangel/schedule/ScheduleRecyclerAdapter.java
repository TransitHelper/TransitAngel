package com.transitangel.transitangel.schedule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.scheduleItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ScheduleViewHolder> {


    private List<scheduleItem> recentsItemList;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public ScheduleRecyclerAdapter(Context context, List<scheduleItem> recentsItemList, OnItemClickListener onItemClickListener) {
        this.recentsItemList = recentsItemList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.schedule_item, parent, false);
        return new ScheduleViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        scheduleItem item=recentsItemList.get(position);
        holder.bindTrainData(item);
    }

    @Override
    public int getItemCount() {
        return recentsItemList.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.train_name)
        TextView mTrainName;

        @BindView(R.id.train_time)
        TextView mTrainArrivalTime;

        @BindView(R.id.important_info)
        TextView mUserName;


        public ScheduleViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        //TODO: go to information page
                        onItemClickListener.onItemClick(getPosition());
                    }
                }

            });
        }

        public void bindTrainData(scheduleItem item) {
            mTrainName.setText(item.getFrom());
            mTrainArrivalTime.setText(item.getDepatureTime());
        }
    }

}
