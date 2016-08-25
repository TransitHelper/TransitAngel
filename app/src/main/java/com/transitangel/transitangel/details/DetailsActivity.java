package com.transitangel.transitangel.details;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.GeofenceManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.TrainStopFence;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.search.StationsAdapter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends AppCompatActivity implements StationsAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_STATION = TAG + ".EXTRA_SELECTED_STATION";
    public static final String EXTRA_TRAIN = TAG + ".EXTRA_TRAIN";
    public static final String EXTRA_SERVICE = TAG + ".EXTRA_SERVICE";
    public static final String EXTRA_SERVICE_BART = TAG + ".EXTRA_SERVICE_BART";
    public static final String EXTRA_SERVICE_CALTRAIN = TAG + ".EXTRA_SERVICE_CALTRAIN";
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
    HashMap<String, Stop> stopHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        serviceType = getIntent().getStringExtra(EXTRA_SERVICE);
        train = getIntent().getParcelableExtra(EXTRA_TRAIN);
        mStops = train.getTrainStops();
        if (EXTRA_SERVICE_CALTRAIN.equalsIgnoreCase(serviceType)) {
            stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        } else {
            stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
        }
        setSupportActionBar(toolbar);
        tvTitle.setText("#" + train.getNumber());
        tvTitle.setContentDescription("Train number " + train.getNumber());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the recents adapter.
        adapter = new StationsAdapter(this, mStops, stopHashMap);
        rvStationList.setAdapter(adapter);
        rvStationList.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        // Use a custom search icon for the SearchView in AppBar
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.search);

        // Customize searchview text and hint colors
        int searchEditId = android.support.v7.appcompat.R.id.search_src_text;
        EditText et = (EditText) searchView.findViewById(searchEditId);
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(Color.WHITE);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_favorite) {
            Toast.makeText(this, "Under developement", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.action_alarm) {
            Toast.makeText(this, "Under developement", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position) {
        Intent resultIntent = new Intent();
//        resultIntent.putExtra(EXTRA_SELECTED_STATION, adapter.getItem(position));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.setFilter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.setFilter(newText);
        return false;
    }

    @OnClick(R.id.fabStartTrip)
    public void startTrip() {
        //Adding Geofence to the last trip
        //TODO: based on user selected station add geofence
        addGeoFenceToSelectedStops();
        //SetUp Alaram
        addAlarmToSelectedStops();
        //Start Notification
        startOnGoingNotification();

    }

    private void startOnGoingNotification() {
        TrainStop lastStop = mStops.get(mStops.size() - 1);
        NotificationProvider.getInstance().showTripStartedNotification(this, lastStop.getStopId());
    }

    private void addAlarmToSelectedStops() {
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        TrainStop lastStop = mStops.get(mStops.size() - 1);
        final Timestamp timestamp =
                Timestamp.valueOf(
                        new SimpleDateFormat("yyyy-MM-dd ")
                                .format(new Date())
                                .concat(lastStop.getArrrivalTime())
                );
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, timestamp.getHours());
        calendar.set(Calendar.MINUTE, timestamp.getMinutes()-5);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);

        Toast.makeText(this, "Alarm will vibrate at time specified",
                Toast.LENGTH_SHORT).show();
    }

    private void addGeoFenceToSelectedStops() {
        TrainStopFence trainStopFence = new TrainStopFence(mStops.get(mStops.size() - 1), 15);
        GeofenceManager.getSharedInstance().addGeofence(this, trainStopFence, new GeofenceManager.GeofenceManagerListener() {
            @Override
            public void onGeofencesUpdated() {
                Log.d("Fence Added", "Here");
            }

            @Override
            public void onError() {
                Log.d("Error", "Error adding fence");
            }
        });
    }
}
