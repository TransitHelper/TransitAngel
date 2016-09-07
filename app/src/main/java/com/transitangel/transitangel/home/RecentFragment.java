package com.transitangel.transitangel.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.details.DetailsActivity;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.schedule.ScheduleActivity;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentFragment extends Fragment implements RecentAdapter.OnItemClickListener, RecentAdapter.OnMoreMenuClickListener {

    @BindView(R.id.rvRecents)
    RecyclerView rvRecents;

    @BindView(R.id.emptyView)
    ViewGroup mEmptyView;

    private RecentAdapter adapter;
    private TextView mEmptyTextView;
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
        adapter.setOnMoreMenuClickListener(this);
        rvRecents.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvRecents.setNestedScrollingEnabled(false);
    }

    @Override
    public void onItemClick(int position) {
        if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_VIEW_MORE_TYPE) {
            Toast.makeText(getActivity(), getString(R.string.show_all_trips), Toast.LENGTH_LONG);
        } else if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_TYPE) {
            // Subtract the position of header
            position = adapter.getRecentTripPosition(position);
            Trip trip = recentTripLists.get(position);
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            if (trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_BART);
            } else {
                intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_CALTRAIN);
            }
            intent.putExtra(DetailsActivity.EXTRA_TRAIN, trip.getSelectedTrain());
            intent.putExtra(DetailsActivity.EXTRA_FROM_STATION_ID, trip.getFromStop().getId());
            intent.putExtra(DetailsActivity.EXTRA_TO_STATION_ID, trip.getToStop().getId());
            startActivity(intent);
        } else if(adapter.getItemViewType(position) ==  RecentAdapter.RECENT_SEARCH_ITEM_TYPE) {
            // Subtract header and recent trips
            position = adapter.getSearchListPosition(position);
            Trip trip = searchTripLists.get(position);
            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
            TAConstants.TRANSIT_TYPE type = trip.getType();
            intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, type);
            intent.putExtra(ScheduleActivity.FROM_STATION_ID, trip.getFromStop().getId());
            intent.putExtra(ScheduleActivity.TO_STATION_ID, trip.getToStop().getId());
            startActivity(intent);
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

        if(recentTripLists.isEmpty() && searchTripLists.isEmpty()) {
            showNoRecentTrips();
        } else {
            hideNoRecentTrips();
        }

    }

    private void updateList() {
        adapter.updateData(recentTripLists, searchTripLists);
    }

    @Override
    public void onMenuItemClicked(int position, View view) {
      if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_TYPE) {
            position = adapter.getRecentTripPosition(position);
            showPopup(recentTripLists.get(position), view);
        } else if(adapter.getItemViewType(position) ==  RecentAdapter.RECENT_SEARCH_ITEM_TYPE) {
            position = adapter.getSearchListPosition(position);
            showPopup(searchTripLists.get(position), view);
      }
    }

    private void showPopup(final Trip trip, View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_create_shortcut:
                      //  Toast.makeText(getActivity(), "Create a shortcut here", Toast.LENGTH_LONG).show();
                        if (trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                            BartTransitManager.getSharedInstance().createShortCut(trip);
                        } else {
                            CaltrainTransitManager.getSharedInstance().createShortCut(trip);
                        }
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showNoRecentTrips() {
        mEmptyView.setVisibility(View.VISIBLE);
        mEmptyTextView = (TextView) mEmptyView.findViewById(R.id.empty_state_description);
        mEmptyTextView.setText(R.string.no_recent_trips_or_searches);
    }

    private void hideNoRecentTrips() {
        mEmptyView.setVisibility(View.GONE);
    }
}
