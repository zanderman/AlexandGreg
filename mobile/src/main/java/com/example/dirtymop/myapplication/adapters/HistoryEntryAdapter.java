package com.example.dirtymop.myapplication.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dirtymop.myapplication.R;
import com.example.dirtymop.myapplication.classes.HistoryTable;
import com.example.dirtymop.myapplication.fragments.StoredMapSelection;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class HistoryEntryAdapter extends ArrayAdapter<HistoryTable> implements View.OnClickListener {

    private final Context context;
    private final ArrayList<HistoryTable> entries;
    private final StoredMapSelection fragment;
    // Constructor
    public HistoryEntryAdapter(StoredMapSelection fragment, Context context) {
        super(context, R.layout.stored_route_entry_layout);

        this.context = context;
        this.entries = null;
        this.fragment = fragment;
    }
    public HistoryEntryAdapter(StoredMapSelection fragment,Context context, ArrayList<HistoryTable> entries) {
        super(context, R.layout.history_entry_layout, entries);

        // Initialize object attributes
        this.context = context;
        this.entries = entries;
        this.fragment = fragment;
        Log.d("listviewtest", "inside constructer");
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Initialize layout inflater object.

//        Toast.makeText(getContext(), "getView", Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate the entry layout.
        final View row = inflater.inflate(R.layout.history_entry_layout, parent, false);

        /* ACCESS ALL VIEW ELEMENTS HERE AND AFTERWARD */

        TextView distance = (TextView) row.findViewById(R.id.distanceHistoryView);
        TextView speed = (TextView) row.findViewById(R.id.speedHistoryView);
        TextView elevation =(TextView) row.findViewById(R.id.elevationHistoryView);
        TextView topspeed =(TextView) row.findViewById(R.id.topspeedHistoryView);
        TextView date= (TextView) row.findViewById(R.id.dateHistoryView);
        TextView time= (TextView) row.findViewById(R.id.timeHistoryView);
        ImageView mapSnapshot = (ImageView) row.findViewById(R.id.mapSnapshot);


        topspeed.setText(entries.get(position).getTop_speed());
        date.setText(entries.get(position).getDate());
        time.setText(entries.get(position).getTime());
        distance.setText(entries.get(position).getDistance());
        speed.setText(entries.get(position).getAvgspeed());
        elevation.setText(entries.get(position).getElevation());
        mapSnapshot.setImageBitmap(StringToBitMap(entries.get(position).getIdentify()));

        row.setTag(new String[]{((Integer) position).toString()});
        row.setOnClickListener(this);

        Log.d("listviewtest", entries.get(position).getFacebookID() + "inside adapter");

        return row;
    }

    @Override
    public void onClick(View view) {
        // Load the entry expansion fragment

        // this is the index of the entry to be saved
        int indexInAdapter = Integer.parseInt(((String[]) view.getTag())[0]);
        Log.d("adapter", "item clicked: " + indexInAdapter);

        // get the item and pass to the activity
        this.fragment.loadExpansion(entries.get(indexInAdapter).getFacebookID()); // NEEDS UNIQUE_ID
    }
    @Override
    public void add(HistoryTable object) {
        super.add(object);
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}



// --------------------------------------
//public class StoredRouteAdapter extends ArrayAdapter<HistoryTable> implements View.OnClickListener {
//
//    private final Context context;
//    private final ArrayList<HistoryTable> entries;
//    private final StoredMapSelection fragment;
//
//    // Constructor with no initial data
//    public StoredRouteAdapter(StoredMapSelection fragment, Context context) {
//        super(context, R.layout.stored_route_entry_layout);
//
//        this.context = context;
//        this.entries = null;
//        this.fragment = fragment;
//    }
//    // Constructor with initial data
//    public StoredRouteAdapter(StoredMapSelection fragment, Context context, ArrayList<HistoryTable> entries) {
//        super(context, R.layout.stored_route_entry_layout, entries);
//
//        this.context = context;
//        this.entries = entries;
//        this.fragment = fragment;
//    }
//
//    @Override
//    public void onClick(View view) {
//        // Load the entry expansion fragment
//
//        // this is the index of the entry to be saved
//        int indexInAdapter = Integer.parseInt(((String[]) view.getTag())[0]);
//        Log.d("adapter", "item clicked: " + indexInAdapter);
//
//        // get the item and pass to the activity
//        this.fragment.loadExpansion(entries.get(indexInAdapter).getFacebookID()); // NEEDS UNIQUE_ID
//    }
//
//    @Override
//    public void add(HistoryTable object) {
//        super.add(object);
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        // Inflate the entry layout.
//        final View row = inflater.inflate(R.layout.stored_route_entry_layout, parent, false);
//
//        // Inite each item
//        TextView textView = (TextView) row.findViewById(R.id.aTextView);
//        textView.setText(entries.get(position).getFacebookID());
//
//        row.setTag(new String[]{((Integer) position).toString()});
//        row.setOnClickListener(this);
//
//        return row;//super.getView(position, convertView, parent);
//    }
//}
