package com.happyhouse.electcandle;

import android.content.Context;
import android.media.SoundPool;

/**
 * Created by overm on 2019-02-10.
 */

public class SoundManager {

    private Context context;
    public SoundPool soundPool;
    int dokk;
    int dokk2;
    private SoundManagerCallBack soundManagerCallBack;
    float soundLeft, soundRight;

    public SoundManager(Context context) {
        this.context = context;
        soundInit();
    }

    public void setSoundManager(SoundManagerCallBack soundManagerCallBack) {
        this.soundManagerCallBack = soundManagerCallBack;
    }

    private void soundInit(){
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
    }

    boolean played = false;

    public void loadingSound(int soundResource){
        dokk2 = soundPool.load(context, soundResource, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                soundManagerCallBack.soundEnd();
//                playSound2();
            }
        });
        dokk = soundPool.load(context, soundResource, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

            }
        });
    }

    public void playSound(){
//        soundManagerCallBack.soundStart();
        soundPool.play(dokk2, 1f, 1f, 0, 2, 1.0f);
    }

    public void playSoundBreath(){
        soundPool.play(dokk, soundLeft, soundRight, 0, 0, 1.0f);
    }
    public void stop(){
        soundPool.stop(dokk2);
    }

    public void release(){
        soundPool.release();
        soundPool = null;
        soundInit();
    }

    public void volumeOff(){
        soundLeft = 0.0f;
        soundRight = 0.0f;
    }
    public void volumeOn(){
        soundLeft = 1.0f;
        soundRight = 1.0f;
    }
    interface SoundManagerCallBack{
        void soundStart();
        void soundEnd();
    }

}
