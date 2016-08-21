package com.transitangel.transitangel.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.home.RecentAdapter;
import com.transitangel.transitangel.home.RecentsItem;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchFragment extends Fragment {

    @BindView(R.id.from_station)
    Spinner mFromStation;
    @BindView(R.id.to_station)
    Spinner mToStation;
    @BindView(R.id.swap_station)
    ImageButton mSwapStationBtn;
    @BindView(R.id.rvRecents)
    RecyclerView mRecyclerView;
    @BindView(R.id.recents_Scroll_view)
    NestedScrollView mNestedScrollView;
    private static final String ARG_TRANSIT_TYPE = "transit_type";
    private TAConstants.TRANSIT_TYPE mTRANSIT_type;
    private String mFromStationId = "";
    private String mToStationId = "";
    List<Stop> mStops = new ArrayList<>();
    List<RecentsItem> mRecentItems = new ArrayList<>();
    RecentAdapter mRecyclerViewAdapter;
    int mFromStationPosition=0;
    int mToStationPosition=0;

    public SearchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(TAConstants.TRANSIT_TYPE type) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSIT_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTRANSIT_type = (TAConstants.TRANSIT_TYPE) getArguments().getSerializable(ARG_TRANSIT_TYPE);
        }
        if (mTRANSIT_type == TAConstants.TRANSIT_TYPE.BART) {
            mStops = BartTransitManager.getSharedInstance().getStops();
        } else {
            mStops = CaltrainTransitManager.getSharedInstance().getStops();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        setUpStations();
        getRecentTrains();
        mRecyclerViewAdapter = new RecentAdapter(getContext(), mRecentItems);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setNestedScrollingEnabled(false);
        mNestedScrollView.post(() -> mNestedScrollView.scrollTo(0, 0));
        return view;
    }

    private void getRecentTrains() {
        ArrayList<Train> trains = new ArrayList<>();
        if (TAConstants.TRANSIT_TYPE.BART == mTRANSIT_type) {
            trains = BartTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId, 5, new Date(), false);
        } else {
            trains = CaltrainTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId,
                    5, new Date(), false);
        }
        mRecentItems.clear();
        for (Train train : trains) {
            mRecentItems.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()));
        }
    }

    private void setUpStations() {
        //TODO: getDefault stations
        mFromStationId = mStops.get(0).getId();
        mToStationId = mStops.get(mStops.size() - 1).getId();
        HashMap<String, Stop> stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        ArrayAdapter<Stop> adapter = new ArrayAdapter<Stop>
                (getContext(), android.R.layout.simple_spinner_item, mStops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
        mFromStation.setAdapter(adapter); // Apply the adapter to the spinner
        mToStation.setSelection(mFromStationPosition);
        mToStation.setAdapter(adapter);
        mToStation.setSelection(mToStationPosition);
        mFromStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (mFromStationId.isEmpty() || !mFromStationId.equals(mStops.get(position).getId())) {
                    mFromStationId = mStops.get(position).getId();
                    refreshTrainSchedule();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mToStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (mToStationId.isEmpty() || !mToStationId.equals(mStops.get(position).getId())) {
                    mToStationId = mStops.get(position).getId();
                    refreshTrainSchedule();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void refreshTrainSchedule() {
        ArrayList<Train> trains = CaltrainTransitManager.getSharedInstance().fetchTrains(mFromStationId, mToStationId,
                5, new Date(), false);
        mRecentItems.clear();
        for (Train train : trains) {
            mRecentItems.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()));
        }
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }

    }

    @OnClick(R.id.swap_station)
    protected void onSwapStationClick() {
        //TODO: replace spinner with new screen
        String mtemp = mFromStationId;
        mFromStationId = mToStationId;
        mToStationId = mtemp;
      //  mToStation.setSelection((adapter.getPosition(stopHashMap.get(mToStationId)));
        refreshTrainSchedule();

    }
}
