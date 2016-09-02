package com.transitangel.transitangel.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.utils.TAConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tvTitle)
    TextView mTitle;
    @BindView(R.id.fragment_container)
    FrameLayout mFrameLayout;


    public static final String FROM_STATION_ID = "from_station_id";
    public static final String TO_STATION_ID = "to_station_id";
    public static final String ARG_TRANSIT_TYPE = "transit_type";
    public static String BART_FROM_STATION;
    public static String BART_TO_STATION;
    public static String CAL_FROM_STATION;
    public static String CAL_TO_STATION;
    private static TAConstants.TRANSIT_TYPE mTransitType;
    private static String FRAG_TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        ButterKnife.bind(this);
        mTransitType = (TAConstants.TRANSIT_TYPE) getIntent().getSerializableExtra(ARG_TRANSIT_TYPE);
        setUpTitle();
        if (mTransitType == null || mTransitType == TAConstants.TRANSIT_TYPE.CALTRAIN) {
            CAL_FROM_STATION = getIntent().getStringExtra(FROM_STATION_ID);
            CAL_TO_STATION = getIntent().getStringExtra(TO_STATION_ID);
            mTitle.setText("Schedule: Caltrain");
            mTitle.setContentDescription("Schedule for Caltrain, tap to select other service");
            loadCalTrainFragment(true);
        } else {
            BART_FROM_STATION = getIntent().getStringExtra(FROM_STATION_ID);
            BART_TO_STATION = getIntent().getStringExtra(TO_STATION_ID);
            mTitle.setText("Schedule: Bart");
            mTitle.setContentDescription("Schedule for Bart, tap to select other service");
            loadBartFragment(true);
        }
    }

    private void loadCalTrainFragment(boolean isFromRecent) {
        ScheduleFragment newFragment = ScheduleFragment.newInstance(mTransitType);
        if (isFromRecent && mTransitType == TAConstants.TRANSIT_TYPE.CALTRAIN) {
            Bundle bundle = new Bundle();
            bundle.putString(ScheduleFragment.FROM_STATION_ID, CAL_FROM_STATION);
            bundle.putString(ScheduleFragment.TO_STATION_ID, CAL_TO_STATION);
            newFragment.setArguments(bundle);
        }
        FRAG_TAG = TAConstants.TRANSIT_TYPE.CALTRAIN.name();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newFragment, FRAG_TAG).commit();
    }

    private void loadBartFragment(boolean isFromRecent) {
        BartScheduleFragment newFragment = BartScheduleFragment.newInstance(mTransitType);
        if (isFromRecent && mTransitType== TAConstants.TRANSIT_TYPE.BART) {
            Bundle bundle = new Bundle();
            bundle.putString(BartScheduleFragment.FROM_STATION_ID, BART_FROM_STATION);
            bundle.putString(BartScheduleFragment.TO_STATION_ID, BART_TO_STATION);
            newFragment.setArguments(bundle);
        }
        FRAG_TAG = TAConstants.TRANSIT_TYPE.BART.name();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newFragment, FRAG_TAG).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressedListener currentFragment = (onBackPressedListener) getSupportFragmentManager().findFragmentByTag(FRAG_TAG);
                if (currentFragment != null)
                    currentFragment.onBackPressed();
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
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment currentFragment = (Fragment) getSupportFragmentManager().findFragmentByTag(FRAG_TAG);
        if (currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                        mTransitType = TAConstants.TRANSIT_TYPE.CALTRAIN;
                        loadCalTrainFragment(true);
                        return true;
                    case R.id.action_bart:
                        mTitle.setText("Schedule: Bart");
                        mTitle.setContentDescription("Schedule for Bart, tap to select other service");
                        mTransitType = TAConstants.TRANSIT_TYPE.BART;
                        loadBartFragment(true);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    interface onBackPressedListener {
        void onBackPressed();
    }
}
