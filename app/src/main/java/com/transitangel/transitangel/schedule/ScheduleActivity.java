package com.transitangel.transitangel.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.search.SearchActivity;
import com.transitangel.transitangel.utils.TAConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScheduleActivity extends AppCompatActivity {

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tablayout)
    TabLayout mTabLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tvTitle)
    TextView mTitle;
    @BindView(R.id.from_station)
    TextView tvFromStation;
    @BindView(R.id.to_station)
    TextView tvToStation;


    private SampleFragmentPagerAdapter fragmentPageAdapter;
    public static final String FROM_STATION_ID = "from_station_id";
    public static final String TO_STATION_ID = "to_station_id";
    public static final String ARG_TRANSIT_TYPE = "transit_type";
    private TAConstants.TRANSIT_TYPE mTransitType;


    public interface OnStationSelected {
        void onFromStationSelected(Intent intent);

        void onToStationSelected(Intent intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        ButterKnife.bind(this);
        Bundle bundle = new Bundle();
        bundle.putString
                (ScheduleFragment.FROM_STATION_ID,
                        getIntent().getStringExtra(FROM_STATION_ID));
        bundle.putString(
                ScheduleFragment.TO_STATION_ID,
                getIntent().getStringExtra(TO_STATION_ID));
        mTransitType = (TAConstants.TRANSIT_TYPE) getIntent().getSerializableExtra(ARG_TRANSIT_TYPE);
        setUpTitle();
        fragmentPageAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(), bundle);
        mViewPager.setAdapter(fragmentPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        if (mTransitType == TAConstants.TRANSIT_TYPE.BART) {
            TabLayout.Tab tab = mTabLayout.getTabAt(1);
            tab.select();
        } else {
            TabLayout.Tab tab = mTabLayout.getTabAt(0);
            tab.select();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void setUpTitle() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitle.setText("Schedule: Caltrain");
        mTitle.setContentDescription("Schedule for Caltrain, tap to select other service");
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment currentFragment = fragmentPageAdapter.getItem(mViewPager.getCurrentItem());
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Fragment getCurrentFragment() {
        Fragment currentFragment = fragmentPageAdapter.getItem(mViewPager.getCurrentItem());
        return currentFragment;
    }

    private void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.item_popup_schedules, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_caltrain:
                        mTitle.setText("Schedule: Caltrain");
                        mTitle.setContentDescription("Schedule for Caltrain, tap to select other service");
                        return true;
                    case R.id.action_bart:
                        mTitle.setText("Schedule: Bart");
                        mTitle.setContentDescription("Schedule for Bart, tap to select other service");
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    public void setFromStation(String fromStation) {
        tvFromStation.setText(fromStation);
    }

    public void setToStation(String toStation) {
        tvToStation.setText(toStation);
    }

    @OnClick(R.id.from_station)
    public void onClickFromStation() {
        Fragment fragment = getCurrentFragment();
        if(fragment != null && fragment instanceof OnStationSelected) {
            Intent intent = new Intent(this, SearchActivity.class);
            if (mTransitType == TAConstants.TRANSIT_TYPE.BART) {
                intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_BART);
            } else {
                intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_CALTRAIN);
            }
            ((OnStationSelected)getCurrentFragment()).onFromStationSelected(intent);
            startActivityForResult(intent, ScheduleFragment.RESULT_SEARCH_FROM, null);
        }
    }

    @OnClick(R.id.to_station)
    public void onClickToStation() {
        Fragment fragment = getCurrentFragment();
        if(fragment != null && fragment instanceof OnStationSelected) {
            Intent intent = new Intent(this, SearchActivity.class);
            if (mTransitType == TAConstants.TRANSIT_TYPE.BART) {
                intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_BART);
            } else {
                intent.putExtra(SearchActivity.EXTRA_SERVICE, SearchActivity.EXTRA_SERVICE_CALTRAIN);
            }
            ((OnStationSelected)getCurrentFragment()).onToStationSelected(intent);
            startActivityForResult(intent, ScheduleFragment.RESULT_SEARCH_TO, null);
        }
    }
}
