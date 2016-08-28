package com.transitangel.transitangel.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * author yogesh.shrivastava.
 */
public class HomePagerAdapter extends SmartFragmentStatePagerAdapter {
    int mNumOfTabs;

    public HomePagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                NearByFragment nearByFragment = NearByFragment.newInstance();
                return nearByFragment;
            case 1:
                RecentFragment tab2 = RecentFragment.newInstance();
                return tab2;
            case 2:
                LiveTripFragment tab3 = LiveTripFragment.newInstance();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}