package com.transitangel.transitangel.home;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Trip;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NearByFragment extends Fragment implements RecentAdapter.OnItemClickListener {

    @BindView(R.id.rvRecents)
    RecyclerView rvRecents;

    ArrayList<Trip> combinedList;

    public NearByFragment() {}

    public static NearByFragment newInstance() {
        NearByFragment fragment = new NearByFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        // Creating a dummy recents list.
        ArrayList<Trip> recentTripLists = TransitManager.getSharedInstance().fetchRecentTripList();
        ArrayList<Trip> searchTripLists = TransitManager.getSharedInstance().fetchRecentSearchList();

        combinedList = new ArrayList<>();
        combinedList.addAll(recentTripLists);
        combinedList.addAll(searchTripLists);

        // Create the recents adapter.
        RecentAdapter adapter = new RecentAdapter(getActivity(), combinedList);
        rvRecents.setAdapter(adapter);
        rvRecents.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvRecents.setNestedScrollingEnabled(false);
        adapter.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(int position) {
        Trip trip = combinedList.get(position);
        Toast.makeText(getActivity(), trip.getFromStop().getName() + " to " + trip.getToStop().getName(), Toast.LENGTH_LONG).show();
    }
}
