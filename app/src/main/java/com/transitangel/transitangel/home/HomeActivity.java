package com.transitangel.transitangel.home;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TrafficNewsAlertResponseHandler;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.Manager.TweetAlertResponseHandler;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrafficNewsAlert;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.Tweet;
import com.transitangel.transitangel.model.sampleJsonModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends AppCompatActivity implements RecentAdapter.OnItemClickListener {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnSchedule)
    Button btnSchedule;
    @BindView(R.id.tvRecents)
    TextView tvRecents;
    @BindView(R.id.rvRecents)
    RecyclerView rvRecents;
    @BindView(R.id.nsvContent)
    NestedScrollView nsvContent;
    @BindView(R.id.fabStartTrip)
    FloatingActionButton fabAdd;
    @BindView(R.id.clMainContent)
    CoordinatorLayout clMainContent;

    List<RecentsItem> recentsItemList;

    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        init();
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));

        executeSampleAPICalls();


    }

    private void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        tvTitle.setText(getString(R.string.home_title));

        Date today = new Date();
        ArrayList<Train> trains = CaltrainTransitManager.getSharedInstance().fetchTrains("70021", "70242",5,today,false);
        Log.d("Trains from SF to MView", trains.toString());

        // Creating a dummy recents list.
        recentsItemList = new ArrayList<>();
        for(Train train: trains) {
            recentsItemList.add(new RecentsItem(train.getTrainStops().get(0).getStopId(), train.getTrainStops().get(train.getTrainStops().size()-1).getStopId()));
        }
        // Create the recents adapter.
        RecentAdapter adapter = new RecentAdapter(this, recentsItemList);
        rvRecents.setAdapter(adapter);
        rvRecents.setLayoutManager(new LinearLayoutManager(this));
        rvRecents.setNestedScrollingEnabled(false);
        adapter.setOnItemClickListener(this);

        // Hack to avoid recycler view scrolling to middle.
        nsvContent.post(() -> nsvContent.scrollTo(0,0));
    }

    private void executeSampleAPICalls() {

        //get all the services limited,local and babybullet
        ArrayList<Service> services = CaltrainTransitManager.getSharedInstance().getServices();

        //get all the stops
        ArrayList<Stop> stops = CaltrainTransitManager.getSharedInstance().getStops();
        //get hashmap for faster lookup of stop if you have stop id
        HashMap<String, Stop> stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        Log.d("Services", services.toString());
        Log.d("Stops", stops.toString());

        //fetch trains from SF to Santa Clara
        //Note: currently ignores the leaving after parameter and also ignore weekday/weekend

        //fetch trains arriving at a certain destination within a certain duration
        ArrayList<Train> arrivingTrains = CaltrainTransitManager.getSharedInstance().fetchTrainsArrivingAtDestination("70011",3);
        Log.d("Trains arriving station",arrivingTrains.toString());

        //bart stops
        ArrayList<Stop> bartStops = BartTransitManager.getSharedInstance().getStops();
        Log.d("Bart Stops",bartStops.toString());
        //bart services
        ArrayList<Service> bartServices = BartTransitManager.getSharedInstance().getServices();
        Log.d("Bart Services",bartServices.toString());
        // fetch trains from Fremont to Daly City
        //last boolean to include all trains irrespective of that day time or not
        ArrayList<Train> bartTrains = BartTransitManager.getSharedInstance().fetchTrains("12018519","12018513",-1,new Date(),true);
        Log.d("Fremont to DalyCity",bartTrains.toString());
        ArrayList<Train> arrivingBartTrains = BartTransitManager.getSharedInstance().fetchTrainsArrivingAtDestination("12018519",4);
        Log.d("Bart arriving fremont",arrivingBartTrains.toString());

        //fetch news alerts
        TransitManager.getSharedInstance().fetchLatestTrafficNewsAlerts(new TrafficNewsAlertResponseHandler() {
            @Override
            public void onNewsAlertsReceived(boolean isSuccess, ArrayList<TrafficNewsAlert> trafficNewsAlerts) {
                if ( isSuccess) {
                    Log.d("Traffic News Alerts",trafficNewsAlerts.toString());
                }
            }
        });

        //fetch tweets
        TransitManager.getSharedInstance().fetchTweetAlerts(new TweetAlertResponseHandler() {
            @Override
            public void onTweetsReceived(boolean isSuccess, ArrayList<Tweet> tweetAlerts) {
                if ( isSuccess) {
                    Log.d("Tweet alerts",tweetAlerts.toString());
                }
            }
        });

        //sample recents
//        ArrayList<Trip> recents = TransitManager.getSharedInstance().fetchRecents();
//        Trip trip = new Trip();
//        trip.setFromStop(bartStops.get(0));
//        trip.setToStop(bartStops.get(1));
//        trip.setDate(new Date());
//        TransitManager.getSharedInstance().saveRecent(trip);
//        recents = TransitManager.getSharedInstance().fetchRecents();
//        Log.d("Recents",recents.toString());
    }

    @OnClick(R.id.fabStartTrip)
    public void onStartTripClicked() {
        showSnackBar(clMainContent, "Start Trip!");
    }

    @OnClick(R.id.btnSchedule)
    public void onScheduleClicked() {
        showSnackBar(clMainContent, "Schedule clicked!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Avoid issue that recycler view gets automatic focus on start of the app.
        nsvContent.scrollTo(0, 0);
    }

    @Override
    protected void onDestroy() {
        if (mSubscription != null)
            mSubscription.clear();
        super.onDestroy();
    }


//    public void sampleLoadJsonData() {
//        mSubscription.add(
//                mTripHelperApiFactory.getApiForJson(TAConstants.TRANSIT_TYPE.CALTRAIN).getJsonStationInfo()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .unsubscribeOn(Schedulers.io())
//                .subscribe(response -> handleResult(response),
//                        throwable -> handleError(throwable))
//        );
//
//    }

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
    public void onItemClick(int position) {
        RecentsItem recent = recentsItemList.get(position);
        showSnackBar(clMainContent, recent.from + " to " + recent.to);
    }


}
