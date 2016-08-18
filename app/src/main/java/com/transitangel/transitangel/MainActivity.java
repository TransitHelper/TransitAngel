package com.transitangel.transitangel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.transitangel.transitangel.api.TripHelperApiFactory;
import com.transitangel.transitangel.api.TripHelplerRequestInterceptor;
import com.transitangel.transitangel.model.sampleJsonModel;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class MainActivity extends AppCompatActivity {

    private TripHelperApiFactory mTripHelperApiFactory;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTripHelperApiFactory = new TripHelperApiFactory(new TripHelplerRequestInterceptor(this));
        sampleLoadJsonData();
    }

    @Override
    protected void onDestroy() {
        if (mSubscription != null)
            mSubscription.clear();
        super.onDestroy();
    }


    public void sampleLoadJsonData() {
        mSubscription.add(
                mTripHelperApiFactory.getApiForJson(TAConstants.TRANSIT_TYPE.CALTRAIN).getJsonStationInfo()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(response -> handleResult(response),
                        throwable -> handleError(throwable))
        );

    }

    private void handleError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void handleResult(List<sampleJsonModel> response) {
        Log.e(MainActivity.class.getSimpleName(),response.toString());
    }
}
