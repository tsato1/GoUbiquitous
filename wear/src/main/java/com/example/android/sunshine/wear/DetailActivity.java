package com.example.android.sunshine.wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.WatchViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.sunshine.wear.data.WeatherContract;

/**
 * Created by tsato on 3/16/16.
 */
public class DetailActivity extends FragmentActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
//                savedInstanceState.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
//                savedInstanceState.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);
//
//                DetailFragment fragment = new DetailFragment();
//                fragment.setArguments(savedInstanceState);
//
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.weather_detail_container, fragment)
//                        .commit();
//
//                // Being here means we are in animation mode
//                supportPostponeEnterTransition();
            }
        });

//        if (savedInstanceState == null) {
//            // Create the detail fragment and add it to the activity
//            // using a fragment transaction.
//
//            Bundle arguments = new Bundle();
//            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
//            arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);
//
//            DetailFragment fragment = new DetailFragment();
//            fragment.setArguments(arguments);
//
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.weather_detail_container, fragment)
//                    .commit();
//
//            // Being here means we are in animation mode
//            supportPostponeEnterTransition();
//        }
    }
}
