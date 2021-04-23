package com.github.h4de5ing.okpermission;

import android.app.Activity;

import androidx.core.app.ActivityCompat;

import java.util.List;

class OKPermission {

    public static final int REQUEST_CODE_PERMISSION = 0;

    public static void okPermission(Activity activity, List<String> requestPermission) {
        if (requestPermission.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    requestPermission.toArray(new String[requestPermission.size()]),
                    REQUEST_CODE_PERMISSION);
        }
    }
}
