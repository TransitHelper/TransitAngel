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
import butterknife.OnClick;

public class NearByFragment extends Fragment {

    @BindView(R.id.bart_container)
    LinearLayout bartContainer;

    @BindView(R.id.caltrain_container)
    LinearLayout caltrainContainer;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.errorText)
    TextView errorText;

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


    @OnClick(R.id.caltrain_container)
    public void onCaltrainContainerClicked() {
        if ( currentCalStop != null ) {
            //start the schedule by setting the from station
            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
            intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, TAConstants.TRANSIT_TYPE.CALTRAIN);
            intent.putExtra(ScheduleActivity.FROM_STATION_ID,currentCalStop.getId());

            //set the to station of the first train?
            final ArrayList<Train> calTrainList = CaltrainTransitManager.getSharedInstance().fetchTrainsDepartingFromStation(currentCalStop.getId(), 3);
            if ( calTrainList != null && calTrainList.size()>0){
                Train firstTrain = calTrainList.get(0);
                TrainStop lastStop = firstTrain.getTrainStops().get(firstTrain.getTrainStops().size()-1);
                if ( lastStop != null ) {
                    intent.putExtra(ScheduleActivity.TO_STATION_ID,lastStop.getStopId());
                }
            }
            startActivity(intent);
        }
      }

    @OnClick(R.id.bart_container)
    public void onBartContainerClicked() {
        if ( currentBartStop != null ) {
            //start the schedule by setting the from station
            Intent intent = new Intent(getActivity(), ScheduleActivity.class);
            intent.putExtra(ScheduleActivity.ARG_TRANSIT_TYPE, TAConstants.TRANSIT_TYPE.BART);
            intent.putExtra(ScheduleActivity.FROM_STATION_ID,currentBartStop.getId());

            //set the to statio of the first train
            final ArrayList<Train> bartTrainList = BartTransitManager.getSharedInstance().fetchTrainsDepartingFromStation(currentBartStop.getId(), 3);
            if ( bartTrainList != null && bartTrainList.size()>0){
                Train firstTrain = bartTrainList.get(0);
                TrainStop lastStop = firstTrain.getTrainStops().get(firstTrain.getTrainStops().size()-1);
                if ( lastStop != null ) {
                    intent.putExtra(ScheduleActivity.TO_STATION_ID,lastStop.getStopId());
                }
            }
            startActivity(intent);
        }
     }

    public void loadCurrentStops() {
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int calTrainSize = calTrainList.size() > 3 ? 3 : calTrainList.size();
                for (int i = 0; i < calTrainSize; i++) {
                    // Check near by station and add it in the following way:
                    RelativeLayout caltrain = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_nearby_trains, caltrainContainer, false);
                    ImageView icon = (ImageView) caltrain.findViewById(R.id.ivIcon);
                    TextView trainInfo = (TextView) caltrain.findViewById(R.id.tvTrainInfo);
                    TextView trainDeparture = (TextView) caltrain.findViewById(R.id.tvDeparture);
                    icon.setImageResource(R.drawable.train_red);
                    Train train = calTrainList.get(i);
                    TrainStop currentStop = getCurrentStop(currentCalStop.getId(), train);
                    int lastStop = train.getTrainStops().size() - 1;
                    trainInfo.setText(getString(R.string.nearby_train, currentStop.getName(), train.getTrainStops().get(lastStop).getName()));
                    trainDeparture.setText(getString(R.string.nearby_scheduled_at, currentStop.getDepartureTime()));
                    caltrainContainer.addView(caltrain);
                }

                int bartTrainSize = bartTrainList.size() > 3 ? 3 : bartTrainList.size();
                for (int i = 0; i < bartTrainSize; i++) {
                    // Check near by station and add it in the following way:
                    RelativeLayout bart = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_nearby_trains, bartContainer, false);
                    ImageView icon = (ImageView) bart.findViewById(R.id.ivIcon);
                    icon.setImageResource(R.drawable.train_blue);
                    TextView trainInfo = (TextView) bart.findViewById(R.id.tvTrainInfo);
                    TextView trainDeparture = (TextView) bart.findViewById(R.id.tvDeparture);
                    Train train = bartTrainList.get(i);
                    TrainStop currentStop = getCurrentStop(currentBartStop.getId(), train);
                    int lastStop = train.getTrainStops().size() - 1;
                    trainInfo.setText(getString(R.string.nearby_train, currentStop.getName(), train.getTrainStops().get(lastStop).getName()));
                    trainDeparture.setText(getString(R.string.nearby_scheduled_at, currentStop.getDepartureTime()));
                    bartContainer.addView(bart);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
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
