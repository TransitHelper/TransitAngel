package com.transitangel.transitangel.search;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

/**
 * @author yvastavaus.
 */
public class StationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_STATION_START = 1;
    private static final int ITEM_TYPE_STATION_MIDDLE = 2;
    private static final int ITEM_TYPE_STATION_END = 3;
    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

    @IntDef({ITEM_DETAIL, ITEM_ONGOING})
    public @interface ItemType {}

    public static final int ITEM_DETAIL = 0;
    public static final int ITEM_ONGOING = 1;

    private ArrayList<TrainStop> visibleStopsList;
    private ArrayList<TrainStop> allStopItemList;
    private OnItemClickListener onItemClickListener;

    @ItemType private int itemType;

    private boolean isNotificationClickedOnce;

    private Context context;

    public interface OnItemClickListener {
        void onCheckBoxSelected(View view, int position);

        void onCheckBoxUnSelected(View view, int position);
    }

    public StationsAdapter(Context context, ArrayList<TrainStop> stationStopItemList, @ItemType int itemType) {
        this.allStopItemList = stationStopItemList;
        this.visibleStopsList = new ArrayList<>(allStopItemList);
        this.context = context;
        this.itemType = itemType;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        View view;
        if (viewType == ITEM_TYPE_STATION_MIDDLE) {
            if (itemType == ITEM_DETAIL) {
                view = inflater.inflate(R.layout.item_stop_first, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_ongoing, parent, false);
            }
            viewHolder = new StationStopsViewHolder(view, onItemClickListener);
        } else if (viewType == ITEM_TYPE_STATION_START) {
            if (itemType == ITEM_DETAIL) {
                view = inflater.inflate(R.layout.item_stop_first, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_ongoing, parent, false);
            }
            viewHolder = new StationStopsViewHolder(view, onItemClickListener);
        } else if (viewType == ITEM_TYPE_STATION_END) {
            if (itemType == ITEM_DETAIL) {
                view = inflater.inflate(R.layout.item_stop_first, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_ongoing, parent, false);
            }
            viewHolder = new StationStopsViewHolder(view, onItemClickListener);
        }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_STATION_START;
        } else if (position == visibleStopsList.size() - 1) {
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
        viewHolder.tvStopName.setText(visibleStopsList.get(position).getName());
        Timestamp departureTime=DateUtil.getTimeStamp(visibleStopsList.get(position).getDepartureTime());
        String formattedTime=dateFormat.format(departureTime);
        viewHolder.tvStopTime.setText(formattedTime);
        Timestamp now = new Timestamp(new Date().getTime());
        switch (getItemViewType(position)) {
            case ITEM_TYPE_STATION_START:
                params = (RelativeLayout.LayoutParams) viewHolder.trackFinal.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                viewHolder.trackFinal.setLayoutParams(params);
                viewHolder.trackFinal.setVisibility(View.VISIBLE);
                viewHolder.mSetAlarm.setVisibility(View.GONE);
                break;
            case ITEM_TYPE_STATION_END:
                viewHolder.mSetAlarm.setChecked(visibleStopsList.get(position).getNotify());
                params = (RelativeLayout.LayoutParams) viewHolder.trackFinal.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                viewHolder.trackFinal.setVisibility(View.VISIBLE);
                viewHolder.trackFinal.setLayoutParams(params);
                if (DateUtil.getTimeStamp(visibleStopsList.get(position).getDepartureTime()).before(now)) {
                    viewHolder.mSetAlarm.setVisibility(View.GONE);
                } else {
                    viewHolder.mSetAlarm.setVisibility(View.VISIBLE);
                    viewHolder.mSetAlarm.setChecked(visibleStopsList.get(position).getNotify());
                }
                break;
            case ITEM_TYPE_STATION_MIDDLE:
            default:
                if (DateUtil.getTimeStamp(visibleStopsList.get(position).getDepartureTime()).before(now)) {
                    viewHolder.mSetAlarm.setVisibility(View.GONE);
                } else {
                    viewHolder.mSetAlarm.setVisibility(View.VISIBLE);
                    viewHolder.mSetAlarm.setChecked(visibleStopsList.get(position).getNotify());
                }
        }

        String contentDescription = visibleStopsList.get(position).getName()
                + context.getString(R.string.content_description_station)
                + formattedTime
                + (viewHolder.mSetAlarm.isChecked() ? context.getString(R.string.notification_selected) : context.getString(R.string.tap_to_add_notifications));

        viewHolder.setContentDescption(contentDescription);

    }

    public ArrayList<TrainStop> getVisibleStops() {
        return visibleStopsList;
    }

    @Override
    public int getItemCount() {
        return visibleStopsList.size();
    }

    public TrainStop getItem(int position) {
        return visibleStopsList.get(position);
    }

    public void setFilter(String queryText) {
        if (TextUtils.isEmpty(queryText)) {
            visibleStopsList = new ArrayList<>(allStopItemList);
            return;
        }
        visibleStopsList = new ArrayList<>();
        for (TrainStop item : allStopItemList) {
            if (item.getName().toLowerCase().contains(queryText)) {
                visibleStopsList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public class StationStopsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
        @BindView(R.id.cb_alarm)
        CheckBox mSetAlarm;

        View itemView;

        public StationStopsViewHolder(View itemView, StationsAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            mSetAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSetAlarm.isChecked()) {
                        visibleStopsList.get(getLayoutPosition()).setNotify(true);
                        onItemClickListener.onCheckBoxSelected(itemView, getLayoutPosition());
                    } else {
                        visibleStopsList.get(getLayoutPosition()).setNotify(false);
                        onItemClickListener.onCheckBoxUnSelected(itemView, getLayoutPosition());
                    }
                }
            });

            mSetAlarm.setContentDescription(itemView.getContext().getString(R.string.content_description_notification_checkbox));

        }

        @Override
        public void onClick(View view) {
            if (mSetAlarm.isChecked()) {
                mSetAlarm.setChecked(false);
                visibleStopsList.get(getLayoutPosition()).setNotify(false);
                onItemClickListener.onCheckBoxUnSelected(itemView, getLayoutPosition());
            } else {
                mSetAlarm.setChecked(true);
                visibleStopsList.get(getLayoutPosition()).setNotify(true);
                onItemClickListener.onCheckBoxSelected(itemView, getLayoutPosition());
            }
        }

        public void setContentDescption(String contentDescription) {
            itemView.setContentDescription(contentDescription);
        }
    }

}
