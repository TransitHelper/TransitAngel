package com.transitangel.transitangel.search;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.TrainStop;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author yvastavaus.
 */
public class StationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_STATION_START = 1;
    private static final int ITEM_TYPE_STATION_MIDDLE = 2;
    private static final int ITEM_TYPE_STATION_END = 3;

    @IntDef({ITEM_DETAIL, ITEM_ONGOING})
    public @interface ItemType {}

    public static final int ITEM_DETAIL = 0;
    public static final int ITEM_ONGOING = 1;

    private ArrayList<TrainStop> visibleStopsList;
    private ArrayList<TrainStop> allStopItemList;
    private OnItemClickListener onItemClickListener;

    @ItemType private int itemType;

    private Context context;

    public interface OnItemClickListener {
        void onCheckBoxSelected(int position);

        void onCheckBoxUnSelected(int position);
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
        // FUTURE USE TO SETUP THE ICONS ON SEARCH
        switch (getItemViewType(position)) {
            case ITEM_TYPE_STATION_START:
                viewHolder.tvStopName.setText(visibleStopsList.get(position).getName());
                viewHolder.tvStopTime.setText(visibleStopsList.get(position).getDepartureTime());
                viewHolder.mStopIcon.setImageResource(R.mipmap.ic_train_caltrain);
                if (itemType == ITEM_DETAIL) {
                    viewHolder.mSetAlarm.setVisibility(View.GONE);
                }
                break;
            case ITEM_TYPE_STATION_END:
                viewHolder.tvStopName.setText(visibleStopsList.get(position).getName());
                viewHolder.tvStopTime.setText(visibleStopsList.get(position).getDepartureTime());
                viewHolder.mStopIcon.setImageResource(R.mipmap.ic_cal_dest);
                viewHolder.mSetAlarm.setChecked(visibleStopsList.get(position).getNotify());
                break;
            case ITEM_TYPE_STATION_MIDDLE:
            default:
                viewHolder.tvStopName.setText(visibleStopsList.get(position).getName());
                viewHolder.tvStopTime.setText(visibleStopsList.get(position).getDepartureTime());
                viewHolder.mSetAlarm.setChecked(visibleStopsList.get(position).getNotify());
        }
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

    public class StationStopsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.stopIcon)
        ImageView mStopIcon;
        @BindView(R.id.tvStopName)
        TextView tvStopName;
        @BindView(R.id.tvStopTime)
        TextView tvStopTime;
        @BindView(R.id.cb_alarm)
        CheckBox mSetAlarm;

        public StationStopsViewHolder(View itemView, StationsAdapter.OnItemClickListener onItemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mSetAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSetAlarm.isChecked()) {
                        visibleStopsList.get(getLayoutPosition()).setNotify(true);
                        onItemClickListener.onCheckBoxSelected(getLayoutPosition());
                    } else {
                        visibleStopsList.get(getLayoutPosition()).setNotify(false);
                        onItemClickListener.onCheckBoxUnSelected(getLayoutPosition());
                    }
                }
            });

        }
    }

}
