package com.transitangel.transitangel.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.Manager.TestManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.details.AlarmBroadcastReceiver;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.sampleJsonModel;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.schedule.ScheduleActivity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity implements ShowNotificationListener {

    public static final String ACTION_SHOW_ONGOING = "ACTION_SHOW_ONGOING";
    public static final String ACTION_TRIP_CANCELLED = "ACTION_TRIP_CANCELLED";
    public static final int ALARM_REQUEST_CODE = 111;


    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nsvContent)
    NestedScrollView nsvContent;
    @BindView(R.id.fabStartTrip)
    FloatingActionButton fabAdd;
    @BindView(R.id.clMainContent)
    CoordinatorLayout clMainContent;
    @BindView(R.id.sliding_tabs)
    TabLayout slidingTabs;
    @BindView(R.id.viewpager)
    ViewPager viewpager;

    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        init();
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));
        TestManager.getSharedInstance().executeSampleAPICalls();
    }

    private void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        tvTitle.setText(getString(R.string.home_title));
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        nsvContent.setFillViewport(true);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new RecentsFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Hack to avoid recycler view scrolling to middle.
        nsvContent.post(() -> nsvContent.scrollTo(0, 0));
        String action = getIntent().getAction();
        if(!TextUtils.isEmpty(action)) {
            if(action.equalsIgnoreCase(ACTION_SHOW_ONGOING)) {
                Toast.makeText(this, "Show on going screen here.", Toast.LENGTH_LONG).show();
            } else if (action.equalsIgnoreCase(ACTION_TRIP_CANCELLED)) {
                Toast.makeText(this, "Show on cancelled trip clicked.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search) {
            onScheduleClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if ( requestCode == TransitLocationManager.GET_LOCATION_REQUEST_CODE) {
            TransitLocationManager.getSharedInstance().getCurrentLocation(this, new TransitLocationManager.LocationResponseHandler() {
                @Override
                public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                    //testHandleOnLocationReceived(isSuccess,latLng);
                }
            });
        }
        else if ( requestCode == TransitLocationManager.GET_UPDATES_LOCATION_REQUEST_CODE ) {
            TransitLocationManager.getSharedInstance().getLocationUpdates(this);
        }
    }

    @OnClick(R.id.fabStartTrip)
    public void onStartTripClicked() {
        //Adding Geofence to the last trip
        //TODO: based on user selected station add geofence
        AddGeoFenceToSelectedStops();
        //SetUp Alaram
        AddAlarmToSelectedStops();
        //Start Notification
        TrainStop  trainStop = new TrainStop();
        trainStop.setName("SFO");
        trainStop.setStopId("323");
        startTripNotification(trainStop);
    }

    private void startTripNotification(TrainStop trainStop) {
        //TODO: based on user selected station add geofence
       // addGeoFenceToSelectedStops();
        AddAlarmToSelectedStops();
        NotificationProvider.getInstance().showTripStartedNotification(this, trainStop.getStopId());
    }

    @OnClick(R.id.btnSchedule)
    public void onScheduleClicked() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Avoid issue that recycler view gets automatic focus on start of the app.
        nsvContent.scrollTo(0, 0);

        //setup brodcast receiver
        IntentFilter intentFilter = new IntentFilter(TransitLocationManager.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdatesReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdatesReceiver);
    }

    private BroadcastReceiver locationUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null ) {
                double latitude = intent.getDoubleExtra("Latitude",0.0);
                double longitude = intent.getDoubleExtra("Longitude",0.0);
                Log.d("Location Update","Update received");
                TransitLocationManager.getSharedInstance().stop();
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mSubscription != null)
            mSubscription.clear();
        super.onDestroy();
    }



    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void handleResult(List<sampleJsonModel> response) {
        Log.e(HomeActivity.class.getSimpleName(), response.toString());
    }

    private void showSnackBar(View parent, String displayText) {
        Snackbar.make(parent, displayText, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showNotification(String text) {
        showSnackBar(clMainContent, text);
    }

    //Move it to schedule Activity
    private void AddAlarmToSelectedStops() {
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, timestamp.getHours());
        calendar.set(Calendar.MINUTE, timestamp.getMinutes());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);


        Toast.makeText(this, "Alarm will vibrate at time specified",
                Toast.LENGTH_SHORT).show();
    }

    private void AddGeoFenceToSelectedStops() {
//        TrainStopFence trainStopFence = new TrainStopFence(mStops.get(mStops.size() - 1), 15);
//        GeofenceManager.getSharedInstance().addGeofence(this, trainStopFence, new GeofenceManager.GeofenceManagerListener() {
//            @Override
//            public void onGeofencesUpdated() {
//                Log.d("Fence Added", "Here");
//            }
//
//            @Override
//            public void onError() {
//                Log.d("Error", "Error adding fence");
//            }
//        });
    }
}
