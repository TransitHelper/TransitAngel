package com.transitangel.transitangel.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * author yvastavaus.
 */
public class UiUtils {
    /**
     * Show snack bar at the bottom of the screen to notify our users.
     *
     * @param parent
     * @param displayText
     */
    private void showSnackBar(View parent, String displayText, int length) {
        Snackbar.make(parent, displayText, length).show();
    }

    private void showSnackBar(View parent, String displayText) {
        showSnackBar(parent, displayText, Snackbar.LENGTH_LONG);
    }


}
