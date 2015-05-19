package com.iconasystems.christo.baalafinal;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.iconasystems.christo.common.SlidingTabLayout;


public class DrinkListActivity extends FragmentActivity {
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter(fragmentManager));

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drink_list, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SamplePagerAdapter extends FragmentPagerAdapter {

        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position){
            String title = null;
            if (position == 0){
                title = "Soft";
            }
            if (position == 1){
                title = "Whisky";
            }
            if (position == 2){
                title = "Wines";
            }
            if (position == 3){
                title = "Gin";
            }
            if (position == 4){
                title = "Spirits";
            }
            if (position == 5){
                title = "Vodka";
            }
            if (position == 6){
                title = "Beer";
            }

            return title;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0){
                fragment = new SoftDrinksFragment();
            }
            if (position == 1){
                fragment = new WhiskyFragment();
            }
            if (position == 2){
                fragment = new WineFragment();
            }
            if (position == 3){
                fragment = new GinFragment();
            }
            if (position == 4){
                fragment = new SpiritsFragment();
            }
            if (position == 5){
                fragment = new VodkaFragment();
            }
            if (position == 6){
                fragment = new BeerFragment();
            }


            return fragment;
        }

    }

}

