package com.teamon.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Location {
    companion object {
        fun initialize(context: Context, activity: Activity):Boolean {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user
                    AlertDialog.Builder(context)
                        .setTitle("Location Access Permission Needed")
                        .setMessage("This app needs the Location access permission in order to set your location.")
                        .setPositiveButton("OK") { dialog, _ ->
                            // Request the permission again
                            ActivityCompat.requestPermissions(activity,
                                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                1)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            // Handle the user's refusal
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(activity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        1)
                }
            }
            return true
        }
    }
}