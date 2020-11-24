package com.example.geofence;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;

public class MyLocationService extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATE = "com.example.geofence.UPDATE_LOCATION";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            final String action = intent.getAction();

            if(ACTION_PROCESS_UPDATE.equals(action)){
                LocationResult result = LocationResult.extractResult(intent);

                if(result!=null){
                    Location location = result.getLastLocation();
                    String locstring = "" + location.getLatitude() + " , " + location.getLongitude();
                    try{
                        MapsActivity.getInstance().updateLocation(String.valueOf(location.getLatitude()),
                                String.valueOf(location.getLongitude()));
                    }
                    catch (Exception e){
                        Toast.makeText(context, locstring, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}