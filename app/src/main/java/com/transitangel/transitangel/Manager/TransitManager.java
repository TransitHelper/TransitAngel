package com.transitangel.transitangel.Manager;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.transitangel.transitangel.model.Transit.Line;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.utils.TAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class TransitManager {


    private static TransitManager sInstance;

    protected String apiBaseUrl = "http://api.511.org/transit";
    protected String apiKey = "a24b8b61-63e2-4571-a41b-11490cd9ada9";

    protected Context mApplicationContext;
    public static TAConstants.TRANSIT_TYPE mTransitType;

    public static AsyncHttpClient httpClient;

    public static synchronized TransitManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new TransitManager();

        }
        return sInstance;
    }

    public void setup(Context context) {
        mApplicationContext = context;
    }

    public RequestParams getBaseParams() {
        RequestParams baseParams = new RequestParams();
        baseParams.put("api_key",apiKey);
        baseParams.put("format","json");
        if ( mTransitType == TAConstants.TRANSIT_TYPE.BART) {
            baseParams.put("operator_id","BART");
        }
        else if ( ( mTransitType == TAConstants.TRANSIT_TYPE.CALTRAIN)) {
            baseParams.put("operator_id","Caltrain");
        }

        return baseParams;
    }

    protected ArrayList<Line> fetchLineArrFromJson(JSONArray lineArr) throws JSONException {
        ArrayList<Line> lines = new ArrayList<Line>();
        for (int i=0;i<lineArr.length();i++){
            JSONObject lineObj = lineArr.getJSONObject(i);
            Line line = new Line(lineObj);
            lines.add(line);
        }
        return lines;
    }

    protected ArrayList<Stop> fetchStopArrFromJson(JSONArray stopArr) throws JSONException {
        ArrayList<Stop> stops = new ArrayList<Stop>();
        for (int i=0;i<stopArr.length();i++){
            JSONObject stopObj = stopArr.getJSONObject(i);
            Stop stop = new Stop(stopObj);
            stops.add(stop);
        }
        return stops;
    }

    public void fetchLines(LineResponseHandler handler){
        String lineUrl = apiBaseUrl + "/lines";
        httpClient.get(lineUrl,getBaseParams(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("JSON response",response.toString());
                try {
                    JSONArray lineArr = response.getJSONArray(0);
                    ArrayList<Line> lines = fetchLineArrFromJson(lineArr);
                    handler.OnLinesResponseReceived(true,lines);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.OnLinesResponseReceived(false,null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.OnLinesResponseReceived(false,null);
            }
        });
    }

    public void fetchStops(StopResponseHandler handler){
        String stopUrl = apiBaseUrl + "/stops";
        httpClient.get(stopUrl,getBaseParams(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    JSONArray stopArr = response.getJSONObject("Contents").getJSONObject("dataObjects").getJSONArray("ScheduledStopPoint");
                    ArrayList<Stop> stops = fetchStopArrFromJson(stopArr);
                    handler.OnStopsResponseReceived(true,stops);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.OnStopsResponseReceived(false,null);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.OnStopsResponseReceived(false,null);
            }
        });
    }


    public String loadJSONFromAsset(String fileName) {
        String json = null;
        try {

            InputStream is = mApplicationContext.getAssets().open(fileName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
