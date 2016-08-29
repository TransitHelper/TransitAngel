package com.transitangel.transitangel.schedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.transitangel.transitangel.utils.TAConstants;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[]{"CalTrain", "Bart"};
    private final Bundle fragmentBundle;


    public SampleFragmentPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
       fragmentBundle=bundle;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            ScheduleFragment scheduleFragment=ScheduleFragment.newInstance(TAConstants.TRANSIT_TYPE.CALTRAIN);
            scheduleFragment.setArguments(fragmentBundle);
            return scheduleFragment;
        }
        else {
            BartScheduleFragment scheduleFragment=BartScheduleFragment.newInstance(TAConstants.TRANSIT_TYPE.BART);
            scheduleFragment.setArguments(fragmentBundle);
            return scheduleFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
