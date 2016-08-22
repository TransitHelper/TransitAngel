package com.transitangel.transitangel.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchActivity extends AppCompatActivity implements StationsAdapter.OnItemClickListener {

    private static final String TAG = SearchActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_STATION = TAG + ".EXTRA_SELECTED_STATION";
    public static final String EXTRA_MODE = TAG + ".EXTRA_MODE";
    private static final String EXTRA_TRAIN_INFO = TAG + ".EXTRA_TRAIN_INFO";

    public static final int MODE_TYPE_SEARCH = 1;
    public static final int MODE_TYPE_DETAILS = 2;


    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;
    @BindView(R.id.clMainContent)
    CoordinatorLayout clMainContent;

    private ArrayList<Stop> mStops;
    private Train train;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_TYPE_SEARCH);
        mStops = CaltrainTransitManager.getSharedInstance().getStops();
        if(mode == MODE_TYPE_DETAILS) {
            train = getIntent().getParcelableExtra(EXTRA_TRAIN_INFO);
            if(train != null) {
                tvTitle.setText("Train Details :" + train.getName());
            }
        } else {
            tvTitle.setText(getString(R.string.search_title));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        // Create the recents adapter.
        StationsAdapter adapter = new StationsAdapter(this, mStops);
        rvStationList.setAdapter(adapter);
        rvStationList.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(int position) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_STATION, mStops.get(position));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
