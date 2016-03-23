package com.example.android.sunshine.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
        private Uri mUri;

        private Typeface WATCH_TEXT_TYPEFACE = Typeface.create( Typeface.SERIF, Typeface.NORMAL );

        private static final int MSG_UPDATE_TIME_ID = 42;
        private static final int DEFAULT_UPDATE_RATE_MS = 1000;
        private long mUpdateRateMs = DEFAULT_UPDATE_RATE_MS;

        private Time mDisplayTime;

        //private Paint mBackgroundColorPaint;
        //private Paint mTextColorPaint;

        private boolean mHasTimeZoneReceiverBeenRegistered = false;
        private boolean mIsInMuteMode;
        private boolean mIsLowBitAmbient;

        private float mXOffset;
        private float mYOffset;

        private int mBackgroundColor = Color.parseColor( "black" );
        private int mTextColor = Color.parseColor( "red" );

        private FrameLayout mFrameLayout;
        private TextView mTimeTextView;
        private TextView mDateTextView;
        private ImageView mWeatherImageView;
        private TextView mHighTempTextView;
        private TextView mLowTempTextView;

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
            mWeatherImageView = (ImageView) mFrameLayout.findViewById(R.id.imv_weather);
            mHighTempTextView = (TextView) mFrameLayout.findViewById(R.id.txv_high);
            mLowTempTextView = (TextView) mFrameLayout.findViewById(R.id.txv_low);

            //initBackground();
            //initDisplayText();
        }

        private void initBackground() {
            //mBackgroundColorPaint = new Paint();
            //mBackgroundColorPaint.setColor( mBackgroundColor );
        }

        private void drawBackground( Canvas canvas, Rect bounds ) {
            //canvas.drawRect( 0, 0, bounds.width(), bounds.height(), mBackgroundColorPaint );
        }

        private void initDisplayText() {
            //mTextColorPaint = new Paint();
            //mTextColorPaint.setColor( mTextColor );
            //mTextColorPaint.setTypeface( WATCH_TEXT_TYPEFACE );
            //mTextColorPaint.setAntiAlias( true );
            //mTextColorPaint.setTextSize( getResources().getDimension( R.dimen.watch_face_text_size ) );
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
                //mTextView
                //mTextColorPaint.setColor( Color.parseColor( "white" ) );
            } else {
                //mTextColorPaint.setColor( Color.parseColor( "red" ) );
            }

            if( mIsLowBitAmbient ) {
                //mTextColorPaint.setAntiAlias( !inAmbientMode );
            }

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

            //drawBackground( canvas, bounds );
            drawTimeText( canvas );
            //loadFromDatabase();
        }

        private void drawTimeText( Canvas canvas ) {
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

            if( visible ) {
                if( !mHasTimeZoneReceiverBeenRegistered ) {

                    IntentFilter filter = new IntentFilter( Intent.ACTION_TIMEZONE_CHANGED );
                    SunshineWatchFace.this.registerReceiver( mTimeZoneBroadcastReceiver, filter );

                    mHasTimeZoneReceiverBeenRegistered = true;
                }

                mDisplayTime.clear( TimeZone.getDefault().getID() );
                mDisplayTime.setToNow();
            } else {
                if( mHasTimeZoneReceiverBeenRegistered ) {
                    SunshineWatchFace.this.unregisterReceiver( mTimeZoneBroadcastReceiver );
                    mHasTimeZoneReceiverBeenRegistered = false;
                }
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

//            mYOffset = getResources().getDimension( R.dimen.y_offset );
//
//            if( insets.isRound() ) {
//                mXOffset = getResources().getDimension( R.dimen.x_offset_round );
//            } else {
//                mXOffset = getResources().getDimension( R.dimen.x_offset_square );
//            }
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
                int alpha = ( isDeviceMuted ) ? 100 : 255;
                //mTextColorPaint.setAlpha( alpha );
                invalidate();
                updateTimer();
            }
        }

        private void loadFromDatabase() {
            String locationSetting = Utility.getPreferredLocation(getApplicationContext());
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSetting, System.currentTimeMillis());

            Cursor c = getApplicationContext()
                    .getContentResolver()
                    .query(weatherForLocationUri, null, null, null, null);

            if (c.moveToFirst()) {
                // Read weather condition ID from cursor
                int weatherId = c.getInt(DetailFragment.COL_WEATHER_CONDITION_ID);

                if ( Utility.usingLocalGraphics(getApplicationContext()) ) {
                    mWeatherImageView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                } else {
                    // Use weather art image
                    Glide.with(getApplicationContext())
                            .load(Utility.getArtUrlForWeatherCondition(getApplicationContext(), weatherId))
                            .error(Utility.getArtResourceForWeatherCondition(weatherId))
                            .crossFade()
                            .into(mWeatherImageView);
                }

                // Read date from cursor and update views for day of week and date
                long date = c.getLong(DetailFragment.COL_WEATHER_DATE);
                String dateText = Utility.getFullFriendlyDayString(getApplicationContext(),date);
                mDateTextView.setText(dateText);

                // Get description from weather condition ID
                String description = Utility.getStringForWeatherCondition(getApplicationContext(), weatherId);
//                mDescriptionView.setText(description);
//                mDescriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

                // For accessibility, add a content description to the icon field. Because the ImageView
                // is independently focusable, it's better to have a description of the image. Using
                // null is appropriate when the image is purely decorative or when the image already
                // has text describing it in the same UI component.
                mWeatherImageView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

                double high = c.getDouble(DetailFragment.COL_WEATHER_MAX_TEMP);
                String highString = Utility.formatTemperature(getApplicationContext(), high);
                mHighTempTextView.setText(highString);
                mHighTempTextView.setContentDescription(getString(R.string.a11y_high_temp, highString));

                // Read low temperature from cursor and update view
                double low = c.getDouble(DetailFragment.COL_WEATHER_MIN_TEMP);
                String lowString = Utility.formatTemperature(getApplicationContext(), low);
                mLowTempTextView.setText(lowString);
                mLowTempTextView.setContentDescription(getString(R.string.a11y_low_temp, lowString));
            }
            c.close();
        }
    }
}
