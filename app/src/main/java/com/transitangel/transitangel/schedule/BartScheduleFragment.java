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

public class BartScheduleFragment extends Fragment
        implements ScheduleRecyclerAdapter.OnItemClickListener,
        FilterDialogFragment.FilterChangedListener, ScheduleActivity.OnStationSelected  {

    private static final int RESULT_SEARCH_FROM = 1;
    private static final int RESULT_SEARCH_TO = 2;
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
    private static TAConstants.TRANSIT_TYPE mTRANSITType;
    private static String mFromStationId;
    private static String mToStationId;
    List<Stop> mStops = new ArrayList<>();
    List<scheduleItem> mRecentItems = new ArrayList<>();
    ScheduleRecyclerAdapter mRecyclerViewAdapter;
    HashMap<String, Stop> stopHashMap = new HashMap<>();
    public Calendar mCalendar = Calendar.getInstance();

    public BartScheduleFragment() {
        // Required empty public constructor
    }

    public static BartScheduleFragment newInstance(TAConstants.TRANSIT_TYPE type) {
        BartScheduleFragment fragment = new BartScheduleFragment();
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
            mTRANSITType = TAConstants.TRANSIT_TYPE.BART;
            mToStationId = getArguments().getString(TO_STATION_ID, null);
            mFromStationId = getArguments().getString(FROM_STATION_ID, null);
        }
        InitializeData();

    }

    private void InitializeData() {
        mStops = BartTransitManager.getSharedInstance().getStops();
        stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
        if (mFromStationId == null) {
            Trip trip = TransitManager.getSharedInstance().fetchRecentTrip();
            if (trip != null && trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                mFromStationId = trip.getFromStop().getId();
                mToStationId = trip.getToStop().getId();
            } else {
                //TODO: set nearest location, want to save last known location and get nearest stop
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
        trains = BartTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId, 5, date, false);
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

    @Override
    public void onToStationSelected() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_BART);
        intent.putExtra(FROM_STATION_ID, mFromStationId);
        getActivity().startActivityForResult(intent, RESULT_SEARCH_TO, null);
    }

    @Override
    public void onFromStationSelected(Intent intent) {
        intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_BART);
        intent.putExtra(TO_STATION_ID, mToStationId);
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
        intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_BART);
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
}