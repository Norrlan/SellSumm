package com.example.sellsumm;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public class StaffKPIFragment extends Fragment
{

    // TODO: Rename and change types of parameters

    public StaffKPIFragment()
    {
        // Required empty public constructor
    }

    /**

     */

    public static StaffKPIFragment newInstance(String param1, String param2)
    {
        StaffKPIFragment fragment = new StaffKPIFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff_k_p_i, container, false);
    }
}