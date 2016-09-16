package com.transitangel.transitangel;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.pixplicity.easyprefs.library.Prefs;
import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.GeofenceManager;
import com.transitangel.transitangel.Manager.TTSManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.util.Arrays;

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

         SessionConfiguration config = new SessionConfiguration.Builder()
                 .setClientId("UEyCTAgOkCzdObRsU39xrnpSnQFGCXgD") //This is necessary
                 .setServerToken("B7JV01T4VB6Tqaat-k4bHwY1TxiVjmsVW6P_EoKU")
                 //.setRedirectUri("YOUR_REDIRECT_URI") //This is necessary if you'll be using implicit grant
                 .setEnvironment(SessionConfiguration.Environment.SANDBOX) //Useful for testing your app in the sandbox environment
                 .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
                 .build();

//This is a convenience method and will set the default config to be used in other components without passing it directly.
         UberSdk.initialize(config);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
