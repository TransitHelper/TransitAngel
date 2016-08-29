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
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.transitangel.transitangel.R;
import com.transitangel.transitangel.utils.TAConstants;

import java.text.DateFormat;
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
    @BindView(R.id.txtDate)
    TextView mSelectDate;
    @BindView(R.id.txtTime)
    TextView mSelectTime;
    private String mDateFormat = "MM/dd/yyyy";
    private String mTimeFormat = "HH:mm";
    private static Calendar calendar = Calendar.getInstance();
    private static Context mContext;
    private static TAConstants.TRANSIT_TYPE mTransitType;

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
        View container = inflater.inflate(R.layout.filter, null);
        ButterKnife.bind(this, container);
        DateFormat df = new SimpleDateFormat(mDateFormat);
        String date = df.format(calendar.getTime());
        mSelectDate.setText(date);
        DateFormat tf = new SimpleDateFormat(mTimeFormat);
        mSelectTime.setText(tf.format(calendar.getTime()));
        View view = inflater.inflate(R.layout.dialog_custom_title, null);
        builder.setCustomTitle(view);
        builder.setView(container);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mFilterChanged.onFilterChanged(calendar,mTransitType);
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
        return alertDialog;
    }

    private void setValues() {
    }


    private void setListeners() {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1);
    }

    @OnClick(R.id.txtDate)
    public void showDatePicker() {
        Calendar mDateCal = getMidnightInUTC(Calendar.getInstance());
        int year = mDateCal.get(Calendar.YEAR);
        int month = mDateCal.get(Calendar.MONTH);
        int day = mDateCal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.show();
    }

    @OnClick(R.id.txtTime)
    public void showTimePicker() {
        Calendar c = java.util.Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show();
    }

    public static String getDate(long date) {
        if (date == 0)
            return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
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
        mSelectTime.setText(hourOfDay + ":" + minute);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        DateFormat df = new SimpleDateFormat(mDateFormat);
        String date = df.format(calendar.getTime());
        mSelectDate.setText(date);
    }
}
