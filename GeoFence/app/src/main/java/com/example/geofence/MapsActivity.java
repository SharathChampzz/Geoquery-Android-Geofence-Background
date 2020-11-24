package com.example.geofence;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener {

    private GoogleMap mMap;

    static MapsActivity instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    Marker currentUser;
    DatabaseReference myLocationRef;
    GeoFire geoFire;
    List<LatLng> dangerousArea = new ArrayList<>();

    String lat = "", lon = "";

    public static MapsActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        instance = this;

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);
                        updateLocation();

                        initArea();
                        settingGeoFire();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getApplicationContext(), "You must Grant Permission to make it work!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    private void initArea() {
        dangerousArea.add(new LatLng(13.7964, 75.7283));
        dangerousArea.add(new LatLng(13.831127, 75.695608));
        dangerousArea.add(new LatLng(13.886428, 75.640983));
        }


    private void settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);
    }

    private void updateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }


    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(1f);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        for (LatLng latlng : dangerousArea){
            mMap.addCircle(new CircleOptions().center(latlng)
                    .radius(500)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f)
            );
//            Toast.makeText(getApplicationContext(),"" + latlng.latitude , Toast.LENGTH_SHORT).show();
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latlng.latitude, latlng.longitude), 0.5f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);
        }


        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void updateLocation(String latitude, String longitude) {
        MapsActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                try {
//                    Toast.makeText(getApplicationContext(),"Size: " + dangerousArea.size() , Toast.LENGTH_SHORT).show();
//                    for(LatLng latlng : dangerousArea){
////                        Toast.makeText(getApplicationContext(),"L:" + latlng.latitude , Toast.LENGTH_SHORT).show();
//                    }
//                }
//                catch(Exception ex){
//                    Toast.makeText(getApplicationContext(),"Databse Empty" + String.valueOf(ex) , Toast.LENGTH_SHORT).show();
//                }
                String s = latitude + " , " + longitude;
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                if(mMap != null){

                    geoFire.setLocation("Keyy", new GeoLocation(Double.parseDouble(latitude), Double.parseDouble(longitude)),
                            new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if(currentUser != null) currentUser.remove();
                                    currentUser = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                            .title("You"));

                                    mMap.animateCamera(CameraUpdateFactory
                                            .newLatLngZoom(currentUser.getPosition(), 12.0f));
                                }
                            }
                    );
                }
                else{
                    Toast.makeText(getApplicationContext(), "mMap is NULL", Toast.LENGTH_SHORT).show();
                }
//                textView.setText(s);
//                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
//                LocationHelper object = new LocationHelper(latitude, longitude);
//                mDatabase.push().setValue(object);
            }
        });
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Toast.makeText(getApplicationContext(), key + "Entering.." , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyExited(String key) {
        Toast.makeText(getApplicationContext(), key + "Exited" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Toast.makeText(getApplicationContext(), key + "Moving inside" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(getApplicationContext(), error.getMessage() , Toast.LENGTH_SHORT).show();
    }
}