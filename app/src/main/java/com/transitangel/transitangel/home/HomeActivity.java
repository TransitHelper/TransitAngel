package com.transitangel.transitangel.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.Intent.ShakerService;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.details.AlarmBroadcastReceiver;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.model.sampleJsonModel;
import com.transitangel.transitangel.ongoing.OnGoingActivity;
import com.transitangel.transitangel.schedule.ScheduleActivity;
import com.transitangel.transitangel.utils.ShakeListener;
import com.transitangel.transitangel.utils.TAConstants;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity implements ShowNotificationListener,ShakeListener.Callback {

    public static final String ACTION_SHOW_ONGOING = "ACTION_SHOW_ONGOING";
    public static final String ACTION_TRIP_CANCELLED = "ACTION_TRIP_CANCELLED";
    public static final int ALARM_REQUEST_CODE = 111;
    public static final String HOME_STATION_SET = "home_station_set";
    public static final String WORK_STATION_SET = "work_station_set";
    public static final String PLACE_SAVED_SET = "place_saved_set";
    private static SharedPreferences mSharedPreference;


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
    @BindView(R.id.layout_on_going)
    ViewGroup mLayoutOnGoing;
    @BindView(R.id.layout_home)
    ViewGroup mLayoutHome;
    @BindView(R.id.layout_work)
    ViewGroup mLayoutWork;
    @BindView(R.id.layout_fav)
    ViewGroup mLayoutFav;
    @BindView(R.id.layout_search)
    ViewGroup mLayoutSearch;


    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        init();
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));
//        TestManager.getSharedInstance().executeSampleAPICalls(this);
        //ShakeListener shakeListener = new ShakeListener(this,3,100,this);
        Intent intent = new Intent(this,ShakerService.class);
        startService(intent);
    }

    private void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        tvTitle.setText(getString(R.string.home_title));
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        nsvContent.setFillViewport(true);
        mSharedPreference = getApplicationContext().getSharedPreferences(TAConstants.SharedPrefGeofences, Context.MODE_PRIVATE);
        setupOnGoingView();
        setupSearchView();
        setUpGetMeHomeView();
        setUpGetMeWorkView();
        setUpSavedTrips();

        // Hack to avoid recycler view scrolling to middle.
        nsvContent.post(() -> nsvContent.scrollTo(0, 0));

        String action = getIntent().getAction();
        if (!TextUtils.isEmpty(action)) {
            if (action.equalsIgnoreCase(ACTION_SHOW_ONGOING)) {
                Toast.makeText(this, "Show on going screen here.", Toast.LENGTH_LONG).show();
                launchOnGoingScreen();
            } else if (action.equalsIgnoreCase(ACTION_TRIP_CANCELLED)) {
                Toast.makeText(this, "Show on cancelled trip clicked.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupOnGoingView() {
        ImageView imageView = (ImageView) mLayoutOnGoing.findViewById(R.id.on_going_icon);
        imageView.setImageResource(R.mipmap.ic_current_trip);
        TextView textView = (TextView) mLayoutOnGoing.findViewById(R.id.on_going_trip_name);
        textView.setText("On Going Trip");
    }


    private void setUpGetMeWorkView() {
        ImageView imageView = (ImageView) mLayoutWork.findViewById(R.id.place_item_icon);
        imageView.setImageResource(R.mipmap.ic_work);
        TextView textView = (TextView) mLayoutWork.findViewById(R.id.place_item_title);
        textView.setText("Get Me To Work");
        TextView textViewSet = (TextView) mLayoutWork.findViewById(R.id.set_place);
        if (mSharedPreference.getBoolean(WORK_STATION_SET, false)) {
            //TODO:get a good description
            textView.setContentDescription("tap to start trip to work");
            textViewSet.setVisibility(View.GONE);
        } else {
            textView.setContentDescription("tap to set work station");
            textViewSet.setVisibility(View.VISIBLE);
        }

    }

    private void setupSearchView() {
        ImageView imageView = (ImageView) mLayoutSearch.findViewById(R.id.place_item_icon);
        imageView.setImageResource(R.mipmap.ic_home_search);
        TextView textView = (TextView) mLayoutSearch.findViewById(R.id.place_item_title);
        textView.setText("Schedules");
    }

    private void setUpGetMeHomeView() {
        ImageView imageView = (ImageView) mLayoutHome.findViewById(R.id.place_item_icon);
        imageView.setImageResource(R.mipmap.ic_home);
        TextView textView = (TextView) mLayoutHome.findViewById(R.id.place_item_title);
        textView.setText("Get Me Home");
        TextView textViewSet = (TextView) mLayoutHome.findViewById(R.id.set_place);
        if (mSharedPreference.getBoolean(HOME_STATION_SET, false)) {
            //TODO:get a good description
            textView.setContentDescription("tap to start trip to Home");
            textViewSet.setVisibility(View.GONE);
        } else {
            textView.setContentDescription("tap to set home station");
            textViewSet.setVisibility(View.VISIBLE);
            textViewSet.setText("SET");
        }
    }


    private void setUpSavedTrips() {
        ImageView imageView = (ImageView) mLayoutFav.findViewById(R.id.place_item_icon);
        imageView.setImageResource(R.mipmap.ic_save);
        TextView textView = (TextView) mLayoutFav.findViewById(R.id.place_item_title);
        textView.setText("Saved Places");
        TextView textViewSet = (TextView) mLayoutFav.findViewById(R.id.set_place);
        textView.setContentDescription("tap to view saved places");
        textViewSet.setVisibility(View.GONE);
    }

    private void launchOnGoingScreen() {
        Trip trip = PrefManager.getOnGoingTrip();
        if(trip != null) {
            Intent intent = new Intent(this, OnGoingActivity.class);
            if (trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                intent.putExtra(OnGoingActivity.EXTRA_SERVICE, OnGoingActivity.EXTRA_SERVICE_BART);
            } else {
                intent.putExtra(OnGoingActivity.EXTRA_SERVICE, OnGoingActivity.EXTRA_SERVICE_CALTRAIN);
            }
            intent.putExtra(OnGoingActivity.EXTRA_TRAIN, trip.getSelectedTrain());
            intent.putExtra(OnGoingActivity.EXTRA_FROM_STATION, trip.getFromStop().getId());
            intent.putExtra(OnGoingActivity.EXTRA_TO_STATION, trip.getToStop().getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No trip information found", Toast.LENGTH_LONG).show();
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
        if (item.getItemId() == R.id.action_search) {
            onScheduleClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == TransitLocationManager.GET_LOCATION_REQUEST_CODE) {
            TransitLocationManager.getSharedInstance().getCurrentLocation(this, new TransitLocationManager.LocationResponseHandler() {
                @Override
                public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                    //testHandleOnLocationReceived(isSuccess,latLng);
                }
            });
        } else if (requestCode == TransitLocationManager.GET_UPDATES_LOCATION_REQUEST_CODE) {
            TransitLocationManager.getSharedInstance().getLocationUpdates(this);
        }
    }

    @OnClick(R.id.layout_on_going)
    public void onGoingClicked() {
        launchOnGoingScreen();
    }

    @OnClick(R.id.fabStartTrip)
    public void onStartTripClicked() {
        //Adding Geofence to the last trip
        //TODO: based on user selected station add geofence
        AddGeoFenceToSelectedStops();
        //SetUp Alaram
        AddAlarmToSelectedStops();
        //Start Notification
        TrainStop trainStop = new TrainStop();
        trainStop.setName("SFO");
        trainStop.setStopId("323");
        startTripNotification(trainStop);
    }

    private void startTripNotification(TrainStop trainStop) {
        //TODO: based on user selected station add geofence
        // addGeoFenceToSelectedStops();
//        AddAlarmToSelectedStops();
//        Trip trip = new Trip();
//        trip.setSelectedTrain("323");
//        trip.setFromStop(trainStop.getStopId());
//        trip.setToStop(stopHashMap.get(toStation));
//        trip.setDate(new Date());
//        trip.setType(TAConstants.TRANSIT_TYPE.CALTRAIN);
//        PrefManager.addOnGoingTrip(trip);
//        NotificationProvider.getInstance().showTripStartedNotification(this, trainStop.getStopId());
        // TODO: Need to add start trip integration here.
        // TODO: After starting a trip we need to remove the older one.
    }

    @OnClick(R.id.layout_search)
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

        // Set ongoing if there is a trip.
        Trip trip = PrefManager.getOnGoingTrip();
        if(trip != null) {
            mLayoutOnGoing.setVisibility(View.VISIBLE);
        } else {
            mLayoutOnGoing.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdatesReceiver);
    }

    private BroadcastReceiver locationUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                double latitude = intent.getDoubleExtra("Latitude", 0.0);
                double longitude = intent.getDoubleExtra("Longitude", 0.0);
                Log.d("Location Update", "Update received");
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

    @Override
    public void shakingStarted() {
        Log.d("Shaking","Started");
    }

    @Override
    public void shakingStopped() {
        Log.d("Shaking","Stopped");
        final Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        Toast.makeText(this,"Shaked!!",Toast.LENGTH_SHORT);
    }
}
