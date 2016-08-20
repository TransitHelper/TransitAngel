package com.transitangel.transitangel;

import android.app.Application;

import com.transitangel.transitangel.Manager.TransitManager;

/**
 * Created by vidhurvoora on 8/20/16.
 */
public class TransitAngelApplication extends Application {

     @Override
    public void onCreate() {
        super.onCreate();
         TransitManager.getSharedInstance().setup(this);
    }
}
