package com.example.testandroidapp;

/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/** Helper to ask permission for hardware modules. */
public class PermissionsHelper implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int PERMISSION_CODE = 0;
    private static String MANIFEST_PERMISSION = "android.permission.";

    private static final int PERMISSION_ALL = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    //public PermissionsHelper(String whichPermission) {
    //    MANIFEST_PERMISSION += whichPermission;
    //}

    public static boolean hasPermissions(Context context, String... permissions) {
        if(context != null && permissions != null) {
            for(String permission:permissions) {
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Check to see we have the necessary permissions for this app. */
    public static boolean hasWhichPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, MANIFEST_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't. */
    public static void requestWhichPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity, new String[] {MANIFEST_PERMISSION}, PERMISSION_CODE);
    }
    public static void requestAllPermissions(Activity activity) {
        if(!hasPermissions(activity, PERMISSIONS)){
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    /** Check to see if we need to show the rationale for this permission. */
    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, MANIFEST_PERMISSION);
    }

    /** Launch Application Setting to grant permission. */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    // If this worked, it would close the app upon user denying either or both permissions.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("GRANT RESULTS: ", Integer.toString(grantResults[0]));
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    System.exit(0);
                    Log.d("SHOULD ", "HAVE EXITED");
                }
                break;
        }
    }
}