package com.transitangel.transitangel.home;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LiveTripFragment extends Fragment
        implements LiveTripAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = LiveTripFragment.class.getSimpleName();

    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;

    @BindView(R.id.emptyView)
    ViewGroup mNoLiveTrip;

    @BindView(R.id.btnCancelTrip)
    Button btnCancelTrip;

    @BindView(R.id.swipRefresh)
    SwipeRefreshLayout mSwipeRefresh;

    private ArrayList<TrainStop> mStops;
    private LiveTripAdapter adapter;
    private TAConstants.TRANSIT_TYPE type;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mEmptyTextView;
    HashMap<String, Stop> stopHashMap = new HashMap<>();

    public LiveTripFragment() {}

    public static LiveTripFragment newInstance() {
        LiveTripFragment fragment = new LiveTripFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_trip, container, false);
        ButterKnife.bind(this, view);
        displayOnGoingTrip();
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeResources(new int[]{R.color.colorPrimary});
        return view;
    }

    public void onSelected() {
        displayOnGoingTrip();
    }

    public void displayOnGoingTrip() {
        Trip trip = PrefManager.getOnGoingTrip();
        if (trip != null) {
            btnCancelTrip.setVisibility(View.VISIBLE);
            rvStationList.setVisibility(View.VISIBLE);
            mNoLiveTrip.setVisibility(View.GONE);
            rvStationList.setHasFixedSize(true);
            type = trip.getType();
            mStops = trip.getSelectedTrain().getTrainStopsBetween(trip.getFromStop().getId(), trip.getToStop().getId());
            if (type == TAConstants.TRANSIT_TYPE.CALTRAIN) {
                stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
            } else {
                stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
            }
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            adapter = new LiveTripAdapter(getActivity(), mStops);
            rvStationList.setAdapter(adapter);
            rvStationList.setLayoutManager(mLinearLayoutManager);
            adapter.setOnItemClickListener(this);
            adapter.setCurrentPosition(adapter.getCurrentPositions());
            mLinearLayoutManager.scrollToPositionWithOffset(adapter.getCurrentPosition(), 0);
        } else {
            // Display empty screen
            rvStationList.setVisibility(View.GONE);
            btnCancelTrip.setVisibility(View.GONE);
            mNoLiveTrip.setVisibility(View.VISIBLE);
            mEmptyTextView = (TextView) mNoLiveTrip.findViewById(R.id.empty_state_description);
            mEmptyTextView.setText(R.string.no_live_trip);
            ImageView icon = (ImageView) mNoLiveTrip.findViewById(R.id.image_empty_state);
            icon.setImageResource(R.drawable.train_blue_bart);
        }
    }

    @Override
    public void onMockItDefault(int position) {
    }

    @Override
    public void onMockSelected(int position) {
        TrainStop stop = mStops.get(position);
        TransitLocationManager.getSharedInstance().setMockLocation(getContext(), stop.getLatitude(),
                stop.getLongitude(), 50);
        displayOnGoingTrip(position);
    }

    @Override
    public void onCurrentItemListener(int position) {
        if (rvStationList != null)
            rvStationList.scrollToPosition(position);
    }

    @OnClick(R.id.btnCancelTrip)
    public void onCancelTrip() {
        //Remove Notification FIRST before removing persistent notification
        NotificationProvider.getInstance().dismissOnGoingNotification(getActivity());
        // Remove the persistent notification.
        PrefManager.removeOnGoingTrip();
        // Refresh the ongoing trip tab.
        displayOnGoingTrip();
        Toast.makeText(getActivity(), "Trip Cancelled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        displayOnGoingTrip();
       mSwipeRefresh.setRefreshing(false);
    }

    //Mock Location UI for demo
    public void displayOnGoingTrip(int position) {
        Trip trip = PrefManager.getOnGoingTrip();
        if (trip != null) {
            btnCancelTrip.setVisibility(View.VISIBLE);
            rvStationList.setVisibility(View.VISIBLE);
            mNoLiveTrip.setVisibility(View.GONE);
            rvStationList.setHasFixedSize(true);
            type = trip.getType();
            mStops = trip.getSelectedTrain().getTrainStopsBetween(trip.getFromStop().getId(), trip.getToStop().getId());
            if (type == TAConstants.TRANSIT_TYPE.CALTRAIN) {
                stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
            } else {
                stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
            }
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            adapter = new LiveTripAdapter(getActivity(), mStops);
            rvStationList.setAdapter(adapter);
            rvStationList.setLayoutManager(mLinearLayoutManager);
            adapter.setOnItemClickListener(this);
            adapter.setCurrentPosition(position+1);
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        } else {
            // Display empty screen
            rvStationList.setVisibility(View.GONE);
            btnCancelTrip.setVisibility(View.GONE);
            mNoLiveTrip.setVisibility(View.VISIBLE);
            mEmptyTextView = (TextView) mNoLiveTrip.findViewById(R.id.empty_state_description);
            mEmptyTextView.setText(R.string.no_live_trip);
            ImageView icon = (ImageView) mNoLiveTrip.findViewById(R.id.image_empty_state);
            icon.setImageResource(R.drawable.train_blue_bart);
        }
    }
}
