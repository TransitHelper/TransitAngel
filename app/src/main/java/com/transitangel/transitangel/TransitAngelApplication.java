package com.transitangel.transitangel;

import android.app.Application;
import android.content.ContextWrapper;

import com.crashlytics.android.Crashlytics;
import com.pixplicity.easyprefs.library.Prefs;
import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.GeofenceManager;
import com.transitangel.transitangel.Manager.TTSManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.Manager.TransitManager;
import io.fabric.sdk.android.Fabric;

/**
 * Created by vidhurvoora on 8/20/16.
 */
public class TransitAngelApplication extends Application {

     @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
         // Initialize the Prefs class
         new Prefs.Builder()
                 .setContext(this)
                 .setMode(ContextWrapper.MODE_PRIVATE)
                 .setPrefsName(getPackageName())
                 .setUseDefaultSharedPreference(true)
                 .build();

         TransitManager.getSharedInstance().setup(this);
         CaltrainTransitManager.getSharedInstance().setup(this);
         BartTransitManager.getSharedInstance().setup(this);
         TransitLocationManager.getSharedInstance().setup(this);
         GeofenceManager.getSharedInstance().setup(this);
         TTSManager.getSharedInstance().setupTTS(this);
    }
}
