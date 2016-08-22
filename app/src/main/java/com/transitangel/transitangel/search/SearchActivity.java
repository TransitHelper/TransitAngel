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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchActivity extends AppCompatActivity implements StationsAdapter.OnItemClickListener {

    private static final String TAG = SearchActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_STATION = TAG + ".EXTRA_SELECTED_STATION";


    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvStationList)
    RecyclerView rvStationList;
    @BindView(R.id.clMainContent)
    CoordinatorLayout clMainContent;

    private ArrayList<Stop> mStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        tvTitle.setText(getString(R.string.search_title));
        mStops = CaltrainTransitManager.getSharedInstance().getStops();
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
