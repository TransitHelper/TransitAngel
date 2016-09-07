package com.transitangel.transitangel.schedule;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.scheduleItem;
import com.transitangel.transitangel.utils.DateUtil;
import com.transitangel.transitangel.utils.TAConstants;

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
    private TAConstants.TRANSIT_TYPE mTransitType;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public ScheduleRecyclerAdapter(Context context, List<scheduleItem> recentsItemList,
                                   OnItemClickListener onItemClickListener
            , TAConstants.TRANSIT_TYPE type) {
        mTransitType = type;
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
        ScheduleViewHolder scheduleViewHolder = new ScheduleViewHolder(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleViewHolder.mTrainInformation.setTransitionName(context.getString(R.string.transition_details));
        }
        return scheduleViewHolder;
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
        TextView mRelativeDepatureTime;

        @BindView(R.id.time)
        TextView mDepatureTime;
        @BindView(R.id.imageView)
        ImageView mImageView;


        public ScheduleViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        //TODO: go to information page
                        onItemClickListener.onItemClick(mTrainInformation, getLayoutPosition());
                    }
                }

            });
        }

        public void bindTrainData(scheduleItem item) {
            String departureTime = "";
            String info = item.getFrom() + "-" + item.getTo();
            String infoContent;

            if (mTransitType == TAConstants.TRANSIT_TYPE.CALTRAIN) {
                info = item.getTrain().getNumber() + " " + info;
                infoContent = "Train Number " + item.getTrain().getNumber() + " From " + item.getFrom() + " to " + item.getTo();
            } else {
                mImageView.setImageResource(R.drawable.train_blue);
                infoContent = "Train From " + item.getFrom() + " to " + item.getTo();
            }
            mTrainInformation.setText(info);
            final Timestamp timestamp = DateUtil.getTimeStamp(item.getDepatureTime());
            List<TrainStop> mTrainStop = item.getTrain().getTrainStopsBetween(item.getFromStopID(), item.getToStopID());
            final Timestamp destinationArrivalTime = DateUtil.getTimeStamp(mTrainStop.get(mTrainStop.size() - 1).getArrrivalTime());
            String departureRelativeTime;
            departureTime = dateFormat.format(timestamp);
            if (new Timestamp(new Date().getTime()).after(timestamp)) {
                departureRelativeTime = "At " + dateFormat.format(timestamp);

            } else {
                departureRelativeTime = DateUtil.getRelativeTime(timestamp.getTime(), System.currentTimeMillis());
            }
            infoContent += ", is at " + dateFormat.format(timestamp) + ". Arrives destination at" + dateFormat.format(destinationArrivalTime);
            mTrainInformation.setContentDescription(infoContent);
            mDepatureTime.setText(departureTime + " - " + dateFormat.format(destinationArrivalTime));
            mRelativeDepatureTime.setText(departureRelativeTime);
        }
    }
}
