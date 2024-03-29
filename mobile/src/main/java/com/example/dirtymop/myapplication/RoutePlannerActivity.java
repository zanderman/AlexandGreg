package com.example.dirtymop.myapplication;

import java.util.HashMap;
import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dirtymop.myapplication.adapters.SectionsPagerAdapter;
import com.example.dirtymop.myapplication.classes.ContactsTable;
import com.example.dirtymop.myapplication.classes.DatabaseHelper;
import com.example.dirtymop.myapplication.classes.HistoryTable;
import com.example.dirtymop.myapplication.fragments.EntryExpansionFragment;
import com.example.dirtymop.myapplication.fragments.NewMapSelection;
import com.example.dirtymop.myapplication.fragments.RetainedFragment;
import com.example.dirtymop.myapplication.fragments.StoredMapSelection;
import com.google.android.gms.maps.model.LatLng;

public class RoutePlannerActivity
        extends AppCompatActivity
        implements ActionBar.TabListener,
        NewMapSelection.NewMapSelectionLisstener,
        EntryExpansionFragment.OnFragmentInteractionListener
        {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */



    SectionsPagerAdapter mSectionsPagerAdapter;

    String[] tabs;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /*
    * Tags and Flags
    * */
    private static final String TAG_FRAG_RETAINED = "RetainedFragment";

    /*
    * Fragments
    * */
    RetainedFragment retainedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_planner);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_name);

        // Initialize names of all the tabs.
        tabs = new String[] {"Your Routes", "Create New"};

        // Load the retained fragment onCreate.
        if (getFragmentManager().findFragmentByTag(TAG_FRAG_RETAINED) == null) {
            retainedFragment = new RetainedFragment();
            retainedFragment.setRetainInstance(true);
            android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(retainedFragment,TAG_FRAG_RETAINED);
            fragmentTransaction.commit();
        }
        if (savedInstanceState != null) {
            //Every time during the recreate of the activity, the retainedFragment will be lost, so we need to reassign the retainedFragment
            retainedFragment = (RetainedFragment) getFragmentManager().findFragmentByTag(TAG_FRAG_RETAINED);
            retainedFragment.setRetainInstance(true);
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                tabs);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Log.d("route", "init ViewPager");

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d("route", "position selected: " + Integer.toString(position));
                actionBar.setSelectedNavigationItem(position);
            }
        });

//        this.deleteDatabase("local.db");


        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_planner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(RoutePlannerActivity.this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /*
    * NewMapSelection Interface Methods
    * */
    // Start the HUD.
    public void startHud() {
        Intent intent = new Intent(RoutePlannerActivity.this, HudActivity.class);
        startActivity(intent);
    }

    // Push markers to the retained fragment for storage.
    @Override
    public void storeMarkers(HashMap<LatLng, String> markers) {
        retainedFragment.updateMarkers(markers);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
