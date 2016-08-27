package com.transitangel.transitangel.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.details.DetailsActivity;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.model.scheduleItem;
import com.transitangel.transitangel.search.SearchActivity;
import com.transitangel.transitangel.utils.TAConstants;
import com.transitangel.transitangel.view.RecyclerItemDecoration;
import com.transitangel.transitangel.view.widget.EmptySupportingRecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScheduleFragment extends Fragment implements ScheduleRecyclerAdapter.OnItemClickListener {

    private static final int RESULT_SEARCH_FROM = 1;
    private static final int RESULT_SEARCH_TO = 2;
    private static final int RESULT_DETAILS = 3;
    public static final String FROM_STATION_ID = "from_station_id";
    public static final String TO_STATION_ID = "to_station_id";

    private static final String TAG = ScheduleFragment.class.getSimpleName();

    @BindView(R.id.from_station)
    TextView mFromStation;
    @BindView(R.id.to_station)
    TextView mToStation;
    @BindView(R.id.swap_station)
    ImageView mSwapStationBtn;
    @BindView(R.id.rvRecents)
    EmptySupportingRecyclerView mRecyclerView;
    @BindView(R.id.empty_view_stub)
    ViewStub mViewStub;


    private static final String ARG_TRANSIT_TYPE = "transit_type";
    private TAConstants.TRANSIT_TYPE mTRANSITType;
    private static String mFromStationId;
    private static String mToStationId;
    List<Stop> mStops = new ArrayList<>();
    List<scheduleItem> mRecentItems = new ArrayList<>();
    ScheduleRecyclerAdapter mRecyclerViewAdapter;
    HashMap<String, Stop> stopHashMap = new HashMap<>();
    public ScheduleFragment() {
        // Required empty public constructor
    }

    public static ScheduleFragment newInstance(TAConstants.TRANSIT_TYPE type) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSIT_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTRANSITType = (TAConstants.TRANSIT_TYPE) getArguments().getSerializable(ARG_TRANSIT_TYPE);
        }
        if (mTRANSITType == TAConstants.TRANSIT_TYPE.BART) {
            mStops = BartTransitManager.getSharedInstance().getStops();
            stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
        } else {
            mStops = CaltrainTransitManager.getSharedInstance().getStops();
            stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        ButterKnife.bind(this, view);
        mRecyclerViewAdapter = new ScheduleRecyclerAdapter(getContext(), mRecentItems, this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(getContext(), R.drawable.recycler_view_divider));
        View emptyView = mViewStub.inflate();
        TextView textView=(TextView) emptyView.findViewById(R.id.text_empty_state_description);
        textView.setText(R.string.empty_results);
        ImageView icon=(ImageView) emptyView.findViewById(R.id.image_empty_state);
        icon.setImageResource(R.mipmap.ic_train);
        mRecyclerView.setEmptyView(emptyView);
        return view;
    }

    private void getTrainSchedule() {
        ArrayList<Train> trains = new ArrayList<>();
        if (TAConstants.TRANSIT_TYPE.BART == mTRANSITType) {
            trains = BartTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId, 5, new Date(), false);
        } else {
            trains = CaltrainTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId,
                    5, new Date(), false);
        }
        mRecentItems.clear();
        for (Train train : trains) {
            TrainStop mSource = train.getTrainStops().get(0);
            mRecentItems.add(new scheduleItem(stopHashMap.get(mSource.getStopId()).getName(),
                    stopHashMap.get(train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()).getName()
                    , mSource.getDepartureTime(), "", train));
        }
    }


    private void refreshTrainSchedule() {
        if (mFromStationId != null && mToStationId != null) {
            getTrainSchedule();
            if (mRecyclerViewAdapter != null) {
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @OnClick(R.id.to_station)
    protected void onToStationClick() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        if(mTRANSITType == TAConstants.TRANSIT_TYPE.BART) {
            intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_BART);
        } else {
            intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_CALTRAIN);
        }
        intent.putExtra(FROM_STATION_ID, mFromStationId);
        getActivity().startActivityForResult(intent, RESULT_SEARCH_TO, null);
    }

    @OnClick(R.id.from_station)
    protected void onFromStationClick() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        if(mTRANSITType == TAConstants.TRANSIT_TYPE.BART) {
            intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_BART);
        } else {
            intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_CALTRAIN);
        }
        intent.putExtra(TO_STATION_ID, mToStationId);
        getActivity().startActivityForResult(intent, RESULT_SEARCH_FROM, null);
    }

    @OnClick(R.id.swap_station)
    protected void onSwapStationClick() {
        //TODO: replace spinner with new screen
        String mtemp = mFromStationId;
        mFromStationId = mToStationId;
        mToStationId = mtemp;
        updateStationLabels(true);
    }

    private void updateStationLabels(boolean isSwapStation) {
        boolean isStation = stopHashMap.containsKey(mToStationId);
        if (isStation) {
            String stationName = stopHashMap.get(mToStationId).getName();
            mToStation.setText(stationName);
            Log.d(TAG, "To Station : " + stationName);
        }

        isStation = stopHashMap.containsKey(mFromStationId);
        if (isStation) {
            String stationName = stopHashMap.get(mFromStationId).getName();
            Log.d(TAG, "From Station : " + stationName);
        }
        mToStation.setText(stopHashMap.containsKey(mToStationId) ?
                stopHashMap.get(mToStationId).getName() : "Select To Station");
        mFromStation.setText(stopHashMap.containsKey(mFromStationId) ?
                stopHashMap.get(mFromStationId).getName() : "Select From Station");
        refreshTrainSchedule();
        if (!isSwapStation && stopHashMap.containsKey(mFromStationId) && stopHashMap.containsKey(mToStationId)) {
            Trip trip = new Trip();
            trip.setFromStop(stopHashMap.get(mFromStationId));
            trip.setToStop(stopHashMap.get(mToStationId));
            trip.setDate(new Date());
            if (mTRANSITType == TAConstants.TRANSIT_TYPE.CALTRAIN) {
                CaltrainTransitManager.getSharedInstance().saveRecentSearch(trip);
            } else {
                BartTransitManager.getSharedInstance().saveRecentSearch(trip);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SEARCH_FROM) {
            if (resultCode == Activity.RESULT_OK) {
                Stop stop = data.getParcelableExtra(SearchActivity.EXTRA_SELECTED_STATION);
                if(stop != null) {
                    mFromStationId = stop.getId();
                    Log.d(TAG, "from station id: " + mFromStationId);
                } else {
                    Log.e(TAG, "Error while getting stop for from");
                }
                return;
            }
        }

        if (requestCode == RESULT_SEARCH_TO) {
            if (resultCode == Activity.RESULT_OK) {
                Stop stop = data.getParcelableExtra(SearchActivity.EXTRA_SELECTED_STATION);
                if(stop != null) {
                    mToStationId = stop.getId();
                    Log.d(TAG, "to station id: " + mToStationId);
                } else {
                    Log.e(TAG, "Error while getting stop for to");
                }
                return;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStationLabels(false);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        if(mTRANSITType == TAConstants.TRANSIT_TYPE.BART) {
            intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_BART);
        } else {
            intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_CALTRAIN);
        }
        intent.putExtra(DetailsActivity.EXTRA_TRAIN, mRecentItems.get(position).getTrain());
        intent.putExtra(DetailsActivity.EXTRA_FROM_STATION, mFromStationId);
        intent.putExtra(DetailsActivity.EXTRA_TO_STATION, mToStationId);
        getActivity().startActivityForResult(intent, RESULT_DETAILS, null);
    }
}
