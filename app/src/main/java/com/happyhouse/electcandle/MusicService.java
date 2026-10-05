package com.happyhouse.electcandle;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {

    public MediaPlayer mMediaPlayer;
    public final String TAG = "MeditationService";

    private String nowMusic;

    private final IBinder binder = new MusicLocalBinder();
    private BindServiceCallback bindService = null;

    private int state = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_PLAY = 2;
    public static final int STATE_NONE = 0;
    private int position = 0;

    private Uri nowUri;

    public MusicService() {
    }

    public void musicSet(Uri uri){
        musicStop();
        mMediaPlayer = MediaPlayer.create(this, uri);
        nowUri = uri;
    }

    public void musicPause() {
        mMediaPlayer.pause();
    }

    public void musicPlay() {
        mMediaPlayer.setLooping(true);  // for repeat song
        mMediaPlayer.start();
    }

    public void musicStop() {
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public int getPosition(){
        if(mMediaPlayer == null){
            return position;
        }else{
            return mMediaPlayer.getCurrentPosition();
        }
    }

    public void volumeUp(){
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    public void volumeDown(){
        mMediaPlayer.setVolume(0.0f, 0.0f);

    }

    public int getState(){
        return this.state;
    }
    public void setState(int state){
        this.state = state;
    }

    public boolean isPlayed(){
        return mMediaPlayer.isPlaying();
    }

    public void startStopCount(){
        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){

            }
        }
        //카운트롤 세서 일정한 시간이 지날경우 서비스를 스스로 없앰
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //수련 동작들 정리...
        if(mMediaPlayer == null){
//            Log.d(TAG, "Service Bind : " + nowMusic);
            nowMusic = intent.getStringExtra("NOW_MUSIC");
            mMediaPlayer = MediaPlayer.create(this, Uri.parse(nowMusic));
        }
        return binder;

    }

    public void setBindServiceCallback(BindServiceCallback callBack){
        this.bindService = callBack;
    }


    class MusicLocalBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    public interface BindServiceCallback { //볼륨 조절을 위해 남겨둠.
        void musicStartSignal(String text, int duration, int current);
        void musicEndSignal();
    }

}
