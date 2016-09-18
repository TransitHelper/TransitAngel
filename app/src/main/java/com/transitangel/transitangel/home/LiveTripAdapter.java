package com.transitangel.transitangel.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.utils.DateUtil;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LiveTripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_STATION_START = 1;
    private static final int ITEM_TYPE_STATION_MIDDLE = 2;
    private static final int ITEM_TYPE_STATION_END = 3;
    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
    private ArrayList<TrainStop> allStopItemList;
    private OnItemClickListener onItemClickListener;
    int currentPosition = 0;


    private Context context;

    public interface OnItemClickListener {
        void onMockItDefault(int position);

        void onMockSelected(int position);

        void onCurrentItemListener(int position);
    }

    public LiveTripAdapter(Context context, ArrayList<TrainStop> stationStopItemList) {
        this.allStopItemList = stationStopItemList;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setCurrentPosition(int currentPosition)
    {
        this.currentPosition=currentPosition;
    }

    public int getCurrentPosition(){
        return currentPosition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        View view = inflater.inflate(R.layout.item_ongoing, parent, false);
        return new StationStopsViewHolder(view, onItemClickListener);
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_STATION_START;
        } else if (position == allStopItemList.size() - 1) {
            return ITEM_TYPE_STATION_END;
        }
        // Default is middle
        return ITEM_TYPE_STATION_MIDDLE;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StationStopsViewHolder viewHolder = (StationStopsViewHolder) holder;
        RelativeLayout.LayoutParams params;
        // FUTURE USE TO SETUP THE ICONS ON SEARCH
        viewHolder.tvStopName.setText(allStopItemList.get(position).getName());
        Timestamp departureTime = DateUtil.getTimeStamp(allStopItemList.get(position).getDepartureTime());
        String formattedTime = dateFormat.format(departureTime);
        viewHolder.tvStopTime.setText(formattedTime);
        Timestamp now = new Timestamp(new Date().getTime());
        switch (getItemViewType(position)) {
            case ITEM_TYPE_STATION_START:
                params = (RelativeLayout.LayoutParams) viewHolder.trackFinal.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                viewHolder.trackFinal.setLayoutParams(params);
                viewHolder.trackFinal.setVisibility(View.VISIBLE);
                break;
            case ITEM_TYPE_STATION_END:
                params = (RelativeLayout.LayoutParams) viewHolder.trackFinal.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                viewHolder.trackFinal.setVisibility(View.VISIBLE);
                viewHolder.trackFinal.setLayoutParams(params);
                break;
            case ITEM_TYPE_STATION_MIDDLE:
            default:
                break;
        }
        if (context.getResources().getBoolean((R.bool.is_mock_build))) {
            viewHolder.mMockIt.setChecked(false);
        }else{
            viewHolder.mMockIt.setVisibility(View.GONE);
        }

        if (position < currentPosition) {
            viewHolder.trackVerticalIcon.setBackgroundColor(context.getResources().getColor(R.color.light_divider));
            viewHolder.trackIcon.setBackgroundColor(context.getResources().getColor(R.color.light_divider));
            viewHolder.trackFinal.setBackgroundColor(context.getResources().getColor(R.color.light_divider));
            viewHolder.mMockIt.setEnabled(false);
        } else {
            viewHolder.trackVerticalIcon.setBackgroundColor(context.getResources().getColor(R.color.blue_tracks));
            viewHolder.trackIcon.setBackgroundColor(context.getResources().getColor(R.color.blue_tracks));
            viewHolder.trackFinal.setBackgroundColor(context.getResources().getColor(R.color.blue_tracks));
        }
        String contentDescription = allStopItemList.get(position).getName()
                + context.getString(R.string.content_description_station)
                + formattedTime;
        viewHolder.itemView.setContentDescription(contentDescription);
    }

    @Override
    public int getItemCount() {
        return allStopItemList.size();
    }

    public TrainStop getItem(int position) {
        return allStopItemList.get(position);
    }

    public class StationStopsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vIcon)
        View trackVerticalIcon;
        @BindView(R.id.track)
        View trackIcon;
        @BindView(R.id.trackFinal)
        View trackFinal;
        @BindView(R.id.tvStopName)
        TextView tvStopName;
        @BindView(R.id.tvStopTime)
        TextView tvStopTime;
        @BindView(R.id.mock_it)
        ToggleButton mMockIt;
        View itemView;

        public StationStopsViewHolder(View itemView, LiveTripAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            mMockIt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        currentPosition=getLayoutPosition();
                        onItemClickListener.onMockSelected(getLayoutPosition());
                    } else {
                        currentPosition=getLayoutPosition();
                        onItemClickListener.onMockItDefault(getLayoutPosition());
                    }
                }
            });

        }
    }

    public int getCurrentPositions() {
        Timestamp now = new Timestamp(new Date().getTime());
        for (int position = 0; position < allStopItemList.size(); position++) {
            if (!(DateUtil.getTimeStamp(allStopItemList.get(position).getDepartureTime()).before(now))) {
                return position;
            }
        }
        return 0;
    }
}
