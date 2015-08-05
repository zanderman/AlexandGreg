package com.example.dirtymop.myapplication.fragments;


import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.dirtymop.myapplication.R;
import com.example.dirtymop.myapplication.adapters.StoredRouteAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StoredMapSelection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StoredMapSelection extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // List view
    private ListView entries;

    // List adapter
    private StoredRouteAdapter adapter;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StoredMapSelection.
     */
    // TODO: Rename and change types and number of parameters
    public static StoredMapSelection newInstance(String param1, String param2) {
        StoredMapSelection fragment = new StoredMapSelection();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public StoredMapSelection() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stored_map_selection, container, false);

        // Initialize the ListView
        entries = (ListView) view.findViewById(R.id.storageList);

        // Initialize the adapter
        adapter = new StoredRouteAdapter(getActivity().getApplicationContext());

        // Set the adapter for the ListView
        entries.setAdapter(adapter);

        // Retun the inflated view
        return view;
    }


}