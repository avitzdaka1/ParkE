package com.omeryaari.parke.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

public class AzimutService extends Service implements SensorEventListener{

    private AzimutServiceBinder azimutServiceBinder;
    private AzimutListener azimutListener;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private Sensor accelerometerSensor;
    private float[] mGeomagnetic;
    private float[] mGravity;
    private float azimut;

    public class AzimutServiceBinder extends Binder {
        public AzimutService getService() {
            return AzimutService.this;
        }
    }

    public interface AzimutListener {
        void onRotationEvent(float rotation);
    }

    public void setListener(AzimutListener azimutListener) {
        this.azimutListener = azimutListener;
    }

    @Override
    public IBinder onBind(Intent intent) {
        azimutServiceBinder = new AzimutServiceBinder();
        return azimutServiceBinder;
    }

    public void startListening() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this, magneticFieldSensor);
        sensorManager.unregisterListener(this, accelerometerSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                // orientation contains azimut, pitch and roll
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0];
                if (azimutListener != null)
                    azimutListener.onRotationEvent(azimut * 360 / (2 * 3.14159f));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
