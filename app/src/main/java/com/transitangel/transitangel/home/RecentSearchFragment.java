package com.transitangel.transitangel.home;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Train;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentSearchFragment extends Fragment implements RecentAdapter.OnItemClickListener {

    @BindView(R.id.tvRecents)
    TextView tvRecents;
    @BindView(R.id.rvRecents)
    RecyclerView rvRecents;

    List<RecentsItem> recentsItemList;

    private ShowNotificationListener mNotificationListener;

    public RecentSearchFragment() {
    }

    public static RecentSearchFragment newInstance() {
        RecentSearchFragment fragment = new RecentSearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_search, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        Date today = new Date();
        ArrayList<Train> trains = CaltrainTransitManager.getSharedInstance().fetchTrains("70021", "70242", 5, today, true);
        // Creating a dummy recents list.
        recentsItemList = new ArrayList<>();
        for (Train train : trains) {
            recentsItemList.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()));
        }

        for (Train train : trains) {
            recentsItemList.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()));
        }

        for (Train train : trains) {
            recentsItemList.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()));
        }

        for (Train train : trains) {
            recentsItemList.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size() - 1).getStopId()));
        }

        // Create the recents adapter.
        RecentAdapter adapter = new RecentAdapter(getActivity(), recentsItemList);
        rvRecents.setAdapter(adapter);
        rvRecents.setLayoutManager(new LinearLayoutManager(getActivity()));
//        rvRecents.setNestedScrollingEnabled(false);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof  ShowNotificationListener) {
            mNotificationListener = (ShowNotificationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ShowNotificationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNotificationListener = null;
    }

    @Override
    public void onItemClick(int position) {
        RecentsItem recent = recentsItemList.get(position);
        mNotificationListener.showNotification(recent.from + " to " + recent.to);
    }
}
