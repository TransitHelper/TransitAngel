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

        @BindView(R.id.train_departure_time)
        TextView mTrainArrivalTime;

        @BindView(R.id.journey_time)
        TextView mJourneyTime;


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
            String info = item.getTrain().getNumber() + " " + item.getFrom() + "-" + item.getTo();
            String infoContent = "Train Number " + item.getTrain().getNumber() + " From " + item.getFrom() + " to " + item.getTo();
            mTrainInformation.setText(info);
            final Timestamp timestamp =
                    Timestamp.valueOf(
                            new SimpleDateFormat("yyyy-MM-dd ")
                                    .format(new Date())
                                    .concat(item.getDepatureTime()));
            List<TrainStop> mTrainStop = item.getTrain().getTrainStops();
            final Timestamp destinationArrivalTime =
                    Timestamp.valueOf(
                            new SimpleDateFormat("yyyy-MM-dd ")
                                    .format(new Date())
                                    .concat(mTrainStop.get(mTrainStop.size() - 1).getArrrivalTime()));
            String departureRelativeTime;
            final Timestamp filterTimeStamp = Timestamp.valueOf(
                    new SimpleDateFormat("yyyy-MM-dd ")
                            .format(mCalendar.getTime())
                            .concat(item.getDepatureTime()));
            if (filterTimeStamp.equals(timestamp)) {
                departureRelativeTime = "In " + getRelativeTime(timestamp.getTime(), System.currentTimeMillis()) + "(" + dateFormat.format(timestamp) + ")";
                infoContent += "In " + getRelativeTime(timestamp.getTime(), System.currentTimeMillis());
            } else {
                departureRelativeTime = "At " + dateFormat.format(timestamp);
                infoContent += departureRelativeTime;

            }
            mTrainArrivalTime.setText(departureRelativeTime);
            mTrainInformation.setContentDescription(infoContent);
            mJourneyTime.setText(getRelativeTime(destinationArrivalTime.getTime(), timestamp.getTime()));
            mJourneyTime.setContentDescription("Arrives destination at" + dateFormat.format(destinationArrivalTime.getTime()));
        }
    }

    private String getRelativeTime(long time, long time2) {
        //TODO: Try to get destination arrival date
        long diff = time - time2;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        if (diffHours < 0 || diffMinutes < 0)
            return dateFormat.format(time);
        else
            return diffHours + "hr " + diffMinutes + "mins";
    }

}
