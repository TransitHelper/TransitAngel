package com.transitangel.transitangel.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.utils.TAConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleActivity extends AppCompatActivity {

    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.tablayout)
    TabLayout mTabLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tvTitle)
    TextView mTitle;
    private SampleFragmentPagerAdapter fragmentPageAdapter;
    public static final String FROM_STATION_ID = "from_station_id";
    public static final String TO_STATION_ID = "to_station_id";
    public static final String ARG_TRANSIT_TYPE = "transit_type";
    private TAConstants.TRANSIT_TYPE mTransitType;

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

    private void setUpTitle() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        mTitle.setText("Schedule");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment currentFragment = fragmentPageAdapter.getItem(mViewPager.getCurrentItem());
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
