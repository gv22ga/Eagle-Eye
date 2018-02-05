package neutrinos.eagleeyeserver.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import neutrinos.eagleeyeserver.R;
import neutrinos.eagleeyeserver.fragments.FoundFragment;
import neutrinos.eagleeyeserver.fragments.MissingFragment;



public class MissingPersonActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String TAG = "Main Service";
    public static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MissingPersonActivity.this,MainActivity.class);
                startActivity(i);
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    Log.d(TAG, "message received 0");
                    RecyclerView.Adapter mAdapter = (RecyclerView.Adapter) msg.obj;
                    MissingFragment.mRecyclerView.setAdapter(mAdapter);
                    Log.d(TAG, "adapter updated 0");
                } else {
                    Log.d(TAG, "message received 1");
                    RecyclerView.Adapter mAdapter = (RecyclerView.Adapter) msg.obj;
                    FoundFragment.mRecyclerView.setAdapter(mAdapter);
                    Log.d(TAG, "adapter updated 1");
                }
            }
        };
        Intent i = new Intent(MissingPersonActivity.this, MainService.class);
        Log.d(TAG, "start service");
        startService(i);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent i = new Intent(MissingPersonActivity.this, MainService.class);
        stopService(i);

    }






    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MissingFragment.newInstance();
                case 1:
                    return FoundFragment.newInstance();
                default:
                    return MissingFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Missing";
                case 1:
                    return "Found";
            }
            return null;
        }

    }
}
