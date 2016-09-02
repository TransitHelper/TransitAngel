package com.transitangel.transitangel.details;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
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

import com.google.gson.Gson;
import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.GeofenceManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.TrainStopFence;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.search.StationsAdapter;
import com.transitangel.transitangel.utils.DateUtil;
import com.transitangel.transitangel.utils.Preconditions;
import com.transitangel.transitangel.utils.TAConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsActivity extends AppCompatActivity implements StationsAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;
    @BindView(R.id.clMainContent)
    CoordinatorLayout clMainContent;

    private static final String TAG = DetailsActivity.class.getSimpleName();
    public static final String EXTRA_SELECTED_STATION = TAG + ".EXTRA_SELECTED_STATION";
    public static final String EXTRA_TRAIN = TAG + ".EXTRA_TRAIN";
    public static final String EXTRA_SERVICE = TAG + ".EXTRA_SERVICE";
    public static final String EXTRA_SERVICE_BART = TAG + ".EXTRA_SERVICE_BART";
    public static final String EXTRA_SERVICE_CALTRAIN = TAG + ".EXTRA_SERVICE_CALTRAIN";
    public static final String EXTRA_FROM_STATION = TAG + ".EXTRA_FROM_STATION";
    public static final String EXTRA_TO_STATION = TAG + ".EXTRA_TO_STATION";
    public static final int ALARM_REQUEST_CODE = 111;
    private static GeofenceManager.GeofenceManagerListener mGeofenceManagerListener;

    private ArrayList<TrainStop> mStops;
    private Train train;
    private StationsAdapter adapter;
    private String serviceType;
    private String fromStation;
    private String toStation;
    private TAConstants.TRANSIT_TYPE type;
    private TrainStop selectedStop;
    private Trip selectedTrip;
    private Trip trip;
    private ArrayList<PendingIntent> mPendingIntents = new ArrayList<>();
    private ArrayList<TrainStop> mAlarmStops = new ArrayList<>();
    private ArrayList<TrainStop>geofenceStops = new ArrayList<>();
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
        adapter = new StationsAdapter(this, mStops, StationsAdapter.ITEM_DETAIL);
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
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.search);

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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckBoxSelected(int position) {
        mStops.get(position).setNotify(true);
        mAlarmStops.add(mStops.get(position));
    }

    @Override
    public void onCheckBoxUnSelected(int position) {
        mStops.get(position).setNotify(false);
        mAlarmStops.remove(mStops.get(position));
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

    @OnClick(R.id.btnStartTrip)
    public void startTrip() {
        TrainStop lastStop = getStopFromId(toStation);
        if (lastStop == null) {
            Toast.makeText(this, "No Valid to station found for the train", Toast.LENGTH_LONG).show();
            return;
        }
        if (PrefManager.getOnGoingTrip() != null) {
            showAlertDialog();
        } else {
            startNewTrip();
        }
    }

    private void showAlertDialog() {
        //TODO: have custom alertview
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Your current Trip is not completed!")
                .setMessage("Are you sure you want to start new trip")
                .setPositiveButton("Start Trip", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        GeofenceManager.getSharedInstance().removeAllGeofences(getmGeofenceManagerListener());//Have not tested
                        RemoveCurrentTripAlarms();
                        startNewTrip();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void RemoveCurrentTripAlarms() {
//        try {
//            Type type = new TypeToken<List<PendingIntent>>() {}.getType();
//            Gson gson = new Gson();
//            String json = Prefs.getString(TAConstants.AlarmIntents, "");
//            List<PendingIntent> intentList = gson.fromJson(json, type);
//            for (PendingIntent intent : intentList
//                    ) {
//                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                alarmManager.cancel(intent);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
    }

    private void startNewTrip() {
        trip = new Trip();
        trip.setSelectedTrain(train);
        trip.setFromStop(stopHashMap.get(fromStation));
        trip.setToStop(stopHashMap.get(toStation));
        trip.setDate(new Date());
        trip.setType(type);
        PrefManager.addOnGoingTrip(trip);
        addGeoFencesandAlarm();
        startOnGoingNotification(trip);
        TransitManager.getSharedInstance().saveRecentTrip(trip);
    }

    private void addGeoFencesandAlarm() {
        geofenceStops = new ArrayList<>();
        geofenceStops.addAll(mAlarmStops);
        addGeoFenceToSelectedStops(trip);
        for (TrainStop stop : mAlarmStops) {
            int requestCode = ALARM_REQUEST_CODE + stop.getStopOrder();//find better way to do it
            addAlarmToSelectedStops(stop, requestCode);
        }
    }

    //recursively add geofence , add the next fence only when the fence has been updated
    private void addGeoFenceToSelectedStops(Trip trip) {
        if ( geofenceStops.size() > 0 ) {
            try {
                selectedStop = geofenceStops.get(0);
                TrainStopFence trainStopFence = new TrainStopFence(selectedStop);
                selectedTrip = trip;
                GeofenceManager.getSharedInstance().addGeofence(this, trainStopFence, new GeofenceManager.GeofenceManagerListener() {
                    @Override
                    public void onGeofencesUpdated() {
                        geofenceStops.remove(selectedStop);
                        addGeoFenceToSelectedStops(selectedTrip);
                    }

                    @Override
                    public void onError() {
                        //do nothing?
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

    }

    public GeofenceManager.GeofenceManagerListener getmGeofenceManagerListener() {
        if (mGeofenceManagerListener == null) {
            mGeofenceManagerListener = new GeofenceManager.GeofenceManagerListener() {
                @Override
                public void onGeofencesUpdated() {
                    Log.e(DetailsActivity.class.getSimpleName(), "GeoFence Added");
                }

                @Override
                public void onError() {
                    Toast.makeText(DetailsActivity.this, "Error while adding location, please check location services and try again", Toast.LENGTH_LONG).show();

                }
            };
        }
        return mGeofenceManagerListener;
    }


    private void startOnGoingNotification(Trip trip) {
        NotificationProvider.getInstance().showTripStartedNotification(this, trip);
    }

    private void addAlarmToSelectedStops(TrainStop lastStop, int requestCode) {
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        Gson gson = new Gson();
        String json = gson.toJson(lastStop);
        intent.putExtra(AlarmBroadcastReceiver.ARG_STOP, json);
        intent.putExtra(AlarmBroadcastReceiver.TRIP_ID,trip.getTripId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final Timestamp timestamp = DateUtil.getTimeStamp(lastStop.getArrrivalTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        calendar.add(Calendar.MINUTE,-3);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);
    }

    public TrainStop getStopFromId(String stopId) {
        for (TrainStop trainStop : mStops) {
            if (trainStop.getStopId().equalsIgnoreCase(stopId)) {
                return trainStop;
            }
        }
        return null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == GeofenceManager.GEOFENCE_GET_FINE_LOC_REQ_CODE) {
            if (selectedStop != null && selectedTrip != null) {
                addGeoFenceToSelectedStops(selectedTrip);
            }
        }
    }
}
