package com.transitangel.transitangel.home;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.PrefManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.details.AlarmBroadcastReceiver;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.model.Transit.Trip;
import com.transitangel.transitangel.notifications.NotificationProvider;
import com.transitangel.transitangel.search.StationsAdapter;
import com.transitangel.transitangel.utils.DateUtil;
import com.transitangel.transitangel.utils.TAConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LiveTripFragment extends Fragment implements StationsAdapter.OnItemClickListener {

    public static final String TAG = LiveTripFragment.class.getSimpleName();
    private CompositeSubscription mSubscription = new CompositeSubscription();
    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;

    @BindView(R.id.tvNoLiveTrip)
    TextView tvNoLiveTrip;

    @BindView(R.id.btnCancelTrip)
    Button btnCancelTrip;

    private ArrayList<TrainStop> mStops;
    private StationsAdapter adapter;
    private TAConstants.TRANSIT_TYPE type;
    private ArrayList<TrainStop> mAlarmStops = new ArrayList<>();
    private ArrayList<TrainStop> mPrevSelectedStops = new ArrayList();

    HashMap<String, Stop> stopHashMap = new HashMap<>();

    public LiveTripFragment() {}

    public static LiveTripFragment newInstance() {
        LiveTripFragment fragment = new LiveTripFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_trip, container, false);
        ButterKnife.bind(this, view);
        displayOnGoingTrip();
        return view;
    }

    public void onSelected() {
        displayOnGoingTrip();
    }

    public void displayOnGoingTrip() {
        Trip trip = PrefManager.getOnGoingTrip();
        if (trip != null) {
            btnCancelTrip.setVisibility(View.VISIBLE);
            rvStationList.setVisibility(View.VISIBLE);
            tvNoLiveTrip.setVisibility(View.GONE);
            type = trip.getType();
            mStops = trip.getSelectedTrain().getTrainStopsBetween(trip.getFromStop().getId(), trip.getToStop().getId());
            mSubscription.add(getCurrentStops(mStops).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(response -> mPrevSelectedStops = response,
                            throwable -> throwable.printStackTrace()));
            if (type == TAConstants.TRANSIT_TYPE.CALTRAIN) {
                stopHashMap = CaltrainTransitManager.getSharedInstance().getStopLookup();
            } else {
                stopHashMap = BartTransitManager.getSharedInstance().getStopLookup();
            }

            // Create the recents adapter.
            adapter = new StationsAdapter(getActivity(), mStops, StationsAdapter.ITEM_ONGOING);
            rvStationList.setAdapter(adapter);
            rvStationList.setLayoutManager(new LinearLayoutManager(getActivity()));
            adapter.setOnItemClickListener(this);
        } else {
            // Display empty screen
            rvStationList.setVisibility(View.GONE);
            btnCancelTrip.setVisibility(View.GONE);
            tvNoLiveTrip.setVisibility(View.VISIBLE);
        }
    }

    public rx.Observable<ArrayList<TrainStop>> getCurrentStops(ArrayList<TrainStop> mStops) {
        ArrayList<TrainStop> result = new ArrayList<>();
        for (TrainStop stop :
                mStops) {
            if (stop.getNotify()) result.add(stop);
        }
        return rx.Observable.just(result);
    }

    @Override
    public void onCheckBoxSelected(View view, int position) {
        ArrayList<TrainStop> visibleStopsList = adapter.getVisibleStops();
        String contentDescription = getString(R.string.content_description_train_arriving) + visibleStopsList.get(position).getName()
                + getString(R.string.content_description_station)
                + visibleStopsList.get(position).getDepartureTime()
                + getString(R.string.notification_selected);
        view.setContentDescription(contentDescription);
        TrainStop stop = mStops.get(position);
        stop.setNotify(true);
        mAlarmStops.add(stop);
        int requestCode = TAConstants.ALARM_REQUEST_CODE + stop.getStopOrder();
        AddAlaram(stop, requestCode);
    }

    @Override
    public void onCheckBoxUnSelected(View view, int position) {
        ArrayList<TrainStop> visibleStopsList = adapter.getVisibleStops();
        String contentDescription = getString(R.string.content_description_train_arriving) + visibleStopsList.get(position).getName()
                + getString(R.string.content_description_station)
                + visibleStopsList.get(position).getDepartureTime()
                + getString(R.string.tap_to_add_notifications);
        view.setContentDescription(contentDescription);
        TrainStop stop = mStops.get(position);
        stop.setNotify(false);
        mAlarmStops.remove(stop);
        int requestCode = TAConstants.ALARM_REQUEST_CODE + stop.getStopOrder();
        removeAlarm(stop, requestCode);
    }

    private void AddAlaram(TrainStop stop, int requestCode) {
        mSubscription.add(addAlarmToSelectedStops(stop, requestCode).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(response -> Log.e(TAG, "Alarm added"),
                        throwable -> throwable.printStackTrace()));
    }

    private void removeAlarm(TrainStop stop, int requestCode) {
        mSubscription.add(removeAlarmToSelectedStops(stop, requestCode).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(response -> Log.e(TAG, "Alarm removed"),
                        throwable -> throwable.printStackTrace()));
    }

    @OnClick(R.id.btnCancelTrip)
    public void onCancelTrip() {
        PrefManager.removeOnGoingTrip();
        // Remove the persistent notification.
        NotificationProvider.getInstance().dismissOnGoingNotification(getActivity());
        // Refresh the ongoing trip tab.
        displayOnGoingTrip();
        Toast.makeText(getActivity(), "Trip Cancelled.", Toast.LENGTH_SHORT).show();
    }

    private Observable<TrainStop> addAlarmToSelectedStops(TrainStop lastStop, int requestCode) {
        Intent intent = new Intent(getActivity(), AlarmBroadcastReceiver.class);
        Gson gson = new Gson();
        String json = gson.toJson(lastStop);
        intent.putExtra(AlarmBroadcastReceiver.ARG_STOP, json);
        intent.putExtra(AlarmBroadcastReceiver.TRIP_ID, PrefManager.getOnGoingTrip().getTripId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity().getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Timestamp timestamp = DateUtil.getTimeStamp(lastStop.getArrrivalTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        calendar.add(Calendar.MINUTE, -2);
        Log.e(String.valueOf(calendar.getTime()), String.valueOf(System.currentTimeMillis()));
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(getContext().ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);
        return Observable.just(lastStop);
    }

    private Observable<TrainStop> removeAlarmToSelectedStops(TrainStop lastStop, int requestCode) {
        Intent intent = new Intent(getActivity(), AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity().getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Timestamp timestamp = DateUtil.getTimeStamp(lastStop.getArrrivalTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        calendar.add(Calendar.MINUTE, -2);
        Log.e(String.valueOf(calendar.getTime()), String.valueOf(System.currentTimeMillis()));
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(getContext().ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        return Observable.just(lastStop);
    }

    @Override
    public void onDestroy() {
        mSubscription.clear();
        super.onDestroy();
    }
}
