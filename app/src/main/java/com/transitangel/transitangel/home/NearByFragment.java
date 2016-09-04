package com.transitangel.transitangel.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.Manager.TransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;
import com.transitangel.transitangel.model.Transit.TrainStop;
import com.transitangel.transitangel.schedule.ScheduleActivity;
import com.transitangel.transitangel.utils.TAConstants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NearByFragment extends Fragment {


    @BindView(R.id.card_view_bart)
    View cardViewBart;

    @BindView(R.id.card_view_caltrain)
    View cardViewCaltrain;

    @BindView(R.id.bart_container)
    LinearLayout bartContainer;

    @BindView(R.id.caltrain_container)
    LinearLayout caltrainContainer;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.errorText)
    TextView errorText;

    @BindView(R.id.no_caltrains)
    TextView tvNoCaltrain;

    @BindView(R.id.no_bart)
    TextView tvNoBart;

    private Stop currentCalStop;
    private Stop currentBartStop;

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
        progressBar.setVisibility(View.VISIBLE);
        loadCurrentStops();
    }

    public void  loadCurrentStops() {
        CaltrainTransitManager.getSharedInstance().getNearestStop(getContext(), new TransitManager.NearestStopResponseHandler() {
            @Override
            public void nearestStop(boolean isSuccess, Stop stop) {
                if (isSuccess) {
                    saveCaltrainTrain(stop);
                    BartTransitManager.getSharedInstance().getNearestStop(getContext(), new TransitManager.NearestStopResponseHandler() {
                        @Override
                        public void nearestStop(boolean isSuccess, Stop stop) {
                            if (isSuccess) {
                                saveBarCurrentStop(stop);
                                loadAllStations();
                            } else {
                                failedToLoad();
                            }
                        }
                    });
                } else {
                    failedToLoad();
                }
            }
        });
    }

    private void loadAllStations() {
        final ArrayList<Train> calTrainList = CaltrainTransitManager.getSharedInstance().fetchTrainsDepartingFromStation(currentCalStop.getId(), 3);
        final ArrayList<Train> bartTrainList = BartTransitManager.getSharedInstance().fetchTrainsDepartingFromStation(currentBartStop.getId(), 3);
        getActivity().runOnUiThread(() -> {
                int calTrainSize = calTrainList.size() > 3 ? 3 : calTrainList.size();
                if(calTrainSize != 0) {
                    tvNoCaltrain.setVisibility(View.GONE);
                    caltrainContainer.setVisibility(View.VISIBLE);
                    View caltrain;
                    int caltrainCount = 0;
                    for (; caltrainCount < calTrainSize; caltrainCount++) {
                        caltrain = caltrainContainer.findViewById(getCaltrainId(caltrainCount));
                        // Check near by station and add it in the following way:
                        ImageView icon = (ImageView) caltrain.findViewById(R.id.ivIcon);
                        TextView trainInfo = (TextView) caltrain.findViewById(R.id.tvTrainInfo);
                        TextView trainDeparture = (TextView) caltrain.findViewById(R.id.tvDeparture);
                        icon.setImageResource(R.drawable.train_red);
                        Train train = calTrainList.get(caltrainCount);
                        final TrainStop currentStop = getCurrentStop(currentCalStop.getId(), train);
                        final TrainStop lastStop = train.getTrainStops().get(train.getTrainStops().size() - 1);
                        trainInfo.setText(getString(R.string.nearby_train, currentStop.getName(), lastStop.getName()));
                        trainDeparture.setText(getString(R.string.nearby_scheduled_at, currentStop.getDepartureTime()));
                        caltrain.setVisibility(View.VISIBLE);
                        caltrain.setOnClickListener((View view) -> {
                            if (currentCalStop != null) {
                                //start the schedule by setting the from station
                                Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                                intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, TAConstants.TRANSIT_TYPE.CALTRAIN);
                                intent.putExtra(ScheduleActivity.FROM_STATION_ID, currentStop.getStopId());
                                intent.putExtra(ScheduleActivity.TO_STATION_ID, lastStop.getStopId());
                                startActivity(intent);
                            }
                        });
                    }

                    // If there are any visible from the previous, refresh
                    while(caltrainCount <= 3) {
                        caltrain = caltrainContainer.findViewById(getCaltrainId(caltrainCount));
                        caltrain.setVisibility(View.GONE);
                        caltrainCount++;
                    }
                } else {
                    tvNoCaltrain.setVisibility(View.VISIBLE);
                    caltrainContainer.setVisibility(View.GONE);
                }

                int bartTrainSize = bartTrainList.size() > 3 ? 3 : bartTrainList.size();

                if(bartTrainSize != 0) {
                    tvNoBart.setVisibility(View.GONE);
                    bartContainer.setVisibility(View.VISIBLE);
                    int bartCount = 0;
                    View bart;
                    for (bartCount = 0; bartCount < bartTrainSize; bartCount++) {
                        // Check near by station and add it in the following way:
                        bart = bartContainer.findViewById(getBartId(bartCount));
                        ImageView icon = (ImageView) bart.findViewById(R.id.ivIcon);
                        icon.setImageResource(R.drawable.train_blue);
                        TextView trainInfo = (TextView) bart.findViewById(R.id.tvTrainInfo);
                        TextView trainDeparture = (TextView) bart.findViewById(R.id.tvDeparture);
                        Train train = bartTrainList.get(bartCount);
                        TrainStop currentStop = getCurrentStop(currentBartStop.getId(), train);
                        TrainStop lastStop = train.getTrainStops().get(train.getTrainStops().size() - 1);
                        trainInfo.setText(getString(R.string.nearby_train, currentStop.getName(), lastStop.getName()));
                        trainDeparture.setText(getString(R.string.nearby_scheduled_at, currentStop.getDepartureTime()));
                        bart.setVisibility(View.VISIBLE);
                        bart.setOnClickListener((View view) -> {
                            if (currentBartStop != null) {
                                //start the schedule by setting the from station
                                Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                                intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, TAConstants.TRANSIT_TYPE.BART);
                                intent.putExtra(ScheduleActivity.FROM_STATION_ID, currentStop.getStopId());
                                intent.putExtra(ScheduleActivity.TO_STATION_ID, lastStop.getStopId());
                                startActivity(intent);
                            }
                        });
                    }

                    // If there are any visible from the previous, refresh
                    while(bartCount <= 3) {
                        bart = bartContainer.findViewById(getBartId(bartCount));
                        bart.setVisibility(View.GONE);
                        bartCount++;
                    }
                } else  {
                    tvNoBart.setVisibility(View.VISIBLE);
                    bartContainer.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);


                // Else leave it
                switchOrder();
            });
    }


    private void switchOrder() {
        RelativeLayout.LayoutParams bartParms = (RelativeLayout.LayoutParams)cardViewBart.getLayoutParams();
        bartParms.removeRule(RelativeLayout.BELOW);
        RelativeLayout.LayoutParams caltrainParams = (RelativeLayout.LayoutParams)cardViewCaltrain.getLayoutParams();
        caltrainParams.addRule(RelativeLayout.BELOW, R.id.card_view_bart);
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
        for(TrainStop stop: train.getTrainStops()) {
            if(stop.getStopId().equalsIgnoreCase(id)) {
                return stop;
            }
        }
        return null;
    }

    private void saveBarCurrentStop(Stop stop) {
        currentBartStop = stop;
    }

    private void saveCaltrainTrain(Stop stop) {
        currentCalStop = stop;
    }

    private void failedToLoad() {
        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
    }
}
