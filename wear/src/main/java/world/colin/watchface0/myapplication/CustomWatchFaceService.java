package world.colin.watchface0.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Colin Reilly on 10/9/2015.
 */
public class CustomWatchFaceService extends CanvasWatchFaceService {
    /*
    * Update rate in milliseconds for interactive mode. We update once a second to advance the
    * second hand.
    */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);


    @Override
    public Engine onCreateEngine() {
        /*provide your watch face implementation */
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;

        private Calendar mCalendar;
        private boolean mRegisteredTimeZoneReceiver = false;

        // device features
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mIsRound;
        private int mChinSize;

        // graphic object
        private Paint mDateColorPaint;
        private Paint mBackgroundColorPaint;
        private Paint mBackgroundColorPaint1;
        private Paint mBackgroundColorPaint2;
        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mTickPaint;

        // handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
            mChinSize = insets.getSystemWindowInsetBottom();
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            CustomWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            CustomWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();

            invalidate();
        }

        // service methods (see other sections)

        @Override
        public void onCreate(SurfaceHolder holder) {
            /* super is used to call methods from the parent I.e. super.aMethod()
            stackoverflow.com/questions/3767365/super-in-java */
            super.onCreate(holder);

            /* initialize your watch face */

            // configure the system UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(CustomWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            // create graphic styles
            mHourPaint = new Paint();
            mHourPaint.setARGB(255, 166, 171, 165);
            mHourPaint.setStrokeWidth(7.0f);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.SQUARE);

            mMinutePaint = new Paint();
            mMinutePaint.setARGB(255, 166, 171, 165);
            mMinutePaint.setStrokeWidth(7.0f);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.SQUARE);

            mSecondPaint = new Paint();
            mSecondPaint.setARGB(255, 176, 89, 73);
            mSecondPaint.setStrokeWidth(4.0f);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.SQUARE);

            mTickPaint = new Paint();
            mTickPaint.setARGB(255, 166, 171, 165);
            mTickPaint.setStrokeWidth(7.0f);
            mTickPaint.setAntiAlias(true);
            mTickPaint.setStrokeCap(Paint.Cap.SQUARE);

            mBackgroundColorPaint = new Paint();
            mBackgroundColorPaint.setStrokeWidth(10.0f);
            mBackgroundColorPaint.setAntiAlias(true);
            mBackgroundColorPaint.setARGB(255, 166, 171, 165);

            mBackgroundColorPaint1 = new Paint();
            mBackgroundColorPaint1.setStrokeWidth(10.0f);
            mBackgroundColorPaint1.setAntiAlias(true);
            mBackgroundColorPaint1.setARGB(255, 35, 39, 42);

            mBackgroundColorPaint2 = new Paint();
            mBackgroundColorPaint2.setAntiAlias(true);
            mBackgroundColorPaint2.setARGB(255, 38, 45, 53);
            mBackgroundColorPaint2.setTextSize(20);

            mDateColorPaint = new Paint();
            mDateColorPaint.setARGB(255, 166, 171, 165);
            mDateColorPaint.setTextSize(15);
            mDateColorPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));

            // allocate a Calendar to calculate local time using the UTC time and time zone
            mCalendar = Calendar.getInstance();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mTickPaint.setAntiAlias(antiAlias);
            }

            if (inAmbientMode) {
                mBackgroundColorPaint.setARGB(255, 0, 0, 0);
                mBackgroundColorPaint1.setARGB(255, 0, 0, 0);
                mBackgroundColorPaint2.setARGB(255, 0, 0, 0);
                mDateColorPaint.setARGB(255, 0, 0, 0);
                mTickPaint.setARGB(255, 0, 0, 0);
            }
            else {
                mBackgroundColorPaint.setARGB(255, 166, 171, 165);
                mBackgroundColorPaint1.setARGB(255, 35, 39, 42);
                mBackgroundColorPaint2.setARGB(255, 38, 45, 53);
                mDateColorPaint.setARGB(255, 166, 171, 165);
                mTickPaint.setARGB(255, 166, 171, 165);
            }

            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Update the time
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            // Constant to help calculate clock hand rotations
            final float TWO_PI = (float) Math.PI * 2f;

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Compute rotations and lengths for the clock hands.
            float seconds = mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f;
            float secRot = seconds / 60f * TWO_PI;
            float minutes = mCalendar.get(Calendar.MINUTE) + seconds / 60f;
            float minRot = minutes / 60f * TWO_PI;
            float hours = mCalendar.get(Calendar.HOUR) + minutes / 60f;
            float hrRot = hours / 12f * TWO_PI;

            float secLength = centerX - 20;
            float minLength = centerX - 40;
            float hrLength = centerX - 80;

            // Calculate the date
            int mDate = mCalendar.get(Calendar.DAY_OF_MONTH);

            // Fill the background
            if (mIsRound) {
                canvas.drawCircle(centerX, centerY, width, mBackgroundColorPaint);
                canvas.drawCircle(centerX, centerY, (width / 2) * .94f, mBackgroundColorPaint1);
                canvas.drawCircle(centerX, centerY, (width / 2) * .86f, mBackgroundColorPaint2);
                if (mChinSize > 0) {
                    canvas.drawLine(0, height - mChinSize - 5, width, height - mChinSize - 5, mBackgroundColorPaint);
                    canvas.drawLine(width * .25f, height - mChinSize - 15, width * .75f, height - mChinSize - 15, mBackgroundColorPaint1);
                }
            }
            else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundColorPaint);
                canvas.drawRect(width * .02f, height * .02f, bounds.width() * .98f, bounds.height() * .98f, mBackgroundColorPaint1);
                canvas.drawRect(width * .07f, height * .07f, bounds.width() * .93f, bounds.height() * .93f, mBackgroundColorPaint2);
            }

            // Draw the date
            canvas.drawRect(width * .695f, height * .456f, width * .80f, height * .545f, mDateColorPaint);
            canvas.drawRect(width * .705f, height * .465f, width * .79f, height * .535f, mBackgroundColorPaint1);
            canvas.drawText( "" + mDate, width * .72f, height * .515f, mDateColorPaint);

            // Only draw the second hand in interactive mode.
            if (!isInAmbientMode()) {
                float secX = (float) Math.sin(secRot) * secLength;
                float secY = (float) -Math.cos(secRot) * secLength;
                float secButtX = (float) Math.sin(secRot) * 15;
                float secButtY = (float) -Math.cos(secRot) * 15;
                canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mSecondPaint);
                canvas.drawLine(centerX, centerY, centerX - secButtX, centerY - secButtY, mSecondPaint);
            }

            // Draw the minute and hour hands.
            float minX = (float) Math.sin(minRot) * minLength;
            float minY = (float) -Math.cos(minRot) * minLength;
            float minButtX = (float) Math.sin(minRot) * 15;
            float minButtY = (float) -Math.cos(minRot) * 15;
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY , mMinutePaint);
            canvas.drawLine(centerX, centerY, centerX - minButtX, centerY - minButtY, mMinutePaint);
            float hrX = (float) Math.sin(hrRot) * hrLength;
            float hrY = (float) -Math.cos(hrRot) * hrLength;
            float hrButtX = (float) Math.sin(hrRot) * 15;
            float hrButtY = (float) -Math.cos(hrRot) * 15;
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHourPaint);
            canvas.drawLine(centerX, centerY, centerX - hrButtX, centerY - hrButtY, mHourPaint);

            // Draw the ticks around the clock
            canvas.drawLine(width * .96f, centerY, width, centerY, mTickPaint);
            canvas.drawLine(width * .04f, centerY, 0, centerY, mTickPaint);
            canvas.drawLine(centerX, 0, centerX, height * .04f, mTickPaint);
            canvas.drawLine(centerX, height * .96f, centerX, height, mTickPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
            if(visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible and
            // whether we're in ambient mode, so we may need to start or stop the timer
            updateTimer();
        }
    }
}