package com.transitangel.transitangel.schedule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.details.DetailsActivity;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.model.scheduleItem;
import com.transitangel.transitangel.search.SearchActivity;
import com.transitangel.transitangel.utils.TAConstants;
import com.transitangel.transitangel.view.widget.EmptySupportingRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleFragment extends Fragment
        implements ScheduleRecyclerAdapter.OnItemClickListener,
        FilterDialogFragment.FilterChangedListener, ScheduleActivity.OnStationSelected {

    public static final int RESULT_SEARCH_FROM = 1;
    public static final int RESULT_SEARCH_TO = 2;
    private static final int RESULT_DETAILS = 3;
    public static final String FROM_STATION_ID = "from_station_id";
    public static final String TO_STATION_ID = "to_station_id";
    public static final String TRANSIT_TYPE = "Transit_type";

    private static final String TAG = ScheduleFragment.class.getSimpleName();
    ProgressDialog mProgressDialog;

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
    public Calendar mCalendar = Calendar.getInstance();

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
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            //TODO: mTransitType will always reset to bart due to tabviews
            mTRANSITType = (TAConstants.TRANSIT_TYPE) getArguments().getSerializable(ARG_TRANSIT_TYPE);
            mToStationId = getArguments().getString(TO_STATION_ID, null);
            mFromStationId = getArguments().getString(FROM_STATION_ID, null);
        }
        InitializeData();

    }

    private void InitializeData() {
        if (mTRANSITType == TAConstants.TRANSIT_TYPE.BART) {
            mStops = BartTransitManager.getSharedInstance().getStops();
            stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
        } else {
            mStops = CaltrainTransitManager.getSharedInstance().getStops();
            stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
            if (mFromStationId == null) {
                Trip trip = TransitManager.getSharedInstance().fetchRecentTrip();
                if (trip != null) {
                    mFromStationId = trip.getFromStop().getId();
                    mToStationId = trip.getToStop().getId();
                } else {
                    //TODO: set nearest location, want to save last known location and get nearest stop
                }
            }
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
        View emptyView = mViewStub.inflate();
        TextView textView = (TextView) emptyView.findViewById(R.id.text_empty_state_description);
        textView.setText(R.string.empty_results);
        ImageView icon = (ImageView) emptyView.findViewById(R.id.image_empty_state);
        icon.setImageResource(R.mipmap.ic_train);
        mRecyclerView.setEmptyView(emptyView);
        refreshTrainSchedule();
        return view;
    }

    private void getTrainSchedule() {
        ArrayList<Train> trains = new ArrayList<>();
        Date date = mCalendar.getTime();
        if (TAConstants.TRANSIT_TYPE.BART == mTRANSITType) {
            trains = BartTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId, 5, date, false);
        } else {
            trains = CaltrainTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId,
                    5, date, false);
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
        showProgressDialog();
        if (mFromStationId != null && mToStationId != null) {
            getTrainSchedule();
            if (mRecyclerViewAdapter != null) {
                mRecyclerViewAdapter.setFilterCalendar(mCalendar);
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
        hideProgressDialog();
    }

    protected void onSwapStationClick() {
        String temp = mFromStationId;
        mFromStationId = mToStationId;
        mToStationId = temp;
        updateStationLabels(true);
    }

    private void updateStationLabels(boolean isSwapStation) {
        boolean isStation = stopHashMap.containsKey(mToStationId);
        if (isStation) {
            String stationName = stopHashMap.get(mToStationId).getName();
            Log.d(TAG, "To Station : " + stationName);
        }

        isStation = stopHashMap.containsKey(mFromStationId);
        if (isStation) {
            String stationName = stopHashMap.get(mFromStationId).getName();
            Log.d(TAG, "From Station : " + stationName);
        }

        ((ScheduleActivity)getActivity()).setToStation(stopHashMap.containsKey(mToStationId) ?
                stopHashMap.get(mToStationId).getName() : "Select To Station");

        ((ScheduleActivity)getActivity()).setFromStation(stopHashMap.containsKey(mFromStationId) ?
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
                if (stop != null) {
                    mFromStationId = stop.getId();
                }
                return;
            }
        }
        if (requestCode == RESULT_SEARCH_TO) {
            if (resultCode == Activity.RESULT_OK) {
                Stop stop = data.getParcelableExtra(SearchActivity.EXTRA_SELECTED_STATION);
                if (stop != null) {
                    mToStationId = stop.getId();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_schedule2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            FilterDialogFragment editDialogFragment = FilterDialogFragment.newInstance(getContext(), this, mTRANSITType);
            editDialogFragment.show(getActivity().getFragmentManager().beginTransaction(), "Filter");
            return true;
        } else if(item.getItemId() == R.id.action_reverse) {
            onSwapStationClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        if (mTRANSITType == TAConstants.TRANSIT_TYPE.BART) {
            intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_BART);
        } else {
            intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_CALTRAIN);
        }
        intent.putExtra(DetailsActivity.EXTRA_TRAIN, mRecentItems.get(position).getTrain());
        intent.putExtra(DetailsActivity.EXTRA_FROM_STATION, mFromStationId);
        intent.putExtra(DetailsActivity.EXTRA_TO_STATION, mToStationId);
        getActivity().startActivityForResult(intent, RESULT_DETAILS, null);
    }


    @Override
    public void onFilterChanged(Calendar calendar, TAConstants.TRANSIT_TYPE type) {
        mCalendar = calendar;
        mTRANSITType = type;
        refreshTrainSchedule();
    }


    public void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(getContext(), null, "Loading Data..", true, true);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onFromStationSelected(Intent intent) {
        intent.putExtra(TO_STATION_ID, mToStationId);
    }

    @Override
    public void onToStationSelected(Intent intent) {
        intent.putExtra(FROM_STATION_ID, mFromStationId);
    }
}
