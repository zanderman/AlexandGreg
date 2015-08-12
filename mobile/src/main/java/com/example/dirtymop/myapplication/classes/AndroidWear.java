package com.example.dirtymop.myapplication.classes;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

/**
 * Created by lndsharkfury on 8/9/15.
 */
public class AndroidWear
        implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /*
    * Member Variables
    * */
    // Context of instantiating application
    private Context context;
    // Google API Client
    private GoogleApiClient googleApiClient;



    public AndroidWear(final Context context) {
        this.context = context;


        // Iitialize Google API client.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this.context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API) // Request access only to the wearable API.
                    .build();
        }
        if (!googleApiClient.isConnected()) googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/accellist") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    Log.d("aw", "new data, sending message to handler");
                    Message m = new Message();
                    Bundle b = new Bundle();
                    b.putFloat("x-axis", dataMap.getFloat("x-axis"));
                    b.putFloat("y-axis", dataMap.getFloat("y-axis"));
                    b.putFloat("z-axis", dataMap.getFloat("z-axis"));
                    m.setData(b);
                    wearHandler.sendMessage(m);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    Handler wearHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.d("aw", "handling message: "
                + msg.getData().getFloat("x-axis")
                    + msg.getData().getFloat("y-axis")
                    + msg.getData().getFloat("z-axis")
            );
//            processAccelerometer("wear", new float[]{
//                    msg.getData().getFloat("x-axis"),
//                    msg.getData().getFloat("y-axis"),
//                    msg.getData().getFloat("z-axis")
//            });
        }
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /*
    * Send Data Method
    *
    * */
    public void sendLatLng(LatLng data) {

        Toast.makeText(context, "sending coordinates to wear!", Toast.LENGTH_SHORT).show();
        new DataTask().execute(data);
    }

    /*
    * Send Data Async Task
    *
    * */
    public class DataTask extends AsyncTask<LatLng, Void, Void> {

        @Override
        protected Void doInBackground(LatLng... params) {

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/latlnglist");

            for (int i = 0; i < params.length; i++) {
                Log.d("aw", "lat:  " + params[i].latitude + " | long: " + params[i].longitude);
                putDataMapReq.getDataMap().putDouble("latitude", params[i].latitude);
                putDataMapReq.getDataMap().putDouble("longitude", params[i].longitude);
            }

            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

            return null;
        }
    }
}
