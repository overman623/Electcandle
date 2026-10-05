package com.happyhouse.electcandle;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GyroSensor implements SensorEventListener {
    private MainActivity mainActivity = null;

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;
    //Using the Gyroscope
    private Sensor accelerometerSensor = null;
    private Sensor gyroSensor = null;

    private boolean gyroRunning = false;
    private boolean accRunning = false;
    private float[] mGyroValues = new float[3];
    private float[] mAccValues = new float[3];

    private GyroCallBack gyroCallBack;

    public GyroSensor(MainActivity mainActivity, GyroCallBack gyroCallBack) {
        this.mainActivity = mainActivity;
        this.gyroCallBack = gyroCallBack;
    }

    private void initSensor() {
        //Using the Gyroscope & Accelometer
        if(mSensorManager == null){
            mSensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
            //Using the Accelometer
            accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccValues = event.values;
                if(!accRunning)
                    accRunning = true;
                break;
            case Sensor.TYPE_GYROSCOPE:
                mGyroValues = event.values;
                if(!gyroRunning)
                    gyroRunning = true;
                break;
        }

        if(gyroRunning && accRunning){
            complementaty(event.timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public boolean isStart(){
        return (gyroRunning && accRunning);
    }

    private static final float NS2S = 1.0f/1000000000.0f;

    //timestamp and dt
    private double timestamp;
    private double dt;
    private double mAccPitch, mAccRoll;
    private double temp;
    private float a = 0.2f;
    private double pitch;
    private double roll;

    private void complementaty(double new_ts){ //상보필터 : 프로젝트의 핵심이 된다.

        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll= Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);

        temp = (1/a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp*dt);

//        Log.d(TAG, "roll : "+roll);
//        Log.d(TAG, "pitch : "+pitch);
//        rotate = (int)roll;
//        rotateX = (int)pitch;
        gyroCallBack.getGyroRotate((int)pitch);
    }

    public void startGyroSensor(){
        initSensor();
    }

    public void releaseGyroSensor(){
        if(mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    public interface GyroCallBack{
        void getGyroRotate(int rotate);
    }

}
