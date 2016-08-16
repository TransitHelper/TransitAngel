package com.jeevitharoyapathi.triphelper.data;

import android.content.SharedPreferences;

import com.jeevitharoyapathi.triphelper.api.TripHelperApi;
import com.jeevitharoyapathi.triphelper.api.TripHelperApiFactory;

public class DataManager {

    private TripHelperApiFactory mApiFactory;
    private SharedPreferences mSharedPreferences;

    public DataManager(TripHelperApiFactory apiFactory, SharedPreferences sharedPreferences) {
        mApiFactory = apiFactory;
        mSharedPreferences = sharedPreferences;
    }

    private TripHelperApi getExpenseApi() {
        return mApiFactory.getApi();
    }


}
