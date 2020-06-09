package com.example.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.PolyUtil;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class BarcodeScanner extends FragmentActivity
        implements OnMapReadyCallback {
    private TextView t1, t2, t3, toast;
    GoogleMap mgoogleMap;
    boolean opened;
    FloatingActionButton floatingActionButton;
    private static final String TAG = "BarcodeScanner";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String Loc = Manifest.permission.GET_PACKAGE_SIZE;
    public static final String Camera = Manifest.permission.CAMERA;
    List<LatLng> latLngList = new ArrayList<>(new LinkedHashSet<LatLng>());
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    ImageView imageView;
    Fragment fragment;
    LinearLayout linearLayout;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean mLocationPermissionGranted = false;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode);
        t1 = findViewById(R.id.name);
        t2 = findViewById(R.id.url);
        t3 = findViewById(R.id.id);
        toast = findViewById(R.id.Toast);
        imageView = findViewById(R.id.downloadedImage);
        linearLayout = findViewById(R.id.view);
        linearLayout.setVisibility(View.INVISIBLE);
        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        getLocationPermission();
    }

    private void getLocationPermission() {

        String[] permissions = {FINE_LOCATION, COARSE_LOCATION, Camera};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Camera) == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionGranted = true;
                //  getLastLocation();
                //createLocationRequest();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        if (mLocationPermissionGranted) {
            if (isLocationEnabled()) {
                Task<Location> task = fusedLocationProviderClient.getLastLocation();
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                            Toast.makeText(getApplicationContext(), mLastLocation.getLatitude() + "" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
                            //assert supportMapFragment != null;
                            supportMapFragment.getMapAsync(BarcodeScanner.this);
                        }
                        else{
                            requestNewLocationData();
                        }
                    }
                });
            } else {
                LocationRequest locationRequest = LocationRequest.create();

                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(1500);
                locationRequest.setFastestInterval(1500);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);

                SettingsClient client = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                        Task<Location> task = fusedLocationProviderClient.getLastLocation();
//                        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//                            @Override
//                            public void onSuccess(Location location) {
//                                if (location != null) {
//                                    mLastLocation = location;
//                                    Toast.makeText(getApplicationContext(), mLastLocation.getLatitude() + "" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
//                                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
//                                    //assert supportMapFragment != null;
//                                    supportMapFragment.getMapAsync(BarcodeScanner.this);
//                                }
//                                else{
//                                    requestNewLocationData();
//                                }
//                            }
//                        });

                        requestNewLocationData();

                    }
                });

                task.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException) {
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(BarcodeScanner.this,
                                        40);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                        }
                    }
                });

                Task<Location> tk = fusedLocationProviderClient.getLastLocation();
                tk.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                            Toast.makeText(getApplicationContext(), mLastLocation.getLatitude() + "" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
                            //assert supportMapFragment != null;
                            supportMapFragment.getMapAsync(BarcodeScanner.this);
                        }
                    }
                });
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    getLocationPermission();
                    //initialize our map

                }
        }
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }
        @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location lastLocation = locationResult.getLastLocation();
            Log.d(TAG, "onLocationResult: " + mLastLocation);
            if (lastLocation != null) {

                mLastLocation = lastLocation;
                if (mgoogleMap != null) {

                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mCurrLocationMarker = mgoogleMap.addMarker(markerOptions);

                    //move map camera
                }
            }
        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap = googleMap;

        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        googleMap.addMarker(markerOptions);

    }
    public void ScanButton(View view)
    {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null){
            if (intentResult.getContents() == null)
            {

            }else
                {
                try {
                    JSONObject jsonObject = new JSONObject(intentResult.getContents());
                    JSONArray jsonArray = jsonObject.getJSONArray("geo_fencing");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray latlngArray = jsonArray.getJSONArray(i);
                        double lat = (double) latlngArray.get(0);
                        double lng = (double) latlngArray.get(1);
                        latLngList.add(new LatLng(lat,lng));
                    }
                        if (mLastLocation != null) {
                            boolean contain = PolyUtil.containsLocation(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), latLngList, false);
                            if (contain) {

                                try {
                                    String name = jsonObject.getString("name");
                                    t1.setText(name);
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                                try {
                                    String validTill = jsonObject.getString("valid_till");
                                    t2.setText(validTill);
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                                try
                                {
                                    String id=jsonObject.getString("id");
                                    t3.setText(id);
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                                try{
                                    String imageUri = jsonObject.getString("image_url");
                                    Picasso.get().load(imageUri).into(imageView);
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                                linearLayout.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),R.anim.anim));
                            } else {
                                linearLayout.startAnimation(AnimationUtils.loadAnimation(getBaseContext(),R.anim.anim));
                                toast.setText("Not in the range of location!");
                            }
                        }
                        Log.d(TAG, "onActivityResult: " + jsonObject);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}