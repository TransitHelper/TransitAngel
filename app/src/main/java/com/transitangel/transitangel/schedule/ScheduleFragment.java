package com.transitangel.transitangel.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.scheduleItem;
import com.transitangel.transitangel.search.SearchActivity;
import com.transitangel.transitangel.utils.TAConstants;
import com.transitangel.transitangel.view.RecyclerItemDecoration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScheduleFragment extends Fragment {

    private static final int RESULT_SEARCH_FROM = 1;
    private static final int RESULT_SEARCH_TO = 2;
    public static final String FROM_STATION_ID = "from_station_id";
    public static final String TO_STATION_ID = "to_station_id";

    private static final String TAG = ScheduleFragment.class.getSimpleName();

    @BindView(R.id.from_station)
    TextView mFromStation;
    @BindView(R.id.to_station)
    TextView mToStation;
    @BindView(R.id.swap_station)
    ImageButton mSwapStationBtn;
    @BindView(R.id.rvRecents)
    RecyclerView mRecyclerView;
    @BindView(R.id.recents_Scroll_view)
    NestedScrollView mNestedScrollView;

    private static final String ARG_TRANSIT_TYPE = "transit_type";
    private TAConstants.TRANSIT_TYPE mTRANSITType;
    private String mFromStationId = "";
    private String mToStationId = "";
    List<Stop> mStops = new ArrayList<>();
    List<scheduleItem> mRecentItems = new ArrayList<>();
    ScheduleRecyclerAdapter mRecyclerViewAdapter;
    HashMap<String, Stop> stopHashMap = new HashMap<>();
    int mFromStationPosition = 0;
    int mToStationPosition = 0;
    ScheduleRecyclerAdapter.OnItemClickListener mOnItemClickListener;

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
        setUpStations();
        getTrainSchedule();
        mRecyclerViewAdapter = new ScheduleRecyclerAdapter(getContext(), mRecentItems);
        mRecyclerViewAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(getContext(),R.drawable.recycler_view_divider));
        mNestedScrollView.post(() -> mNestedScrollView.scrollTo(0, 0));
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
            TrainStop mSource=train.getTrainStops().get(0);
            mRecentItems.add(new scheduleItem(mSource.getStopId(),
                    train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()
                    ,mSource.getDepartureTime(),""));
        }
    }

    private void setUpStations() {
        //TODO: getDefault stations
        mFromStationId = mStops.get(0).getId();
        mToStationId = mStops.get(mStops.size() - 1).getId();

        ArrayAdapter<Stop> adapter = new ArrayAdapter<Stop>
                (getContext(), android.R.layout.simple_spinner_item, mStops);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Specify the layout to use when the list of choices appears
        mFromStation.setText(mStops.get(0).getName());
        mToStation.setText(mStops.get(2).getName());
    }

    private void refreshTrainSchedule() {
        getTrainSchedule();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }

    }

    @OnClick(R.id.to_station)
    protected void onToStationClick() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(FROM_STATION_ID, mFromStationId);
        getActivity().startActivityForResult(intent, RESULT_SEARCH_TO, null);
    }

    @OnClick(R.id.from_station)
    protected void onFromStationClick() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(TO_STATION_ID, mToStationId);
        getActivity().startActivityForResult(intent, RESULT_SEARCH_FROM, null);
    }

    @OnClick(R.id.swap_station)
    protected void onSwapStationClick() {
        //TODO: replace spinner with new screen
        String mtemp = mFromStationId;
        mFromStationId = mToStationId;
        mToStationId = mtemp;
        updateStationLabels();
        //  mToStation.setSelection((adapter.getPosition(stopHashMap.get(mToStationId)));
        refreshTrainSchedule();
    }

    private void updateStationLabels() {
        mToStation.setText(stopHashMap.containsKey(mToStationId) ?
                stopHashMap.get(mToStationId).getName() : "Select To Station");
        mFromStation.setText(stopHashMap.containsKey(mFromStationId) ?
                stopHashMap.get(mFromStationId).getName() : "Select From Station");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SEARCH_FROM || requestCode == RESULT_SEARCH_TO) {
            if (resultCode == Activity.RESULT_OK) {
                Stop stop = data.getParcelableExtra(SearchActivity.EXTRA_SELECTED_STATION);
                Log.d(TAG, "Stop name: " + stop.getName());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnItemClickListener=(ScheduleRecyclerAdapter.OnItemClickListener)getActivity();
    }
}
