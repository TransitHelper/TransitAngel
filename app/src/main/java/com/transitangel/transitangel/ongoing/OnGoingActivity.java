package com.transitangel.transitangel.ongoing;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.search.StationsAdapter;
import com.transitangel.transitangel.utils.Preconditions;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnGoingActivity extends AppCompatActivity implements StationsAdapter.OnItemClickListener {

    private static final String TAG = OnGoingActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_STATION = TAG + ".EXTRA_SELECTED_STATION";
    public static final String EXTRA_TRAIN = TAG + ".EXTRA_TRAIN";
    public static final String EXTRA_SERVICE = TAG + ".EXTRA_SERVICE";
    public static final String EXTRA_SERVICE_BART = TAG + ".EXTRA_SERVICE_BART";
    public static final String EXTRA_SERVICE_CALTRAIN = TAG + ".EXTRA_SERVICE_CALTRAIN";
    public static final String EXTRA_FROM_STATION = TAG + ".EXTRA_FROM_STATION";
    public static final String EXTRA_TO_STATION = TAG + ".EXTRA_TO_STATION";
    public static final int ALARM_REQUEST_CODE = 111;


    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;
    @BindView(R.id.clMainContent)
    CoordinatorLayout clMainContent;

    private ArrayList<TrainStop> mStops;
    private Train train;
    private StationsAdapter adapter;
    private String serviceType;
    private String fromStation;
    private String toStation;
    private TAConstants.TRANSIT_TYPE type;

    HashMap<String, Stop> stopHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        serviceType = getIntent().getStringExtra(EXTRA_SERVICE);
        train = getIntent().getParcelableExtra(EXTRA_TRAIN);
        fromStation = getIntent().getStringExtra(EXTRA_FROM_STATION);
        toStation = getIntent().getStringExtra(EXTRA_TO_STATION);

        Preconditions.checkNull(fromStation);
        Preconditions.checkNull(toStation);
        Preconditions.checkNull(serviceType);
        Preconditions.checkNull(train);

        mStops = train.getTrainStops();
        if (EXTRA_SERVICE_CALTRAIN.equalsIgnoreCase(serviceType)) {
            type = TAConstants.TRANSIT_TYPE.CALTRAIN;
            stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        } else {
            type = TAConstants.TRANSIT_TYPE.BART;
            stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
        }
        setSupportActionBar(toolbar);
        tvTitle.setText("#" + train.getNumber());
        tvTitle.setContentDescription("Train number " + train.getNumber());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        // Create the recents adapter.
        adapter = new StationsAdapter(this, mStops, StationsAdapter.ITEM_ONGOING);
        rvStationList.setAdapter(adapter);
        rvStationList.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ongoing, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_favorite) {
            Toast.makeText(this, "Under developement", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.action_close) {
            cancelTrip();
        }

        return super.onOptionsItemSelected(item);
    }

    public void cancelTrip() {
        PrefManager.removeOnGoingTrip();
        NotificationProvider.getInstance().dismissOnGoingNotification(this);
        Toast.makeText(OnGoingActivity.this, "Trip Cancelled.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onCheckBoxSelected(int position) {

    }

    @Override
    public void onCheckBoxUnSelected(int position) {

    }
}
