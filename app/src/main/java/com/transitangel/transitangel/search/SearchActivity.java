package com.transitangel.transitangel.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.transitangel.transitangel.Manager.BartTransitManager;
import com.transitangel.transitangel.Manager.CaltrainTransitManager;
import com.transitangel.transitangel.R;
import com.transitangel.transitangel.model.Transit.Stop;
import com.transitangel.transitangel.model.Transit.Train;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    private static final String TAG = SearchActivity.class.getSimpleName();

    public static final String EXTRA_SELECTED_STATION = TAG + ".EXTRA_SELECTED_STATION";
    public static final String EXTRA_MODE = TAG + ".EXTRA_MODE";
    private static final String EXTRA_TRAIN_INFO = TAG + ".EXTRA_TRAIN_INFO";
    public static final String EXTRA_SERVICE = TAG + ".EXTRA_SERVICE";
    public static final String EXTRA_SERVICE_BART = TAG + ".EXTRA_SERVICE_BART";
    public static final String EXTRA_SERVICE_CALTRAIN = TAG + ".EXTRA_SERVICE_CALTRAIN";

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
    private SearchAdapter adapter;
    private String serviceType;

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
        serviceType = getIntent().getStringExtra(EXTRA_SERVICE);

        if (EXTRA_SERVICE_CALTRAIN.equalsIgnoreCase(serviceType)) {
            mStops = CaltrainTransitManager.getSharedInstance().getStops();
        } else {
            mStops = BartTransitManager.getSharedInstance().getStops();
        }

        setSupportActionBar(toolbar);
        if (mode == MODE_TYPE_DETAILS) {
            train = getIntent().getParcelableExtra(EXTRA_TRAIN_INFO);
            if (train != null) {
                tvTitle.setText("Train Details :" + train.getName());
            }
        } else {
            getSupportActionBar().setTitle(getString(R.string.search_title));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the recents adapter.
        adapter = new SearchAdapter(this, mStops);
        rvStationList.setAdapter(adapter);
        rvStationList.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        // Use a custom search icon for the SearchView in AppBar
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.search);

        // Customize searchview text and hint colors
        int searchEditId = android.support.v7.appcompat.R.id.search_src_text;
        EditText et = (EditText) searchView.findViewById(searchEditId);
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(Color.WHITE);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_STATION, adapter.getItem(position));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.setFilter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.setFilter(newText);
        return false;
    }
}
