package com.example.android.sunshine.wear;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
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
//                stub.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//                    @Override
//                    public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
//                        if (windowInsets.isRound()) {
//                            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                            Display display = window.getDefaultDisplay();
//                            Point point = new Point (0,0);
//                            display.getSize(point);
//                            int width = (int) convertPixelsToDp((float)point.x, getApplicationContext());
//                            int height = (int) convertPixelsToDp((float)point.y, getApplicationContext())
//                                    - (int) getResources().getDimension(R.dimen.detail_box_inset_padding);
//
//                            Log.d("test", String.valueOf(height));
//                            view.findViewById(R.id.view_background_today).setMinimumHeight(height * 6 / 10);
//                            view.findViewById(R.id.view_background_extra).setMinimumHeight(height * 4 / 10);
//                        }
//
//                        return windowInsets;
//                    }
//                });

                if (savedInstanceState == null) {
                    // Create the detail fragment and add it to the activity
                    // using a fragment transaction.

                    Bundle arguments = new Bundle();
                    arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
                    arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);

                    DetailFragment fragment = new DetailFragment();
                    fragment.setArguments(arguments);

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.weather_detail_container, fragment)
                            .commit();

                    // Being here means we are in animation mode
                    supportPostponeEnterTransition();
                }
            }
        });
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}
