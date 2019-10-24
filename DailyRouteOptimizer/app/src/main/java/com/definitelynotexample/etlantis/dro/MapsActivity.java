package com.definitelynotexample.etlantis.dro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    private FusedLocationProviderClient fusedLocationClient;

    boolean consented;

    private void addLocation(){
        if (consented) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    data = data + String.valueOf(location.getLatitude()) + ',' + String.valueOf(location.getLongitude()) + ',';
                }
            });
        }
    }

    String data = "";
    String result = "";
    String[] coordinates;
    int iterator = 0;
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    String key = "";

    float zoomLevel = 16.0f; //This goes up to 21

    private final static int INTERVAL = 1000 * 60 * 5; //5 minutes
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            addLocation();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("vall", Context.MODE_PRIVATE);
        consented = sharedPref.getBoolean("consented", false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

        final ProgressBar prgsbr = findViewById(R.id.progressbar);

        Button start = findViewById(R.id.buttonStart);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(MapsActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted
                    startRepeatingTask();
                    prgsbr.setProgress(100, true);
                }
            }
        });

        final Button show = findViewById(R.id.buttonShow);
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iterator < 4) {

                    stopRepeatingTask();
                    prgsbr.setProgress(0, true);

                    RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);

                    if (data.length() == 0) {
                        data = "49.911424,16.609458,49.911424,16.609458,49.911932,16.610262,49.912609,16.610171,49.912609,16.610171,49.9035300,16.5608750,49.9035300,16.5608750,49.9079475,16.5533142,49.8978461,16.5700817,49.8978461,16.5700817,49.911424,16.609458,49.911424,16.609458,49.911932,16.610262,49.912609,16.610171,49.912609,16.610171,";
                    }
                    data = data.substring(0, data.length() - 1);

                    final String url = "https://etlantis.pythonanywhere.com/backend/is_shortest/" + data;
                    // prepare the Request
                    StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    result = response;

                                    if (result.equals("shortest")) {
                                        Toast.makeText(MapsActivity.this, "You know your ways", Toast.LENGTH_SHORT).show();
                                    } else {


                                        coordinates = result.split(",");
                                        iterator = coordinates.length-1;


                                        mMap.clear();

                                        place1 = new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]))).title("Location 1");
                                        place2 = new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[2]), Double.parseDouble(coordinates[3]))).title("Location 2");

                                        new com.definitelynotexample.etlantis.dro.FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "walking"), "walking");

                                        mMap.addMarker(place1);
                                        mMap.addMarker(place2);

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), zoomLevel));

                                        if (iterator > 4){
                                            show.setText("Next");
                                        }
                                    }
                                }

                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Response", "FUCK");
                                }
                            }
                    );

                    // add it to the RequestQueue
                    queue.add(getRequest);

                    data = "";
                }
                else {
                    mMap.clear();

                    place1 = new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[iterator-3]), Double.parseDouble(coordinates[iterator-2]))).title("Location 1");
                    place2 = new MarkerOptions().position(new LatLng(Double.parseDouble(coordinates[iterator-1]), Double.parseDouble(coordinates[iterator]))).title("Location 2");

                    new com.definitelynotexample.etlantis.dro.FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "walking"), "walking");

                    mMap.addMarker(place1);
                    mMap.addMarker(place2);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), zoomLevel));

                    iterator = iterator - 4;

                    if (iterator < 4){
                        show.setText("Stop and show me\nwhatcha got");
                    }
                }
            }

        });

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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
