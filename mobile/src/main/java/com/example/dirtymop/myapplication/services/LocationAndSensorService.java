package com.example.dirtymop.myapplication.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.dirtymop.myapplication.HudActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LocationAndSensorService
        extends Service
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /*
    * Service member variables
    * */
    IBinder binder = new LocalBinder();
    private HudActivity activity;

    /*
    * Location member variables
    * */
    private LocationRequest mLocationRequest;
    private GoogleApiClient locationGoogleApiClient, dataGoogleApiClient;
    private long LOCATION_INTERVAL = 500; // milliseconds
    private Bundle locationBundle;
    private volatile boolean isBuilt = false;
    private Location lastLocation = null;
    private float totalDistance = 0;

    /*
    * Sensor member variables
    * */
    private SensorManager sensorManager;
    private Sensor gyroscope;
    private Sensor accelerometer;
    private ArrayList<Float> gyroData;
    private ArrayList<Float> accelData;
    private int sensorDelay = 0;

//    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d("service", "received broadcast in service!");
//        }
//    };


    /*
    * Binder class
    *
    * An instance of binder will be used to bind with the this service
    * from elsewhere
    *
    * binder can start a service even if its dead.
    * */
    public class LocalBinder extends Binder {

        public LocationAndSensorService getServiceInstance() {
            return LocationAndSensorService.this;
        }
    }

    /*
    * Service methods
    * */
    public LocationAndSensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("service", "onCreate started...");

        Log.d("service", "onCreate finished...");
    }

    // NOTE: do all location and service stuff onBind.
    @Override
    public IBinder onBind(Intent intent) {

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("service_bind", false); // stores key/value pairs.
        editor.commit();

        // Connect to the GoogleApiClient
        buildGoogleApiClient();
        locationGoogleApiClient.connect();
//        dataGoogleApiClient.connect();

        // Build sensors.
        buildSensors();
        // Connect sensors to sensor listeners.
        startSensors();

        // Return the binder.
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("service_bind", false); // stores key/value pairs.
        editor.commit();

        return super.onUnbind(intent);
    }

    // NOTE: When service is first started (before binding), it runs this command.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // update SP
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("service_started", true);
        editor.commit();

//        // Build the GoogleApiClient
//        buildGoogleApiClient();
//
//        // Create sensors.
//        buildSensors();

        // Send message to activity that service has started
        newBroadcast();

        // START_STICKY: service will be explicitly started and stopped for arbitrary amounts of time.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Disconnect from the GoogleApiClient on service destruction.
        if (locationGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationGoogleApiClient, this);
            locationGoogleApiClient.disconnect();
        }
//        if (dataGoogleApiClient.isConnected()) {
//            dataGoogleApiClient.disconnect();
//        }

        // Unregister the sensors from their listeners.
        stopSensors();

        // unregister the broadcast receiver
//        unregisterReceiver(serviceReceiver);
    }

    // Assign the activity that service can send callbacks to.
    public void sendCallbacks(HudActivity activity) {
        this.activity = activity;
    }

    public void newBroadcast() {
        Log.d("service","sending broadcast...");
        Intent intent = new Intent();
        intent.setAction("toActivity");
        sendBroadcast(intent);
        Log.d("service", "broadcast sent!");
    }
    /*
    * --------------------
    * */


    /*
    * GoogleApiClient methods
    * */
    protected void buildGoogleApiClient() {
        locationGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .build();

        isBuilt = true;
    }
    /*
    * --------------------
    * */

    /*
    * LocationListener methods
    * */
    @Override
    public void onLocationChanged(Location location) {

        // Save location to bundle
        locationBundle = new Bundle();
        locationBundle.putDouble("latitude", location.getLatitude());
        locationBundle.putDouble("longitude", location.getLongitude());
//        locationBundle.putFloat("accuracy", location.getAccuracy());
        locationBundle.putDouble("altitude", location.getAltitude());
        locationBundle.putFloat("speed", location.getSpeed());

        // Update total distance.
        if (lastLocation != null)
            totalDistance = totalDistance + lastLocation.distanceTo(location);

        // Put the total distance into the bundle.
        locationBundle.putFloat("distance", totalDistance);
        lastLocation = location;

        // Print location to the log.
        Log.d("service", locationBundle.toString());

        this.activity.updateLocation(locationBundle);
    }
    /*
    * --------------------
    * */

    /*
    * Sensor methods
    *
    * */
    public void buildSensors() {
        // Initialize the sensor manager.
        sensorManager = (SensorManager) getSystemService(activity.SENSOR_SERVICE);

        // Initialize sensor
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gyroData = new ArrayList<Float>();
        accelData = new ArrayList<Float>();
    }

    // Registers event listeners for each sensor.
    public void startSensors() {
        // Register listeners for each sensor
        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    // Unregister the sensors with the sensor manager.
    public void stopSensors() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    // Gyroscope event listener.
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            // Determine which sensor triggered the event listener
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (sensorDelay == 1) {
                    sensorDelay = 0;
                    processAccelerometer("mobile", new float[] {event.values[0], event.values[1], event.values[2]});
                } else {
                    sensorDelay++;
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    // Process Accelerometer
    public void processAccelerometer(String type, float[] values) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        Log.d("accelerometer", "data came from: " + type);

        final double alpha = 0.8;
        double[] gravity = new double[3];
        double[] linear_acceleration = new double[3];

        // Determine what contribution of gravity is.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];

        // Calculate true linear acceleration without gravity
        linear_acceleration[0] = values[0] - gravity[0];
        linear_acceleration[1] = values[1] - gravity[1];
        linear_acceleration[2] = values[2] - gravity[2];


        // Push the data to the activity as a bundle.
        Bundle b = new Bundle();
        b.putDouble("x-axis", linear_acceleration[0]);
        b.putDouble("y-axis", linear_acceleration[1]);
        b.putDouble("z-axis", linear_acceleration[2]);

        this.activity.updateAccelerometer(type, b);
    }
    /*
    * ----------------------
    * */


    /*
    * ConnectionCallbacks methods
    * */
    @Override
    public void onConnected(Bundle bundle) {

        // Create new location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Set priority
                .setInterval(LOCATION_INTERVAL) // Set interval for location refresh, milliseconds
                .setFastestInterval(100); // Set fastest update interval to 1 second

        // Run the request for updates.
        LocationServices.FusedLocationApi.requestLocationUpdates(locationGoogleApiClient, mLocationRequest, this);

        // Add data API listener
//        Wearable.DataApi.addListener(dataGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    /*
    * --------------------
    * */


    /*
    * OnConnectionFailedListener methods
    * */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("service", "[fail] connection with GoogleApiClient failed: " + connectionResult.toString());
    }
    /*
    * --------------------
    * */


//    /*
//    * Data API Listener methods
//    *
//    * Runs in a background thread.
//    * */
//    @Override
//    public void onDataChanged(DataEventBuffer dataEventBuffer) {
////        for (DataEvent event : dataEventBuffer) {
////            if (event.getType() == DataEvent.TYPE_CHANGED) {
////                // DataItem changed
////                DataItem item = event.getDataItem();
////                if (item.getUri().getPath().compareTo("/accellist") == 0) {
////                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
////
////                    Log.d("aw", "new data, sending message to handler");
////                    Message m = new Message();
////                    Bundle b = new Bundle();
////                    b.putFloat("x-axis", dataMap.getFloat("x-axis"));
////                    b.putFloat("y-axis", dataMap.getFloat("y-axis"));
////                    b.putFloat("z-axis", dataMap.getFloat("z-axis"));
////                    m.setData(b);
////                    wearHandler.handleMessage(m);
////                }
////
////            } else if (event.getType() == DataEvent.TYPE_DELETED) {
////                // DataItem deleted
////            }
////        }
//    }
//
////    Handler wearHandler = new Handler() {
////        @Override
////        public void handleMessage(Message msg) {
////
////            Log.d("aw", "handling message");
////            processAccelerometer("wear", new float[]{
////                    msg.getData().getFloat("x-axis"),
////                    msg.getData().getFloat("y-axis"),
////                    msg.getData().getFloat("z-axis")
////            });
////        }
////    };


}
