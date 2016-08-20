package com.transitangel.transitangel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.sampleJsonModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.subscriptions.CompositeSubscription;


public class MainActivity extends AppCompatActivity {

    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));

        //get all the services limited,local and babybullet
        ArrayList<Service> services = CaltrainTransitManager.getSharedInstance().getServices(this);
        //get all the stops
        ArrayList<Stop> stops = CaltrainTransitManager.getSharedInstance().getStops(this);
        //get hashmap for faster lookup of stop if you have stop id
        HashMap<String,Stop> stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        Log.d("Services",services.toString());
        Log.d("Stops",stops.toString());

        //fetch trains from SF to Santa Clara
        //Note: currently ignores the leaving after parameter and also ignore weekday/weekend

        ArrayList<Train> trains = CaltrainTransitManager.getSharedInstance().fetchTrains(this,"70021","70242",null);
        Log.d("Trains from SF to MView",trains.toString());

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
        Log.e(MainActivity.class.getSimpleName(),response.toString());
    }
}
