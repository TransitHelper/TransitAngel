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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        ButterKnife.bind(this);
        setUpTitle();
        setUpViewPager();
    }

    private void setUpViewPager() {
        fragmentPageAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(fragmentPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
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
