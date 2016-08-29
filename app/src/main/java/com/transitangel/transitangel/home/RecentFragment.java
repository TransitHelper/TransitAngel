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

import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Trip;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentFragment extends Fragment implements RecentAdapter.OnItemClickListener {

    @BindView(R.id.rvRecents)
    RecyclerView rvRecents;

    @BindView(R.id.tvNoRecents)
    TextView tvNoRecents;

    private RecentAdapter adapter;

    private ArrayList<Trip> recentTripLists;
    private ArrayList<Trip> searchTripLists;

    public RecentFragment() {}

    public static RecentFragment newInstance() {
        RecentFragment fragment = new RecentFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        if(recentTripLists == null) {
            recentTripLists = new ArrayList<>();
        }

        if(searchTripLists == null) {
            searchTripLists = new ArrayList<>();
        }

        // Create the recents adapter.
        adapter = new RecentAdapter(getActivity(), recentTripLists, searchTripLists);
        rvRecents.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        rvRecents.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvRecents.setNestedScrollingEnabled(false);

    }

    @Override
    public void onItemClick(int position) {
        if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_VIEW_MORE_TYPE) {
            Toast.makeText(getActivity(), "show all trips", Toast.LENGTH_LONG);
        } else if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_TYPE) {
            // Subtract the position of header
            position = adapter.getRecentTripPosition(position);
            Trip trip = recentTripLists.get(position);
            Toast.makeText(getActivity(), trip.getFromStop().getName() + " to " + trip.getToStop().getName(), Toast.LENGTH_LONG).show();
        } else if(adapter.getItemViewType(position) ==  RecentAdapter.RECENT_SEARCH_ITEM_TYPE) {
            // Subtract header and recent trips
            position = adapter.getSearchListPosition(position);
            Trip trip = searchTripLists.get(position);
            Toast.makeText(getActivity(), trip.getFromStop().getName() + " to " + trip.getToStop().getName(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        checkListUpdated();
    }

    private void checkListUpdated() {
        boolean isUpdated = false;
        ArrayList<Trip> cachedRecentTrip = TransitManager.getSharedInstance().fetchRecentTripList();
        // It's updated
        if(cachedRecentTrip != null && cachedRecentTrip.size() != recentTripLists.size()) {
            recentTripLists.clear();
            recentTripLists.addAll(cachedRecentTrip);
            isUpdated = true;
        } else if(cachedRecentTrip == null && recentTripLists.size() > 0) {
            recentTripLists.clear();
            isUpdated = true;
        }

        ArrayList<Trip> cachedRecentSearch = TransitManager.getSharedInstance().fetchRecentSearchList();
        // It's updated
        if(cachedRecentSearch != null && cachedRecentSearch.size() != searchTripLists.size()) {
            searchTripLists.clear();
            searchTripLists.addAll(cachedRecentSearch);
            isUpdated = true;
        } else if(cachedRecentSearch == null && searchTripLists.size() > 0) {
            searchTripLists.clear();
            isUpdated = true;
        }

        if(isUpdated) {
            updateList();
        }

    }

    private void updateList() {
        adapter.updateData(recentTripLists, searchTripLists);
    }
}
