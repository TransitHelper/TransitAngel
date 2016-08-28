package com.transitangel.transitangel.Intent;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.home.HomeActivity;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.utils.Shaker;

public class ShakerService extends android.app.Service implements Shaker.OnShakeListener {


    private Shaker mShaker;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {

        super.onCreate();
        this.mSensorManager = ((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        mShaker = new Shaker(this);
        mShaker.setOnShakeListener(this);
    }

    @Override
    public void onShake() {
       Log.d("On Shake","Shake");
        if (TransitLocationManager.getSharedInstance().isLocationModeEnabled() &&
                TransitLocationManager.getSharedInstance().isLocationAccessible() ) {
            TransitLocationManager.getSharedInstance().getCurrentLocation(this, new TransitLocationManager.LocationResponseHandler() {
                @Override
                public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                    if ( isSuccess ) {
                        //TODO determine if caltrain or bart
                        //Check if there is on going trip?
                        Stop stop = CaltrainTransitManager.getSharedInstance().getNearestStop(latLng.latitude,latLng.longitude);

                        //TODO determine exact content which goes here
                        String contextText = "Your nearest stop is "+stop.getName();

                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        NotificationProvider.getInstance().showBigTextNotification(getApplicationContext(), intent, "Nearest Stop", contextText);
                    }
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);

        return START_STICKY;

    }
}