package com.transitangel.transitangel.schedule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.scheduleItem;
import com.transitangel.transitangel.utils.DateUtil;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ScheduleViewHolder> {


    private List<scheduleItem> recentsItemList;
    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

    private Context context;
    private Calendar mCalendar;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public ScheduleRecyclerAdapter(Context context, List<scheduleItem> recentsItemList, OnItemClickListener onItemClickListener) {
        this.recentsItemList = recentsItemList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    public void setFilterCalendar(Calendar calendar) {
        mCalendar = calendar;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        scheduleItem item = recentsItemList.get(position);
        holder.bindTrainData(item);
    }

    @Override
    public int getItemCount() {
        return recentsItemList.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.train_description)
        TextView mTrainInformation;

        @BindView(R.id.status)
        TextView mTrainArrivalTime;

        @BindView(R.id.duration)
        TextView mJourneyTime;

        @BindView(R.id.time)
        TextView tvTime;


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
            String departureTime = "";
            String info = item.getTrain().getNumber() + " " + item.getFrom() + "-" + item.getTo();
            String infoContent = "Train Number " + item.getTrain().getNumber() + " From " + item.getFrom() + " to " + item.getTo();
            mTrainInformation.setText(info);
            final Timestamp timestamp =DateUtil.getTimeStamp(item.getDepatureTime());
            List<TrainStop> mTrainStop = item.getTrain().getTrainStopsBetween(item.getFromStopID(),item.getToStopID());
            final Timestamp destinationArrivalTime =DateUtil.getTimeStamp(mTrainStop.get(mTrainStop.size() - 1).getArrrivalTime());
            String departureRelativeTime;
            if (new Timestamp(new Date().getTime()).equals(timestamp)) {
                departureTime =  dateFormat.format(timestamp);
                departureRelativeTime = "In " + DateUtil.getRelativeTime(timestamp.getTime(), System.currentTimeMillis());
                infoContent += "In " + DateUtil.getRelativeTime(timestamp.getTime(), System.currentTimeMillis());
            } else {
                departureTime =  dateFormat.format(timestamp);
                departureRelativeTime = "At " + dateFormat.format(timestamp);
                infoContent += departureRelativeTime;
            }

            tvTime.setText(departureTime);
            mTrainArrivalTime.setText(departureRelativeTime);
            mTrainInformation.setContentDescription(infoContent);
            mJourneyTime.setText("(" + DateUtil.getRelativeTime(destinationArrivalTime.getTime(), timestamp.getTime()) + ")");
            mJourneyTime.setContentDescription("Arrives destination at" + dateFormat.format(destinationArrivalTime.getTime()));
        }
    }
}
