package com.transitangel.transitangel.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public final class PermissionUtils {

    private PermissionUtils() {}

    public static boolean isPermissionGranted(final String permission, final Context context) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(String permission, int requestCode, Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static void requestPermissions(String[] permission, int requestCode, Activity activity) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }
}
