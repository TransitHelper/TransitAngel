package com.transitangel.transitangel.alltrips;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.details.DetailsActivity;
import com.transitangel.transitangel.home.OnItemClickListener;
import com.transitangel.transitangel.home.OnMoreMenuClickListener;
import com.transitangel.transitangel.home.RecentAdapter;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SeeAllTripActivity extends AppCompatActivity implements OnItemClickListener, OnMoreMenuClickListener {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvRecents)
    RecyclerView rvRecents;

    private ArrayList<Trip> recentTripLists;
    private SeeAllTripsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_trip);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(toolbar);
        tvTitle.setText(getString(R.string.title_all_trips));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        if(recentTripLists == null) {
            recentTripLists = new ArrayList<>();
        }

        // Create the recents adapter.
        adapter = new SeeAllTripsAdapter(this, recentTripLists);
        rvRecents.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setOnMoreMenuClickListener(this);
        rvRecents.setLayoutManager(new LinearLayoutManager(this));
        rvRecents.setNestedScrollingEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkListUpdated();
    }

    @Override
    public void onMenuItemClicked(int position, View view) {
        if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_TYPE) {
            position = adapter.getRecentTripPosition(position);
            showPopup(recentTripLists.get(position), view);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if(adapter.getItemViewType(position) == RecentAdapter.RECENT_TRIP_ITEM_TYPE) {
            // Subtract the position of header
            position = adapter.getRecentTripPosition(position);
            Trip trip = recentTripLists.get(position);
            Intent intent = new Intent(this, DetailsActivity.class);
            if (trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_BART);
            } else {
                intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_CALTRAIN);
            }
            intent.putExtra(DetailsActivity.EXTRA_TRAIN, trip.getSelectedTrain());
            intent.putExtra(DetailsActivity.EXTRA_FROM_STATION_ID, trip.getFromStop().getId());
            intent.putExtra(DetailsActivity.EXTRA_TO_STATION_ID, trip.getToStop().getId());
            String transitionName = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                transitionName = view.getTransitionName();
            }
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, transitionName);
            startActivity(intent, options.toBundle());
        }
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

        if(isUpdated) {
            adapter.updateData(recentTripLists);
        }
    }

    private void showPopup(final Trip trip, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_create_shortcut:
                    if (trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                        BartTransitManager.getSharedInstance().createShortCut(trip);
                    } else {
                        CaltrainTransitManager.getSharedInstance().createShortCut(trip);
                    }
                    return true;
            }
            return false;
        });
        popup.show();
    }
}
