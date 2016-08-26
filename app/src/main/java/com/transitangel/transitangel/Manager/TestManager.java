package com.transitangel.transitangel.Manager;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.model.Transit.Service;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.TrainStopFence;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vidhurvoora on 8/25/16.
 */
public class TestManager {

    private static TestManager sInstance;
    private Context mApplicationContext;

    public void setup(Context context) {
        mApplicationContext = context;
    }

    public static synchronized TestManager getSharedInstance() {
        if (sInstance == null) {
            sInstance = new TestManager();
        }
        return sInstance;
    }

    public void executeSampleAPICalls(Context context) {

        Stop caltrainStop = CaltrainTransitManager.getSharedInstance().getNearestStop(37.401438, -121.9252457);
        Stop bartStop = BartTransitManager.getSharedInstance().getNearestStop(37.401438, -121.9252457);

        //get all the services limited,local and babybullet
        ArrayList<Service> services = CaltrainTransitManager.getSharedInstance().getServices();

        //get all the stops
        ArrayList<Stop> stops = CaltrainTransitManager.getSharedInstance().getStops();
        //get hashmap for faster lookup of stop if you have stop id
        HashMap<String, Stop> stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
        Log.d("Services", services.toString());
        Log.d("Stops", stops.toString());

        ArrayList<Stop>caltrainStops = CaltrainTransitManager.getSharedInstance().getLocalStops();

//       ArrayList<Trip> recents =  TransitManager.getSharedInstance().fetchRecentSearchList();
//        if ( recents.size() > 0 ) {
//            TransitManager.getSharedInstance().createShortCut(recents.get(0));
//        }

        //fetch trains from SF to Santa Clara
        //Note: currently ignores the leaving after parameter and also ignore weekday/weekend

        //fetch trains arriving at a certain destination within a certain duration
//        ArrayList<Train> arrivingTrains = CaltrainTransitManager.getSharedInstance().fetchTrainsArrivingAtDestination("70011", 3);
//        Log.d("Trains arriving station", arrivingTrains.toString());
//
//        //bart stops
//        ArrayList<Stop> bartStops = BartTransitManager.getSharedInstance().getStops();
//        Log.d("Bart Stops", bartStops.toString());
//        //bart services
//        ArrayList<Service> bartServices = BartTransitManager.getSharedInstance().getServices();
//        Log.d("Bart Services", bartServices.toString());
//        // fetch trains from Fremont to Daly City
//        //last boolean to include all trains irrespective of that day time or not
//        ArrayList<Train> bartTrains = BartTransitManager.getSharedInstance().fetchTrains("12018519", "12018513", -1, new Date(), true);
//        Log.d("Fremont to DalyCity", bartTrains.toString());
//        ArrayList<Train> arrivingBartTrains = BartTransitManager.getSharedInstance().fetchTrainsArrivingAtDestination("12018519", 4);
//        Log.d("Bart arriving fremont", arrivingBartTrains.toString());

            //fetch departing trains
//        ArrayList<Train> cTrains = CaltrainTransitManager.getSharedInstance().fetchTrainsDepartingFromStation("70011",3);
//        ArrayList<Train> bTrains = BartTransitManager.getSharedInstance().fetchTrainsDepartingFromStation("12018519",3);
//        Log.d("Departing From Station",cTrains.toString());

        //fetch news alerts
//        TransitManager.getSharedInstance().fetchLatestTrafficNewsAlerts(new TrafficNewsAlertResponseHandler() {
//            @Override
//            public void onNewsAlertsReceived(boolean isSuccess, ArrayList<TrafficNewsAlert> trafficNewsAlerts) {
//                if (isSuccess) {
//                    Log.d("Traffic News Alerts", trafficNewsAlerts.toString());
//                }
//            }
//        });
//
//        //fetch tweets
//        TransitManager.getSharedInstance().fetchTweetAlerts(new TweetAlertResponseHandler() {
//            @Override
//            public void onTweetsReceived(boolean isSuccess, ArrayList<Tweet> tweetAlerts) {
//                if (isSuccess) {
//                    Log.d("Tweet alerts", tweetAlerts.toString());
//                }
//            }
//        });

//        TransitLocationManager.getSharedInstance().getCurrentLocation(context, new TransitLocationManager.LocationResponseHandler() {
//            @Override
//            public void OnLocationReceived(boolean isSuccess, LatLng latLng) {
//                if ( isSuccess ) {
//                    Log.d("Latitude Longitue",latLng.toString());
//                    testHandleOnLocationReceived(isSuccess,latLng);
//                }
//            }
//        });

//        TransitLocationManager.getSharedInstance().getLocationUpdates(this);

//            boolean isLocationOptionEnabled = TransitLocationManager.getSharedInstance().isLocationModeEnabled();
//            boolean isLocationAccessible = TransitLocationManager.getSharedInstance().isLocationAccessible();



        //sample recents
//        ArrayList<Trip> recents = TransitManager.getSharedInstance().fetchRecentSearchList();
//        Trip trip = new Trip();
//        trip.setFromStop(bartStops.get(3));
//        trip.setToStop(bartStops.get(5));
//        trip.setDate(new Date());
//        TransitManager.getSharedInstance().saveRecentSearch(trip);
//        recents = TransitManager.getSharedInstance().fetchRecentSearchList();
//        Log.d("Recents",recents.toString());
//       //sample trips
//        ArrayList<Trip> trips = TransitManager.getSharedInstance().fetchRecentTripList();
//        Trip trip2 = new Trip();
//        trip2.setFromStop(stops.get(0));
//        trip2.setToStop(stops.get(5));
//        trip2.setDate(new Date());
//        TransitManager.getSharedInstance().saveRecentTrip(trip2);
//        trips = TransitManager.getSharedInstance().fetchRecentTripList();
//        Log.d("Trips",trips.toString());

    }

    private void testHandleOnLocationReceived(boolean isSuccess, LatLng latLng) {
        TrainStop trainStop = new TrainStop();
        trainStop.setLatitude(Double.toString(latLng.latitude));
        trainStop.setLongitude(Double.toString(latLng.longitude));
        trainStop.setName("Test Geofence");
        TrainStopFence fence = new TrainStopFence(trainStop);
        GeofenceManager.getSharedInstance().addGeofence(mApplicationContext, fence, new GeofenceManager.GeofenceManagerListener() {
            @Override
            public void onGeofencesUpdated() {
                Log.d("Fence Updated", "Here");
            }

            @Override
            public void onError() {
                Log.d("Error", "Error adding fence");
            }
        });
    }
}
