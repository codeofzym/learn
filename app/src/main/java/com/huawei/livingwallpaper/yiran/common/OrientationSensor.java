package com.huawei.livingwallpaper.yiran.common;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensor implements SensorEventListener {
    private Context mContext;
    private final SensorManager mSensorManager;
    private boolean mRegisterFlag = false;
    private boolean mVisiable;

    private float[] mMagneticValues = new float[3];
    private double mCurrentZ = 0;
    private double mCurrentY = 0;


    public OrientationSensor(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }


    public void setVisiable(boolean visiable) {
        this.mVisiable = visiable;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (this.mVisiable) {
                float[] r = new float[9];
                float[] result = new float[3];
                SensorManager.getRotationMatrix(r, null, event.values, mMagneticValues);
                SensorManager.getOrientation(r, result);
                double degreeZ = Math.toDegrees(result[0]);
                double degreeX = Math.toDegrees(result[1]);
                double degreeY = Math.toDegrees(result[2]);
//                WLog.i(this, " degreeZ:" + degreeZ + " degreeX:" + degreeX + " degreeY:" + degreeY);

                if (Math.abs(mCurrentZ - degreeY) > 10 || Math.abs(mCurrentY - degreeX) > 10) {
                    double angleX = degreeX / 90 - 1;
                    double angleY = -degreeY / 90 - 1;
                    mCurrentZ = degreeY;
                    mCurrentY = degreeX;
//                    mReadDrawBitmap.setPercent(angleY, angleX);
                }
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticValues = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface GravityChangeListener {
        public void onGravityChange(double X, double Y, double Z);
    }



}
