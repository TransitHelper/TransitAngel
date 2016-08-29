package com.transitangel.transitangel.home;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.transitangel.transitangel.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NearByFragment extends Fragment {

    @BindView(R.id.bart_container)
    LinearLayout bartContainer;

    @BindView(R.id.caltrain_container)
    LinearLayout caltrainContainer;

    public NearByFragment() {}

    public static NearByFragment newInstance() {
        NearByFragment fragment = new NearByFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.bind(this, view);
        init(inflater);
        return view;
    }

    private void init(LayoutInflater inflater) {
        for(int i =0; i < 3 ; i++) {
            // Check near by station and add it in the following way:
            RelativeLayout caltrain = (RelativeLayout) inflater.inflate(R.layout.item_nearby_trains, caltrainContainer, false);
            ImageView icon = (ImageView) caltrain.findViewById(R.id.ivIcon);
            TextView trainInfo = (TextView) caltrain.findViewById(R.id.tvTrainInfo);
            TextView trainDeparture = (TextView) caltrain.findViewById(R.id.tvDeparture);
            icon.setImageResource(R.drawable.train_red);
            trainInfo.setText(getString(R.string.nearby_train, "Mountain View", "San Fransisco"));
            trainDeparture.setText(getString(R.string.nearby_scheduled_at, "5 PM"));
            caltrainContainer.addView(caltrain);
        }

        for(int i =0; i < 3 ; i++) {
            // Check near by station and add it in the following way:
            RelativeLayout bart = (RelativeLayout) inflater.inflate(R.layout.item_nearby_trains, bartContainer, false);
            ImageView icon = (ImageView) bart.findViewById(R.id.ivIcon);
            icon.setImageResource(R.drawable.train_blue);
            TextView trainInfo = (TextView) bart.findViewById(R.id.tvTrainInfo);
            TextView trainDeparture = (TextView) bart.findViewById(R.id.tvDeparture);
            trainInfo.setText(getString(R.string.nearby_train, "San Fransisco", "Fremont"));
            trainDeparture.setText(getString(R.string.nearby_scheduled_at, "5 PM"));
            bartContainer.addView(bart);
        }
    }


    @OnClick(R.id.caltrain_container)
    public void onCaltrainContainerClicked() {
        Toast.makeText(getActivity(), "Take me to scheduled screen to show all nearby caltrains leaving", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.bart_container)
    public void onBartContainerClicked() {
        Toast.makeText(getActivity(), "Take me to scheduled screen to show all nearby barts leaving", Toast.LENGTH_LONG).show();
    }
}
