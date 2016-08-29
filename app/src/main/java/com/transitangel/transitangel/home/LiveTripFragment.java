package com.transitangel.transitangel.home;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.search.StationsAdapter;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LiveTripFragment extends Fragment implements StationsAdapter.OnItemClickListener {

    public static final String TAG = LiveTripFragment.class.getSimpleName();

    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;

    @BindView(R.id.tvNoLiveTrip)
    TextView tvNoLiveTrip;

    private ArrayList<TrainStop> mStops;
    private StationsAdapter adapter;
    private TAConstants.TRANSIT_TYPE type;

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
        return view;
    }

    public void onSelected() {
        displayOnGoingTrip();
    }

    private void displayOnGoingTrip() {
        Trip trip = PrefManager.getOnGoingTrip();
        if (trip != null) {
            rvStationList.setVisibility(View.VISIBLE);
            tvNoLiveTrip.setVisibility(View.GONE);
            type = trip.getType();
            mStops = trip.getSelectedTrain().getTrainStops();
            if (type == TAConstants.TRANSIT_TYPE.CALTRAIN) {
                stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
            } else {
                stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
            }

            // Create the recents adapter.
            adapter = new StationsAdapter(getActivity(), mStops, stopHashMap, StationsAdapter.ITEM_ONGOING);
            rvStationList.setAdapter(adapter);
            rvStationList.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter.setOnItemClickListener(this);
        } else {
            // Display empty screen
            rvStationList.setVisibility(View.GONE);
            tvNoLiveTrip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCheckBoxSelected(int position) {

    }

    @Override
    public void onCheckBoxUnSelected(int position) {

    }

    public void onCancelTrip() {
        PrefManager.removeOnGoingTrip();
        // Remove the persistent notification.
        NotificationProvider.getInstance().dismissOnGoingNotification(getActivity());
        // Refresh the ongoing trip tab.
        displayOnGoingTrip();
        Toast.makeText(getActivity(), "Trip Cancelled.", Toast.LENGTH_SHORT).show();
    }
}
