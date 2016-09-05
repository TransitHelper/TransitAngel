package com.transitangel.transitangel.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.utils.TAConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterDialogFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private static FilterChangedListener mFilterChanged;
    @BindView(R.id.layout_date)
    ViewGroup mLayoutDate;
    @BindView(R.id.layout_time)
    ViewGroup mLayoutTime;
    TextView mSelectedDate;
    TextView mDateLabel;
    TextView mTimeLabel;
    TextView mSelectedTime;
    private static Calendar calendar = Calendar.getInstance();
    private static Context mContext;
    private static TAConstants.TRANSIT_TYPE mTransitType;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat(TAConstants.DATE_FORMAT);
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat(TAConstants.TIME_FORMAT);

    public static FilterDialogFragment newInstance(Context context, FilterChangedListener filterChangedListener, TAConstants.TRANSIT_TYPE type) {
        FilterDialogFragment newDialogFragment = new FilterDialogFragment();
        mContext = context;
        mFilterChanged = filterChangedListener;
        Bundle args = new Bundle();
        newDialogFragment.setArguments(args);
        mTransitType = type;
        return newDialogFragment;
    }

    public interface FilterChangedListener {
        void onFilterChanged(Calendar cal, TAConstants.TRANSIT_TYPE type);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View container = inflater.inflate(R.layout.custom_dialog, null);
        ButterKnife.bind(this, container);
        String date = mDateFormat.format(calendar.getTime());
        SimpleDateFormat descriptionFormat = new SimpleDateFormat(TAConstants.ACC_DATE_FORMAT);
        mSelectedDate = (TextView) mLayoutDate.findViewById(R.id.value);
        mDateLabel = (TextView) mLayoutDate.findViewById(R.id.label);
        mDateLabel.setText("Date ");
        mSelectedDate.setText(date);
        mLayoutDate.setContentDescription("Selected Date" +
                descriptionFormat.format(calendar.getTime()) + ". Tap to change");
        mSelectedTime = (TextView) mLayoutTime.findViewById(R.id.value);
        mTimeLabel = (TextView) mLayoutTime.findViewById(R.id.label);
        mTimeLabel.setText("Time ");
        String time = mTimeFormat.format(calendar.getTime());
        mSelectedTime.setText(time);
        mLayoutTime.setContentDescription("Selected Time " + time + ". Tap to change");
        builder.setView(container);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mFilterChanged.onFilterChanged(calendar, mTransitType);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        setListeners();
        setValues();
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).
                        setTextColor(getResources().getColor(R.color.colorPrimary));
                alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
        return alertDialog;
    }

    private void setValues() {
    }


    private void setListeners() {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
    }

    @OnClick(R.id.layout_date)
    public void showDatePicker() {
        Calendar mDateCal = getMidnightInUTC(Calendar.getInstance());
        int year = mDateCal.get(Calendar.YEAR);
        int month = mDateCal.get(Calendar.MONTH);
        int day = mDateCal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.show();
    }

    @OnClick(R.id.layout_time)
    public void showTimePicker() {
        Calendar c = java.util.Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show();
    }


    @NonNull
    public static Calendar getMidnightInUTC(@NonNull Calendar calendar) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        mSelectedTime.setText(hourOfDay + ":" + minute);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        mLayoutTime.setContentDescription("Selected time " + mTimeFormat.format(calendar.getTime()) + "Tap to change");
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = new SimpleDateFormat(TAConstants.DATE_FORMAT).format(calendar.getTime());
        mSelectedDate.setText(date);
        mLayoutDate.setContentDescription("Selected date " +
                new SimpleDateFormat(TAConstants.ACC_DATE_FORMAT).format(calendar.getTime())
                + " Tap to change");
    }
}
