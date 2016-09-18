package com.transitangel.transitangel.home;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TransitLocationManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.schedule.ScheduleActivity;
import com.transitangel.transitangel.utils.DateUtil;
import com.transitangel.transitangel.utils.TAConstants;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.error.ApiError;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class NearByFragment extends Fragment implements HomeActivity.onBackPressedListener {


    private static final String TAG = NearByFragment.class.getSimpleName();

    @BindView(R.id.card_view_bart)
    View cardViewBart;

    @BindView(R.id.srlNearbyContainer)
    SwipeRefreshLayout srlNearbyContainer;

    @BindView(R.id.card_view_caltrain)
    View cardViewCaltrain;

    @BindView(R.id.card_view_uber)
    View cardViewUber;

    @BindView(R.id.bart_container)
    LinearLayout bartContainer;

    @BindView(R.id.caltrain_container)
    LinearLayout caltrainContainer;

    @BindView(R.id.emptyView)
    ViewGroup mEmptyView;

    @BindView(R.id.no_caltrains)
    TextView tvNoCaltrain;

    @BindView(R.id.no_bart)
    TextView tvNoBart;

    @BindView(R.id.uber_caltrain_name)
    TextView uberCaltrainName;

    @BindView(R.id.uber_bart_name)
    TextView uberBartName;

    @BindView(R.id.caltrainUberBtn)
    RideRequestButton caltrainUberButton;

    @BindView(R.id.bartUberBtn)
    RideRequestButton bartUberButton;

    @BindView(R.id.uber_bart_container)
    View uberBartContainer;

    @BindView(R.id.uber_caltrain_container)
    View uberCaltainConatiner;

    @BindView(R.id.uber_suggestions_container)
    View uberSuggestionsContainer;

    @BindView(R.id.nearby_caltrain_header)
    TextView caltrainHeader;

    @BindView(R.id.nearby_header_bart)
    TextView bartHeader;

    @BindView(R.id.uber_title)
    TextView uberSuggestionsTitle;

    private Stop currentCalStop;
    private Stop currentBartStop;
    private TextView mEmptyTextView;
    private SessionConfiguration uberSessionConfig;
    private float mXUberTouch;
    private float mYUberTouch;

    public NearByFragment() {
    }

    public static NearByFragment newInstance() {
        NearByFragment fragment = new NearByFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {

        uberSessionConfig = new SessionConfiguration.Builder()
                .setClientId("UEyCTAgOkCzdObRsU39xrnpSnQFGCXgD")
                .setServerToken("B7JV01T4VB6Tqaat-k4bHwY1TxiVjmsVW6P_EoKU")
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .build();

        srlNearbyContainer.post(() -> {
            srlNearbyContainer.setRefreshing(true);
        });

        loadCurrentStops();
        srlNearbyContainer.setOnRefreshListener(() -> loadCurrentStops());
        srlNearbyContainer.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
    }

    public void loadCurrentStops() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                CaltrainTransitManager.getSharedInstance().getNearestStop(getContext(), (isSuccess, stop) -> {
                    loadBart(isSuccess, stop);
                });
                return null;
            }
        }.execute();
    }

    private void loadBart(boolean isSuccess, Stop stop) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (isSuccess) {
                    saveCaltrainTrain(stop);
                    BartTransitManager.getSharedInstance().getNearestStop(getContext(), (isSuccess, stop) -> {
                        loadAllStations(isSuccess, stop);
                    });
                } else {
                    failedToLoad();
                }
                return null;
            }
        }.execute();
    }

    private void loadAllStations(boolean isSuccess, Stop stop) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (isSuccess) {
                    saveBarCurrentStop(stop);
                    loadAllStations();
                } else {
                    failedToLoad();
                }
                return null;
            }
        }.execute();
    }

    private void loadAllStations() {
        final ArrayList<Train> calTrainList = CaltrainTransitManager.getSharedInstance()
                .fetchTrainsDepartingFromStation(currentCalStop.getId(), 3);
        final ArrayList<Train> bartTrainList = BartTransitManager.getSharedInstance()
                .fetchTrainsDepartingFromStation(currentBartStop.getId(), 3);
        getActivity().runOnUiThread(() -> {
            if (mEmptyView.isShown()) {
                mEmptyView.setVisibility(View.GONE);
            }

            int calTrainSize = calTrainList.size() > 3 ? 3 : calTrainList.size();
            if (calTrainSize != 0) {
                tvNoCaltrain.setVisibility(View.GONE);
                caltrainContainer.setVisibility(View.VISIBLE);
                View caltrain;
                int caltrainCount = 0;
                for (; caltrainCount < calTrainSize; caltrainCount++) {
                    caltrain = caltrainContainer.findViewById(getCaltrainId(caltrainCount));
                    // Check near by station and add it in the following way:
                    ImageView icon = (ImageView) caltrain.findViewById(R.id.ivIcon);
                    final TextView trainInfo = (TextView) caltrain.findViewById(R.id.tvTrainInfo);
                    TextView trainDeparture = (TextView) caltrain.findViewById(R.id.tvDeparture);
                    icon.setImageResource(R.mipmap.caltrian_icon);
                    Train train = calTrainList.get(caltrainCount);
                    final TrainStop currentStop = getCurrentStop(currentCalStop.getId(), train);
                    final TrainStop lastStop = train.getTrainStops().get(train.getTrainStops().size() - 1);
                    trainInfo.setText(getString(R.string.nearby_train, lastStop.getName()));
                    trainInfo.setContentDescription(getString(R.string.nearby_train_description, currentStop.getName(), lastStop.getName()));
                    trainDeparture.setText(getString(R.string.nearby_scheduled_at,
                            DateUtil.getFormattedTime(currentStop.getDepartureTime())));
                    caltrain.setVisibility(View.VISIBLE);
                    caltrain.setOnClickListener((View view) -> {
                        if (currentCalStop != null) {
                            //start the schedule by setting the from station
                            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                            intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, TAConstants.TRANSIT_TYPE.CALTRAIN);
                            intent.putExtra(ScheduleActivity.FROM_STATION_ID, currentStop.getStopId());
                            intent.putExtra(ScheduleActivity.TO_STATION_ID, lastStop.getStopId());
                            Pair<View, String> p1 = Pair.create(trainInfo, "fromStation");
                            Pair<View, String> p2 = Pair.create(trainInfo, "toStation");
                            ActivityOptionsCompat options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(getActivity(), p1, p2);
                            startActivity(intent, options.toBundle());
                        }
                    });
                    caltrain.setClickable(true);
                    if (caltrainCount == (calTrainSize - 2)) {
                        View divider = (View) caltrain.findViewById(R.id.card_divider);
                        divider.setVisibility(View.GONE);
                    }
                }

                // If there are any visible from the previous, refresh
                while (caltrainCount <= 3) {
                    caltrain = caltrainContainer.findViewById(getCaltrainId(caltrainCount));
                    caltrain.setVisibility(View.GONE);
                    caltrainCount++;
                }
            } else {
                tvNoCaltrain.setVisibility(View.VISIBLE);
                caltrainContainer.setVisibility(View.GONE);
            }

            int bartTrainSize = bartTrainList.size() > 3 ? 3 : bartTrainList.size();

            if (bartTrainSize != 0) {
                tvNoBart.setVisibility(View.GONE);
                bartContainer.setVisibility(View.VISIBLE);
                int bartCount = 0;
                View bart;
                for (bartCount = 0; bartCount < bartTrainSize; bartCount++) {
                    // Check near by station and add it in the following way:
                    bart = bartContainer.findViewById(getBartId(bartCount));
                    ImageView icon = (ImageView) bart.findViewById(R.id.ivIcon);
                    icon.setImageResource(R.drawable.bart_icon);
                    final TextView trainInfo = (TextView) bart.findViewById(R.id.tvTrainInfo);
                    TextView trainDeparture = (TextView) bart.findViewById(R.id.tvDeparture);
                    Train train = bartTrainList.get(bartCount);
                    TrainStop currentStop = getCurrentStop(currentBartStop.getId(), train);
                    TrainStop lastStop = train.getTrainStops().get(train.getTrainStops().size() - 1);
                    trainInfo.setText(getString(R.string.nearby_train, lastStop.getName()));
                    trainInfo.setContentDescription(getString(R.string.nearby_train_description, currentStop.getName(), lastStop.getName()));
                    trainDeparture.setText(getString(R.string.nearby_scheduled_at,
                            DateUtil.getFormattedTime(currentStop.getDepartureTime())));
                    bart.setVisibility(View.VISIBLE);
                    bart.setOnClickListener((View view) -> {
                        if (currentBartStop != null) {
                            //start the schedule by setting the from station
                            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                            intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, TAConstants.TRANSIT_TYPE.BART);
                            intent.putExtra(ScheduleActivity.FROM_STATION_ID, currentStop.getStopId());
                            intent.putExtra(ScheduleActivity.TO_STATION_ID, lastStop.getStopId());
                            Pair<View, String> p1 = Pair.create(trainInfo, "fromStation");
                            Pair<View, String> p2 = Pair.create(trainInfo, "toStation");
                            ActivityOptionsCompat options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(getActivity(), p1, p2);
                            startActivity(intent, options.toBundle());
                        }
                    });
                    if (bartCount == (bartTrainSize - 2)) {
                        View divider = (View) bart.findViewById(R.id.card_divider);
                        divider.setVisibility(View.GONE);
                    }
                    bart.setClickable(true);
                }

                // If there are any visible from the previous, refresh
                while (bartCount <= 3) {
                    bart = bartContainer.findViewById(getBartId(bartCount));
                    bart.setVisibility(View.GONE);
                    bartCount++;
                }
            } else {
                tvNoBart.setVisibility(View.VISIBLE);
                bartContainer.setVisibility(View.GONE);
            }

            srlNearbyContainer.setRefreshing(false);
        });

        boolean isBartNearest = TransitManager.getSharedInstance().isBartNearest(currentCalStop, currentBartStop);
        if (isBartNearest) {
            switchOrder();
        }
    }


    private void switchOrder() {
        RelativeLayout.LayoutParams bartParms = (RelativeLayout.LayoutParams) cardViewBart.getLayoutParams();
        bartParms.removeRule(RelativeLayout.BELOW);
        RelativeLayout.LayoutParams caltrainParams = (RelativeLayout.LayoutParams) cardViewCaltrain.getLayoutParams();
        caltrainParams.addRule(RelativeLayout.BELOW, R.id.card_view_bart);

        RelativeLayout.LayoutParams cardViewUberParms = (RelativeLayout.LayoutParams) cardViewUber.getLayoutParams();
        cardViewUberParms.removeRule(RelativeLayout.BELOW);
        cardViewUberParms.addRule(RelativeLayout.BELOW, R.id.card_view_caltrain);
    }

    private int getCaltrainId(int i) {
        switch (i) {
            case 0:
                return R.id.first_caltrain_item;
            case 1:
                return R.id.second_caltrain_item;
            default:
                return R.id.third_caltrain_item;
        }
    }

    private int getBartId(int i) {
        switch (i) {
            case 0:
                return R.id.first_bart_item;
            case 1:
                return R.id.second_bart_item;
            default:
                return R.id.third_bart_item;
        }
    }

    private TrainStop getCurrentStop(String id, Train train) {
        for (TrainStop stop : train.getTrainStops()) {
            if (stop.getStopId().equalsIgnoreCase(id)) {
                return stop;
            }
        }
        return null;
    }

    private void saveBarCurrentStop(Stop stop) {
        currentBartStop = stop;

        //set the uber button
        double stopLatitude = Double.parseDouble(stop.getLatitude());
        double stopLongitude = Double.parseDouble(stop.getLongitude());
        LatLng latLng = TransitLocationManager.getSharedInstance().getCachedLocation();
        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(latLng.latitude, latLng.longitude, "Current Location", "")
                .setDropoffLocation(stopLatitude, stopLongitude, stop.getName(), stop.getName() + " Bart Station")
                .build();

        ServerTokenSession session = new ServerTokenSession(uberSessionConfig);
        bartUberButton.setCallback(new RideRequestButtonCallback() {
            @Override
            public void onRideInformationLoaded() {
                uberBartName.post(() -> {
                    uberBartContainer.setVisibility(View.VISIBLE);
                    uberBartName.setText(stop.getName() + " Bart station :");
                });
            }

            @Override
            public void onError(ApiError apiError) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
        bartUberButton.setRideParameters(rideParams);
        bartUberButton.setSession(session);
        bartUberButton.loadRideInformation();
        bartHeader.post(() -> bartHeader.setText(getString(R.string.nearby_bart_title) + stop.getName()));

    }

    private void saveCaltrainTrain(Stop stop) {
        currentCalStop = stop;

        //set the uber button
        //set it to visible
        double stopLatitude = Double.parseDouble(stop.getLatitude());
        double stopLongitude = Double.parseDouble(stop.getLongitude());
        LatLng latLng = TransitLocationManager.getSharedInstance().getCachedLocation();
        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(latLng.latitude, latLng.longitude, "Current Location", "")
                .setDropoffLocation(stopLatitude, stopLongitude, stop.getName(), stop.getName() + " Caltrain Station")
                .build();

        ServerTokenSession session = new ServerTokenSession(uberSessionConfig);
        caltrainUberButton.setCallback(new RideRequestButtonCallback() {
            @Override
            public void onRideInformationLoaded() {
                uberCaltrainName.post(() -> {
                    uberCaltainConatiner.setVisibility(View.VISIBLE);
                    uberCaltrainName.setText(stop.getName() + " Caltrain station :");
                });
            }

            @Override
            public void onError(ApiError apiError) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
        caltrainUberButton.setRideParameters(rideParams);
        caltrainUberButton.setSession(session);
        caltrainUberButton.loadRideInformation();
        caltrainHeader.post(() -> {
            caltrainHeader.setText(getString(R.string.nearby_caltrain_title) + stop.getName());

        });

    }

    @OnClick(R.id.uber_button)
    public void onUberButtonClicked() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uberSuggestionsContainer.setVisibility(View.VISIBLE);
        } else {
            enterReveal();
        }
        uberSuggestionsContainer.postDelayed(() -> uberSuggestionsTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED), 500);
    }


    @OnTouch(R.id.uber_button)
    public boolean onUberButtonTouch(View view, MotionEvent motionEvent) {
        mXUberTouch = motionEvent.getRawX();
        mYUberTouch = motionEvent.getRawY();
        return false;
    }


    @OnClick(R.id.close_button)
    public void onUberCloseClicked() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uberSuggestionsContainer.setVisibility(View.GONE);
        } else {
            exitReveal();
        }
    }

    private void failedToLoad() {
        getActivity().runOnUiThread(() -> {
            srlNearbyContainer.setRefreshing(false);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyTextView = (TextView) mEmptyView.findViewById(R.id.empty_state_description);
            mEmptyTextView.setText(R.string.nearby_error_text);
            ImageView icon = (ImageView) mEmptyView.findViewById(R.id.image_empty_state);
            icon.setImageResource(R.drawable.train_blue_bart);
            caltrainContainer.setVisibility(View.GONE);
            bartContainer.setVisibility(View.GONE);
        });
    }

    public void noLocationPermissionGranted() {
        failedToLoad();
    }

    @Override
    public boolean onBackPressed() {
        if (uberSuggestionsContainer.isShown()) {
            onUberCloseClicked();
            return false;
        }
        return true;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void enterReveal() {
        // previously invisible view
        final View myView = uberSuggestionsContainer;

        // get the center for the clipping circle
        int cx = (int) mXUberTouch;
        int cy = (int) mYUberTouch;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void exitReveal() {
        // previously visible view
        final View myView = uberSuggestionsContainer;

        // get the center for the clipping circle
        int cx = (int) mXUberTouch;
        int cy = (int) mYUberTouch;

        // get the initial radius for the clipping circle
        int initialRadius = myView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.GONE);
            }
        });

        // start the animation
        anim.start();
    }
}
