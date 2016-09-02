package com.transitangel.transitangel.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.transitangel.transitangel.Intent.ShakerService;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.details.DetailsActivity;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.schedule.ScheduleActivity;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity {

    public static final String ACTION_SHOW_ONGOING = "ACTION_SHOW_ONGOING";
    public static final String ACTION_TRIP_CANCELLED = "ACTION_TRIP_CANCELLED";
    public static final String ACTION_SHORTCUT = "ACTION_SHORTCUT";
    public static final String EXTRA_SHORTCUT_TRIP_ID = "EXTRA_SHORTCUT_TRIP_ID";

    private static SharedPreferences mSharedPreference;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.home_pager)
    ViewPager homePager;

    private HomePagerAdapter adapter;
    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    private String[] titles = {"Near by", "Recents", "Live trip"};

    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        init();
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));
//        TestManager.getSharedInstance().executeSampleAPICalls(this);
        Intent serviceIntent = new Intent(this, ShakerService.class);
        startService(serviceIntent);

        //TO check if TTS in installed
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

    }


    //act on result of TTS data check
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void init() {
        mSharedPreference = getApplicationContext().getSharedPreferences(TAConstants.SharedPrefGeofences, Context.MODE_PRIVATE);
        TabLayout.Tab nearbyTab = tabLayout.newTab();
        nearbyTab.setContentDescription(titles[0]);
        nearbyTab.setText(titles[0]);
        nearbyTab.setIcon(R.drawable.ic_explore_white_48dp);
        tabLayout.addTab(nearbyTab);

        TabLayout.Tab recentsTab = tabLayout.newTab();
        recentsTab.setContentDescription(titles[1]);
        recentsTab.setText(titles[1]);
        recentsTab.setIcon(R.drawable.ic_restore_white_48dp);
        tabLayout.addTab(recentsTab);

        TabLayout.Tab onGoingTab = tabLayout.newTab();
        onGoingTab.setContentDescription(titles[2]);
        onGoingTab.setText(titles[2]);
        onGoingTab.setIcon(R.drawable.ic_play_arrow_white_48dp);
        tabLayout.addTab(onGoingTab);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        adapter = new HomePagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        homePager.setAdapter(adapter);
        homePager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        homePager.setOffscreenPageLimit(3);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                homePager.setCurrentItem(tab.getPosition());
                Fragment fragment = adapter.getRegisteredFragment(tab.getPosition());
                if (fragment instanceof LiveTripFragment) {
                    ((LiveTripFragment) fragment).onSelected();
                } else {
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        String action = getIntent().getAction();
        if (!TextUtils.isEmpty(action)) {
            if (action.equalsIgnoreCase(ACTION_SHOW_ONGOING)) {
                Toast.makeText(this, "Show on going screen here.", Toast.LENGTH_LONG).show();
                launchOnGoingScreen();
            } else if (action.equalsIgnoreCase(ACTION_TRIP_CANCELLED)) {
                Toast.makeText(this, "Show on cancelled trip clicked.", Toast.LENGTH_LONG).show();
            } else if (action.equalsIgnoreCase(ACTION_SHORTCUT)) {
                String tripId = getIntent().getStringExtra(EXTRA_SHORTCUT_TRIP_ID);
                ArrayList<Trip> cachedRecentTrip = TransitManager.getSharedInstance().fetchRecentTripList();
                for (Trip trip : cachedRecentTrip) {
                    if (trip.getTripId().equalsIgnoreCase(tripId)) {
                        launchStartTrip(trip);
                        return;
                    }
                }
            }
        }
    }

    private void launchStartTrip(Trip trip) {
//        Toast.makeText(this, "Short cut clicked from home screen with Trip ID : " + trip.getTripId(), Toast.LENGTH_LONG).show();
        if (trip != null) {
            //show the details activity for the trip
            Intent intent = new Intent(this, DetailsActivity.class);
            if (trip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_BART);
            } else {
                intent.putExtra(DetailsActivity.EXTRA_SERVICE, DetailsActivity.EXTRA_SERVICE_CALTRAIN);
            }
            intent.putExtra(DetailsActivity.EXTRA_TRAIN, trip.getSelectedTrain());
            intent.putExtra(DetailsActivity.EXTRA_FROM_STATION, trip.getFromStop().getId());
            intent.putExtra(DetailsActivity.EXTRA_TO_STATION, trip.getToStop().getId());
            startActivity(intent);
        }
    }


    private void launchOnGoingScreen() {
        // Set the current item to live notifications.
        homePager.setCurrentItem(2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == TransitLocationManager.GET_LOCATION_REQUEST_CODE) {
            //reload the recents fragment
            NearByFragment nearByFragment = (NearByFragment) adapter.getRegisteredFragment(0); //first fragment
            if (nearByFragment != null) {
                nearByFragment.loadCurrentStops();
            }

        } else if (requestCode == TransitLocationManager.GET_UPDATES_LOCATION_REQUEST_CODE) {
            TransitLocationManager.getSharedInstance().getLocationUpdates(this);
        }
    }


    @OnClick(R.id.search)
    public void onSearchClicked() {
        onScheduleClicked();
    }

    public void onScheduleClicked() {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mSubscription != null)
            mSubscription.clear();
        super.onDestroy();
    }
}
