package com.lc.monitor;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.lc.monitor.detection.MonitorFragment;
import com.lc.monitor.history.HistoryFragment;
import com.lc.monitor.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private FragmentManager mFragmentManager;

    private List<Fragment> mFragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mFragmentManager = getSupportFragmentManager();
        mFragmentList = new ArrayList<>();

        mFragmentList.add(new SettingsFragment());
        mFragmentList.add(new MonitorFragment());
        mFragmentList.add(new HistoryFragment());
        switchFragment(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_setting:
                    switchFragment(0);
                    return true;
                case R.id.navigation_dashboard:
                    switchFragment(1);
                    return true;
                case R.id.navigation_history:
                    switchFragment(2);
                    return true;
            }
            return false;
        }
    };


    private void switchFragment(int position){
        if(position == 0 && mFragmentManager.getFragments() == null
                && mFragmentManager.getFragments().size() ==0){
            mFragmentManager.beginTransaction().add(R.id.content,mFragmentList.get(0)).commitNow();
        }else{
            mFragmentManager.beginTransaction().replace(R.id.content,mFragmentList.get(position)).commit();
        }

        ToolsCallback callback = (ToolsCallback) mFragmentList.get(position);
        int titleRes = callback.getTitleRes();
        if(titleRes != 0){
            setTitle(titleRes);
        }
    }


}
