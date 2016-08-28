package com.transitangel.transitangel.Intent;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.home.HomeActivity;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.utils.Shaker;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;

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
        //get the location only if there is ongoing trip
        Trip onGoingTrip = PrefManager.getOnGoingTrip();
        if (TransitLocationManager.getSharedInstance().isLocationModeEnabled() &&
                TransitLocationManager.getSharedInstance().isLocationAccessible() &&
                onGoingTrip != null) {
            TransitLocationManager.getSharedInstance().getCurrentLocation(this, new TransitLocationManager.LocationResponseHandler() {
                @Override
                public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
                    if ( isSuccess ) {
                        double latitude = latLng.latitude;
                        double longitude = latLng.longitude;
                        Train onGoingTrain = onGoingTrip.getSelectedTrain();
                        if ( onGoingTrain != null) {
                            ArrayList<TrainStop> trainStops = onGoingTrain.getTrainStops();
                            ArrayList<TrainStop> nearestStops = TransitManager.getSharedInstance().getNearestStops(latLng.latitude,latLng.longitude,trainStops);
                            if ( nearestStops != null && nearestStops.size() == 2 ) {

                                String stop1 = nearestStops.get(0).getName();
                                String stop2 = nearestStops.get(1).getName();

                                String contextText = "You are between "+stop1 + " & "+ stop2;

                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                NotificationProvider.getInstance().updateTripStartedNotification(getApplicationContext(),contextText);
                            }
                            else {
                                //just tell the nearest station
                                Stop stop = null;
                                if ( onGoingTrip.getType() == TAConstants.TRANSIT_TYPE.BART) {
                                    stop = BartTransitManager.getSharedInstance().getNearestStop(latitude,longitude);
                                }
                                else {
                                    stop = CaltrainTransitManager.getSharedInstance().getNearestStop(latitude,longitude);
                                }

                                String contextText = "You are near "+stop.getName();

                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                NotificationProvider.getInstance().updateTripStartedNotification(getApplicationContext(),contextText);

                            }
                        }

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