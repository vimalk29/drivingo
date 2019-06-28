package com.example.drivingo.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.drivingo.Common.AlertMessage;
import com.example.drivingo.Common.CommonValues;
import com.example.drivingo.Common.SessionManagement;
import com.example.drivingo.Fragments.Help;
import com.example.drivingo.Fragments.Offers;
import com.example.drivingo.R;
import com.example.drivingo.Fragments.Bookings;
import com.example.drivingo.Fragments.Profile;
import com.example.drivingo.Fragments.UserInputs;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements UserInputs.OnFragmentInteractionListener, Profile.OnFragmentInteractionListener, Bookings.OnFragmentInteractionListener,Offers.OnFragmentInteractionListener,Help.OnFragmentInteractionListener {
    private boolean doubleBackToExitPressedOnce = false;
    private SessionManagement sessionManagement;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int tabIcons[] = {
            R.drawable.icon_action_bike,
            R.drawable.icon_offers,
            R.drawable.icon_action_search,
            R.drawable.icon_help,
            R.drawable.icon_action_profile
    };

    @Override
    public void onFragmentInteraction(int code) {
        if (code == 1)//logout
            logOut();
        else if(code ==2) {//book bike
            TabLayout.Tab tab = tabLayout.getTabAt(2);
            if (tab != null)
                tab.select();
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private int NoOfTabs;

        public ViewPagerAdapter(FragmentManager fm, int noOfTabs) {
            super(fm);
            NoOfTabs = noOfTabs;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new Bookings();
                case 1:
                    return new Offers();
                case 2:
                    return new UserInputs();
                case 3:
                    return new Help();
                case 4:
                    return new Profile();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NoOfTabs;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        sessionManagement = new SessionManagement(this);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.main_tabLayout);


        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[0]));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[1]));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[2]));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[3]));
        tabLayout.addTab(tabLayout.newTab().setIcon(tabIcons[4]));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        int index=2;
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            index = bundle.getInt("open_tab",2);
        }
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if (tab != null)
            tab.select();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = new MenuInflater(this);
//        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Logout:
                logOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        sessionManagement.logOut();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, Login.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if(CommonValues.storageTask!=null&&CommonValues.storageTask.isInProgress()){
            AlertMessage.showMessageDialog(this, "Image upload is in progress !\nAre you sure want to exit?", "Yes", "No", new AlertMessage.YesNoListener() {
                @Override
                public void onDecision(boolean btnClicked) {
                    if(btnClicked){
                        CommonValues.storageTask.cancel();
                        MainActivity.super.onBackPressed();
                    }
                }
            });
            return;
        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
