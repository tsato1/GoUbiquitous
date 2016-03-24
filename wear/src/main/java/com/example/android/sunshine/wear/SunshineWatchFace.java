package com.example.android.sunshine.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.wear.data.WeatherContract;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by tsato on 3/21/16.
 * Reference: http://code.tutsplus.com/tutorials/creating-an-android-wear-watch-face--cms-23718
 */
public class SunshineWatchFace extends CanvasWatchFaceService {
    private LayoutInflater mInflater;

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {
        private String TAG = Engine.class.getCanonicalName();

        public static final int COL_WEATHER_ID = 0;
        public static final int COL_WEATHER_DATE = 1;
        public static final int COL_WEATHER_DESC = 2;
        public static final int COL_WEATHER_MAX_TEMP = 3;
        public static final int COL_WEATHER_MIN_TEMP = 4;
        public static final int COL_WEATHER_HUMIDITY = 5;
        public static final int COL_WEATHER_PRESSURE = 6;
        public static final int COL_WEATHER_WIND_SPEED = 7;
        public static final int COL_WEATHER_DEGREES = 8;
        public static final int COL_WEATHER_CONDITION_ID = 9;

        private static final int MSG_UPDATE_DATA_ID = 41;
        private static final int MSG_UPDATE_TIME_ID = 42;
        private static final int DEFAULT_UPDATE_RATE_MS = 1000;
        private static final int LOAD_DATA_DELAY_MS = 1000 * 60 * 60 * 24;
        private long mUpdateRateMs = DEFAULT_UPDATE_RATE_MS;

        private Time mDisplayTime;

        private boolean mHasTimeZoneReceiverBeenRegistered = false;
        private boolean mIsInMuteMode;
        private boolean mIsLowBitAmbient;

        private FrameLayout mFrameLayout;
        private TextView mTimeTextView;
        private TextView mDateTextView;
        private ImageView mWeatherImageView;
        private TextView mHighTempTextView;
        private TextView mLowTempTextView;

        WeatherData weatherData = new WeatherData();
        public class WeatherData {
            public int weatherId;
            public String date;
            public double high;
            public double low;
        }

        private AsyncTask<Void, Void, Integer> mLoadWeatherDataTask;
        private class LoadWeatherDataTask extends AsyncTask<Void, Void, Integer> {
            @Override
            protected Integer doInBackground(Void... voids) {
                String locationSetting = Utility.getPreferredLocation(getApplicationContext());
                long today = System.currentTimeMillis();
                Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, today);

                Cursor c = getApplicationContext()
                        .getContentResolver()
                        .query(weatherForLocationUri, null, null, null, null);

                if (c != null && c.moveToFirst()) {
                    weatherData.weatherId = c.getInt(c.getColumnIndex(DetailFragment.DETAIL_COLUMNS[COL_WEATHER_CONDITION_ID]));
                    weatherData.date = Utility.getDateForWatchFace();
                    weatherData.high = c.getDouble(c.getColumnIndex(DetailFragment.DETAIL_COLUMNS[COL_WEATHER_MAX_TEMP]));
                    weatherData.low = c.getDouble(c.getColumnIndex(DetailFragment.DETAIL_COLUMNS[COL_WEATHER_MIN_TEMP]));
                }
                c.close();
                return 0;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result != null) {
                    mDateTextView.setText(weatherData.date);

                    if ( Utility.usingLocalGraphics(getApplicationContext()) ) {
                        mWeatherImageView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherData.weatherId));
                    } else {
                        // Use weather art image
                        Glide.with(getApplicationContext())
                                .load(Utility.getArtUrlForWeatherCondition(getApplicationContext(), weatherData.weatherId))
                                .error(Utility.getArtResourceForWeatherCondition(weatherData.weatherId))
                                .crossFade()
                                .into(mWeatherImageView);
                    }

                    String description = Utility.getStringForWeatherCondition(getApplicationContext(), weatherData.weatherId);
                    mWeatherImageView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

                    String highString = Utility.formatTemperature(getApplicationContext(), weatherData.high);
                    mHighTempTextView.setText(highString);
                    mHighTempTextView.setContentDescription(getString(R.string.a11y_high_temp, highString));

                    String lowString = Utility.formatTemperature(getApplicationContext(), weatherData.low);
                    mLowTempTextView.setText(lowString);
                    mLowTempTextView.setContentDescription(getString(R.string.a11y_low_temp, lowString));

                    invalidate();
                }
                if (isVisible()) {
                    mLoadWeatherDataHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA_ID, LOAD_DATA_DELAY_MS);
                }
            }
        }

        private final Handler mLoadWeatherDataHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_DATA_ID:
                        cancelLoadWeatherDataTask();
                        mLoadWeatherDataTask = new LoadWeatherDataTask();
                        mLoadWeatherDataTask.execute();
                        break;
                }
            }
        };

        private void cancelLoadWeatherDataTask() {
            if (mLoadWeatherDataTask != null){
                mLoadWeatherDataTask.cancel(true);
            }
        }

        private final Handler mTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch( msg.what ) {
                    case MSG_UPDATE_TIME_ID: {
                        invalidate();
                        if( isVisible() && !isInAmbientMode() ) {
                            long currentTimeMillis = System.currentTimeMillis();
                            long delay = mUpdateRateMs - ( currentTimeMillis % mUpdateRateMs );
                            mTimeHandler.sendEmptyMessageDelayed( MSG_UPDATE_TIME_ID, delay );
                        }
                        break;
                    }
                }
            }
        };

        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mDisplayTime.clear( intent.getStringExtra( "time-zone" ) );
                mDisplayTime.setToNow();
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);super.onCreate(holder);

            setWatchFaceStyle( new WatchFaceStyle.Builder( SunshineWatchFace.this )
                    .setBackgroundVisibility( WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE )
                    .setCardPeekMode( WatchFaceStyle.PEEK_MODE_SHORT )
                    .setShowSystemUiTime( false )
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .build()
            );

            mDisplayTime = new Time();

            mFrameLayout = (FrameLayout) mInflater.inflate(R.layout.watch_face_main, null);
            mTimeTextView = (TextView) mFrameLayout.findViewById(R.id.txv_time);
            mDateTextView = (TextView) mFrameLayout.findViewById(R.id.txv_date);
            mWeatherImageView = (ImageView) mFrameLayout.findViewById(R.id.imv_weather);
            mHighTempTextView = (TextView) mFrameLayout.findViewById(R.id.txv_high);
            mLowTempTextView = (TextView) mFrameLayout.findViewById(R.id.txv_low);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged( properties );

            if( properties.getBoolean( PROPERTY_BURN_IN_PROTECTION, false ) ) {
                mIsLowBitAmbient = properties.getBoolean( PROPERTY_LOW_BIT_AMBIENT, false );
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.d(TAG, "onAmbientModeChanged() called");

            if( inAmbientMode ) {
                //mTextColorPaint.setColor( Color.parseColor( "white" ) );
            } else {
                //mTextColorPaint.setColor( Color.parseColor( "red" ) );
            }

            if( mIsLowBitAmbient ) {
                //mTextColorPaint.setAntiAlias( !inAmbientMode );
            }

            drawTimeText();
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            mDisplayTime.setToNow();

            int widthSpec = View.MeasureSpec.makeMeasureSpec(bounds.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.EXACTLY);
            mFrameLayout.measure(widthSpec, heightSpec);
            mFrameLayout.layout(0, 0, bounds.width(), bounds.height());
            mFrameLayout.draw(canvas);

            drawTimeText();
        }

        private void drawTimeText() {
            String timeText = getHourString() + ":" + String.format( "%02d", mDisplayTime.minute );
            if( isInAmbientMode() || mIsInMuteMode ) {
                timeText += ( mDisplayTime.hour < 12 ) ? "AM" : "PM";
            } else {
                timeText += String.format( ":%02d", mDisplayTime.second);
            }
            mTimeTextView.setText(timeText);
            //canvas.drawText( timeText, mXOffset, mYOffset, mTextColorPaint );
        }

        private String getHourString() {
            if( mDisplayTime.hour % 12 == 0 )
                return "12";
            else if( mDisplayTime.hour <= 12 )
                return String.valueOf( mDisplayTime.hour );
            else
                return String.valueOf( mDisplayTime.hour - 12 );
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.d(TAG, "onVisibilityChanged() called");

            if (visible) {
            } else {
            }

            if( visible ) {
                if( !mHasTimeZoneReceiverBeenRegistered ) {

                    IntentFilter filter = new IntentFilter( Intent.ACTION_TIMEZONE_CHANGED );
                    SunshineWatchFace.this.registerReceiver( mTimeZoneBroadcastReceiver, filter );

                    mHasTimeZoneReceiverBeenRegistered = true;
                }

                mDisplayTime.clear( TimeZone.getDefault().getID() );
                mDisplayTime.setToNow();

                mLoadWeatherDataHandler.sendEmptyMessage(MSG_UPDATE_DATA_ID);
            } else {
                if( mHasTimeZoneReceiverBeenRegistered ) {
                    SunshineWatchFace.this.unregisterReceiver( mTimeZoneBroadcastReceiver );
                    mHasTimeZoneReceiverBeenRegistered = false;
                }

                mLoadWeatherDataHandler.removeMessages(MSG_UPDATE_DATA_ID);
                cancelLoadWeatherDataTask();
            }

            updateTimer();
        }

        private void updateTimer() {
            mTimeHandler.removeMessages( MSG_UPDATE_TIME_ID );
            if( isVisible() && !isInAmbientMode() ) {
                mTimeHandler.sendEmptyMessage( MSG_UPDATE_TIME_ID );
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            Log.d(TAG, "onInterruptionFilterChanged() called");

            boolean isDeviceMuted = ( interruptionFilter == android.support.wearable.watchface.WatchFaceService.INTERRUPTION_FILTER_NONE );
            if( isDeviceMuted ) {
                mUpdateRateMs = TimeUnit.MINUTES.toMillis( 1 );
            } else {
                mUpdateRateMs = DEFAULT_UPDATE_RATE_MS;
            }

            if( mIsInMuteMode != isDeviceMuted ) {
                mIsInMuteMode = isDeviceMuted;
                invalidate();
                updateTimer();
            }
        }
    }
}
