package com.appusagemonitor;

import android.app.Fragment;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vikas-pc on 08/12/15.
 */
public class AppUsageMonitorFragment extends Fragment implements View.OnClickListener, Comparator<UsageStats> {

    private static final String TAG = "AppUsageMonitorFragment";
    AppUsageMonitorListAdapter mAppusageListAdapter;
    UsageStatsManager mUsageStatsManager;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    Button mOpenUsageSettingButton;

    public static AppUsageMonitorFragment newInstance() {

        AppUsageMonitorFragment fragment = new AppUsageMonitorFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsageStatsManager = (UsageStatsManager)getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_usage_monitor, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAppusageListAdapter = new AppUsageMonitorListAdapter();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mAppusageListAdapter);
        mOpenUsageSettingButton = (Button)view.findViewById(R.id.button_open_usage_setting);
        List<UsageStats> usageStatsList =
                getUsageStatistics(UsageStatsManager.INTERVAL_DAILY);
        Collections.sort(usageStatsList,this);
        updateAppsList(usageStatsList);
    }

    public List<UsageStats> getUsageStatistics(int intervalType) {
        // Get the app statistics since one year ago from the current time.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (queryUsageStats.size() == 0) {
            Toast.makeText(getActivity(),
                    getString(R.string.permission_required),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setOnClickListener(this);
        }
        return queryUsageStats;
    }

    void updateAppsList(List<UsageStats> usageStatsList) {
        List<AppUsageDetails> customUsageStatsList = new ArrayList<>();
        for (int i = 0; i < usageStatsList.size(); i++) {
            AppUsageDetails customUsageStats = new AppUsageDetails();
            customUsageStats.usageStats = usageStatsList.get(i);
            try {
                Drawable appIcon = getActivity().getPackageManager()
                        .getApplicationIcon(customUsageStats.usageStats.getPackageName());
                customUsageStats.appIcon = appIcon;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, String.format("App Icon is not found for %s",
                        customUsageStats.usageStats.getPackageName()));
            }
            customUsageStatsList.add(customUsageStats);
        }
        mAppusageListAdapter.setCustomUsageStatsList(customUsageStatsList);
        mAppusageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

    }
    @Override
    public int compare(UsageStats lhs, UsageStats rhs) {
        return Long.compare(rhs.getLastTimeUsed(), lhs.getLastTimeUsed());

    }
}
