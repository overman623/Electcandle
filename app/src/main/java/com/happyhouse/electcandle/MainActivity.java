package com.happyhouse.electcandle;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.happyhouse.electcandle.bgm.DialogBgm;
import com.happyhouse.electcandle.bgm.MusicDisplayActivity;

import static com.happyhouse.electcandle.DialogTimer.getTimerString;
import static com.happyhouse.electcandle.MusicService.STATE_NONE;
import static com.happyhouse.electcandle.MusicService.STATE_PAUSE;
import static com.happyhouse.electcandle.MusicService.STATE_PLAY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GyroSensor.GyroCallBack, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "MainActivity";
    public static int displayX, displayY;
    public static String PACKAGE_NAME;
    private AdView mAdBannerView; //지역변수로 바꾸는게 좋음.
    private InterstitialAd mInterstitialAd;
    private InterstitialAd mInterstitialAd2;

    private boolean appStart = false;
    private boolean isBound = false;
    private int playTime = 0;
    private static int playNowTime = 0;
    private boolean timerSetting = false;

    public static String NOW_MUSIC;
    private int nowMusicState;
    private int nowMusicProgress;
    private boolean timerRunning = false;
    private boolean bgmSoundOnOff;
    private boolean bellSoundOnOff;
    private int breathTime;
    private boolean bellCheck;
    private boolean breathCheck;
    private boolean breathReadyCheck;
    private int breathPercent;
    private int breathStartPercent;
    private int breathIncreasePercent;
    private int breathIncreaseIndex;
    private int breathCycle = 0;
    private int breathSubCycle = 0;

    private MusicService musicService = null;
    private SoundManager soundManager = null;
    private SharedPreferences setting = null;
    private SharedPreferences.Editor setter = null;
    private RequestManager requestManager;

    private Button buttonSetBgm;
    private Button buttonSetTimer;
    private Button buttonPause;
    private Button buttonPlay;
//    private Button buttonMenu;
    private ImageView imageCandle;
//    private DrawerLayout drawerLayout;
//    private View drawerView;
    private FlameAnimationView flameAnimationView;
    private ToggleButton toggleBellOnOff;
    private ToggleButton toggleBgmOnOff;


    private Animation flameAnim;
    private GyroSensor gyroSensor;
    public static int FILE_STRING = 1;
    public static int INTRO = 2;

    private FlameAnimationView.AnimationCallBack animationCallBack = new FlameAnimationView.AnimationCallBack() {

        @Override
        public void aniBellSoundPointed() {
//            Log.d(TAG, "타종 소리");
            soundManager.playSoundBreath();
        }

        @Override
        public void aniBellEndSound() {
            soundManager.playSound();
            musicService.musicStop();
            musicService.musicSet(Uri.parse(NOW_MUSIC));
            musicService.setState(STATE_NONE);
            setRunning(false);
            showDialogAlert();
        }

        @Override
        public void aniSmokeStart() {
            buttonSetBgm.setVisibility(View.INVISIBLE);
            buttonSetTimer.setVisibility(View.INVISIBLE);
            buttonPause.setEnabled(false);
            buttonPlay.setEnabled(false);
        }

        @Override
        public void aniSmokeEnd() {
            buttonPause.setVisibility(View.INVISIBLE);
            buttonSetBgm.setVisibility(View.VISIBLE);
            buttonSetTimer.setVisibility(View.VISIBLE);
            buttonPause.setEnabled(true);
            buttonPlay.setEnabled(true);
            gyroSensor.releaseGyroSensor();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void changeTimed(int time) {
            buttonPause.setText(getTimerString(playTime - time));
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //가로화면 고정
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getPackageName();
        setting = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        setter = setting.edit();

        if(savedInstanceState == null){ //처음실행시에는 이 변수가 비워져 있다.
            Log.d(TAG, "===================savedInstanceState == null======================");
            startActivityForResult(new Intent(getApplicationContext(), IntroActivity.class), 0);
            NOW_MUSIC = setting.getString("NOW_MUSIC", "android.resource://" + PACKAGE_NAME + "/raw/music1");
            playTime = setting.getInt("PLAY_TIME", 3600);
            bgmSoundOnOff = setting.getBoolean("BGM_SOUND_ON_OFF", true);
            bellSoundOnOff = setting.getBoolean("BELL_SOUND_ON_OFF", true);
            breathTime = setting.getInt("BREATH_TIME", 60);
            if (breathTime <= 0) {
                breathTime = 60;
            }
            bellCheck = setting.getBoolean("BELL_CHECK", true);
            breathCheck = setting.getBoolean("BREATH_CHECK", true);
            breathReadyCheck = setting.getBoolean("BREATH_READY_CHECK", false);
            breathPercent = setting.getInt("BREATH_PERCENT", 50);
            breathStartPercent = setting.getInt("BREATH_START_PERCENT", 50);
            breathIncreasePercent = setting.getInt("BREATH_INCREASE_PERCENT", 10);
            breathIncreaseIndex = setting.getInt("BREATH_INCREASE_INDEX", 4);
        }else{
            Log.d(TAG, "===================savedInstanceState not null======================");
            appStart = savedInstanceState.getBoolean("APP_START");            //변수로 저장해놨던 값을 기억시켜서 다른 변수를 초기화한다.
            NOW_MUSIC = savedInstanceState.getString("NOW_MUSIC");
            nowMusicState = savedInstanceState.getInt("MUSIC_STATE");
            nowMusicProgress = savedInstanceState.getInt("MUSIC_PROGRESS");
            playTime = savedInstanceState.getInt("PLAY_TIME");
            playNowTime = savedInstanceState.getInt("PLAY_NOW_TIME");
            timerRunning = savedInstanceState.getBoolean("TIMER_RUNNING");
            bgmSoundOnOff = savedInstanceState.getBoolean("BGM_SOUND_ON_OFF");
            bellSoundOnOff = savedInstanceState.getBoolean("BELL_SOUND_ON_OFF");
            breathTime = savedInstanceState.getInt("BREATH_TIME");
            bellCheck = savedInstanceState.getBoolean("BELL_CHECK");
            breathCheck = savedInstanceState.getBoolean("BREATH_CHECK");
            breathReadyCheck = savedInstanceState.getBoolean("BREATH_READY_CHECK");
            breathPercent = savedInstanceState.getInt("BREATH_PERCENT");
            breathStartPercent = savedInstanceState.getInt("BREATH_START_PERCENT");
            breathIncreasePercent = savedInstanceState.getInt("BREATH_INCREASE_PERCENT");
            breathIncreaseIndex = savedInstanceState.getInt("BREATH_INCREASE_INDEX");
            breathCycle = savedInstanceState.getInt("BREATH_CYCLE");
            breathSubCycle = savedInstanceState.getInt("BREATH_SUB_CYCLE");
            //여기다가 일단 카운트 입력해두고..

            setRunning(timerRunning);
            if(timerRunning){
                Object retainedObject = getLastCustomNonConfigurationInstance();
                if (retainedObject != null) {
                    Log.d(TAG, "======================SET NEW THREAD==== backgroundThread : is not null================");
                    if(!isBound) { //서비스가 바인드 되지 않을때 서비스를 바인드하고 대기 시킴. //한번만 실행하는것이 좋다.
                        Intent musicIntent = new Intent(this, MusicService.class);
                        musicIntent.putExtra("NOW_MUSIC", NOW_MUSIC);
                        bindService(musicIntent, musicPlayConnection, Context.BIND_AUTO_CREATE);
                    }
                } else {
                    Log.d(TAG, "======================SET NEW THREAD==== backgroundThread : is null================");
                }
            }
        }

        MobileAds.initialize(this);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayX = size.x; // 1080
        displayY = size.y; // 2094

        setContentView(R.layout.activity_main);
        requestManager = Glide.with(this);

        //파일 읽기 권한 요청 - 앱 설치하고 한번만 실행됨.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        soundManager = new SoundManager(this);
        soundManager.loadingSound(R.raw.ring);

        initAdBanner();
        initAdFront();
        initLayout();

        flameAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_flame); //불꽃은 항상 떨어야 함.
        /*smokeAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_smoke);
        smokeAnim.setFillEnabled(true);
        smokeAnim.setInterpolator(AnimationUtils.loadInterpolator(getBaseContext(),
                        android.R.anim.decelerate_interpolator));
        smokeAnim.setFillAfter(true);
        smokeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                buttonSetBgm.setVisibility(View.INVISIBLE);
                buttonSetTimer.setVisibility(View.INVISIBLE);
                buttonPause.setEnabled(false);
                buttonPlay.setEnabled(false);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                smokeAnim.cancel();
//                imageObject.clearAnimation();
//                imageObject.setVisibility(View.INVISIBLE);
                buttonPause.setVisibility(View.INVISIBLE);
                buttonSetBgm.setVisibility(View.VISIBLE);
                buttonSetTimer.setVisibility(View.VISIBLE);
                buttonPause.setEnabled(true);
                buttonPlay.setEnabled(true);
                gyroSensor.releaseGyroSensor();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });*/
        gyroSensor = new GyroSensor(this, this);
    }

    @Override
    public void onBackPressed() { //가장 확실한 종료상태         //백버튼 누르는 경우가 있고
        Log.d(TAG, "===================onBackPressed======================");
        //카운트 무시하고 그자리에서 카운트만 없애기
        //음악 시작중이라면 음악을 없애기
        //서비스를 해재하기
        //사운드매니저를 해재하기
        //여기서 종료할건지 말건지 물어봐야함.
        appStart = false;
/*        if(drawerLayout.isDrawerOpen(drawerView)){
            drawerLayout.closeDrawer(drawerView);
            return;
        }*/

        showDialogExit();
//        showDialogAlert();
//        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "===================onStart======================");

        if(!isBound) { //서비스가 바인드 되지 않을때 서비스를 바인드하고 대기 시킴. //한번만 실행하는것이 좋다.
            Intent musicIntent = new Intent(this, MusicService.class);
            musicIntent.putExtra("NOW_MUSIC", NOW_MUSIC);
            bindService(musicIntent, musicPlayConnection, Context.BIND_AUTO_CREATE);
        }else{
            if(musicService.mMediaPlayer != null){
                if(!musicService.mMediaPlayer.isPlaying()){
                    if(musicService.getState() == STATE_PLAY) {
                        musicService.musicPlay();
                        musicService.setState(STATE_PLAY);
                        gyroSensor.startGyroSensor();
                    }
                }
            }else{
                musicService.musicSet(Uri.parse(NOW_MUSIC));
            }
        }
        super.onStart();
        appStart = true;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {// 화면 회전일때만실행
        Log.d(TAG, "===================onRetainCustomNonConfigurationInstance===11111=================== : " + timerRunning);
        if (timerRunning) {
            Log.d(TAG, "===================onRetainCustomNonConfigurationInstance======222222================");
                        //서비스도 언바인드.
            //setRunning(false);
            if(musicService.mMediaPlayer.isPlaying()){
                musicService.musicPause();
            }
            musicService.musicStop();
            musicService.stopSelf();
            return musicService;
        }
        return null;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "===================onPause======================");
//        if(musicService.mMediaPlayer != null){
/*            if(musicService.mMediaPlayer.isPlaying()){
                musicService.musicPause();
                musicService.setState(STATE_PLAY);
            }*/
            Log.d(TAG, "===================onPause==musicService.mMediaPlayer != null====================");
        gyroSensor.releaseGyroSensor();

        if(!appStart){ //뒤로가기 버튼으로 종료할때 나옴.
            Log.d(TAG, "===================onPause==appStart:false====================");
            musicService.musicStop();
            //카운트에 따라서 결정하게된다.
            setRunning(false);
            if(isBound){
                Log.d(TAG, "==================service unbind=======================");
                unbindService(musicPlayConnection);
                isBound = false;
            }
            soundManager.release();
        }

//        }
        super.onPause();
        //음악 정지
        //카운트 세고 안되면 서비스도 정지
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "===================onDestroy======================");
        if(isBound){
            unbindService(musicPlayConnection);
        }
        playNowTime = 0;
        if(soundManager != null)
            soundManager.release();

        gyroSensor.releaseGyroSensor();
        super.onDestroy();
        //서비스 해재
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {//화면 회전이나 특수키를 눌렀을때 실행
        Log.d(TAG, "========================onSaveInstanceState========================");
        super.onSaveInstanceState(outState);
        outState.putBoolean("APP_START", appStart);
        outState.putString("NOW_MUSIC", NOW_MUSIC);
        int musicState = STATE_NONE;
        int musicProgress = 0;
        if (musicService != null) {
            musicState = musicService.getState();
            musicProgress = musicService.getPosition();
        }
        outState.putInt("MUSIC_STATE", musicState);
        outState.putInt("MUSIC_PROGRESS", musicProgress);
        outState.putInt("PLAY_NOW_TIME", playNowTime);
        outState.putInt("PLAY_TIME", playTime);
        outState.putBoolean("TIMER_RUNNING", timerRunning); //슬로우 옵션이 걸렸는지 본다.
        outState.putBoolean("BGM_SOUND_ON_OFF", toggleBgmOnOff.isChecked());
        outState.putBoolean("BELL_SOUND_ON_OFF", toggleBellOnOff.isChecked());

        outState.putInt("BREATH_TIME", breathTime);
        outState.putBoolean("BELL_CHECK", bellCheck);
        outState.putBoolean("BREATH_CHECK", breathCheck);
        outState.putBoolean("BREATH_READY_CHECK", breathReadyCheck);
        outState.putInt("BREATH_PERCENT", breathPercent);
        outState.putInt("BREATH_START_PERCENT", breathStartPercent);
        outState.putInt("BREATH_INCREASE_PERCENT", breathIncreasePercent);
        outState.putInt("BREATH_INCREASE_INDEX", breathIncreaseIndex);
        outState.putInt("BREATH_CYCLE", breathCycle);
        outState.putInt("BREATH_SUB_CYCLE", breathSubCycle);

        //들숨 날숨 초 설정. 들숨 날숨 횟수
        //종소리 기능. 활성화 여부
        //배경음 꺼짐
        //종소리 잠시 꺼짐
        //outState.putSerializable("instance", a); // putString등도 가능, map collection처럼 키와 값 쌍으로 전달.

    }

    ServiceConnection musicPlayConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "==================onServiceConnected=======================");
            MusicService.MusicLocalBinder meditationLocalBinder = (MusicService.MusicLocalBinder) service;
            musicService = meditationLocalBinder.getService();
            musicService.setBindServiceCallback(bindServiceCallback);
            musicService.musicSet(Uri.parse(NOW_MUSIC));
            toggleBellOnOff.setChecked(bellSoundOnOff);
            toggleBgmOnOff.setChecked(bgmSoundOnOff);

            if(nowMusicState == STATE_PLAY){
                Log.d(TAG, "==================onServiceConnected == STATE_PLAY=====================");
                musicService.setState(nowMusicState);
                musicService.mMediaPlayer.seekTo(nowMusicProgress);
                musicService.musicPlay();//음악이 여러번 플레이 되는거....서비스는 다른 객체가 되는거...
//                timerStart();
            }else if(nowMusicState == STATE_PAUSE){
                Log.d(TAG, "==================onServiceConnected == STATE_PAUSE=====================");
                musicService.setState(nowMusicState);
                musicService.mMediaPlayer.seekTo(nowMusicProgress);
            }

            isBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "==================onServiceDisconnected=======================");
//            musicPlayItem.setIcon(android.R.drawable.ic_media_play); //재생 버튼으로 UI 설정
            isBound = false;
        }

    };

    MusicService.BindServiceCallback bindServiceCallback = new MusicService.BindServiceCallback() {
        @Override
        public void musicStartSignal(String text, int duration, int current) {
        }
        @Override
        public void musicEndSignal() {
            Log.d(TAG, "musicEndSignal");
        }
    };

    void setRunning(boolean b) {
        timerRunning = b;
    }

    private void initAdBanner() { //베너광고와 종료 후 광고를 초기화함
        mAdBannerView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdBannerView.loadAd(adRequest);//베너 광고 //test ok //앱 아이디를 바꿔야 함.
    }

    private void initAdFront() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_front), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }
        });

        AdRequest adRequest2 = new AdRequest.Builder().build();
        InterstitialAd.load(this, getString(R.string.ad_end), adRequest2, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd2 = interstitialAd;
            }
        });
    }

    DialogBgm dialogBgmLayout = null;
    private void showDialogBgm(){
//        tts.speak((10 + "회"), TextToSpeech.QUEUE_FLUSH, null, null);
//        Set<String> bgmList = setting.getStringSet("BGM_LIST", null); //저장했었던 bgm 부르기
        dialogBgmLayout = new DialogBgm(this, NOW_MUSIC);
        dialogBgmLayout.setLayoutResource(R.layout.layout_bgm_dialog);
        dialogBgmLayout.setBgmDialogCallback(new DialogBgm.BgmDialogCallback() {
            @Override
            public void bgmSet(String musicString) {
                Log.d(TAG, "bgmSetFile : " + musicString);
                musicService.musicStop();
                musicService.musicSet(Uri.parse(musicString));
                NOW_MUSIC = musicString;
                setter.putString("NOW_MUSIC", musicString);
                setter.commit();
            }
        });

        dialogBgmLayout.show();
        dialogBgmLayout.setGetBgmOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicDisplayActivity.class);
                startActivityForResult(intent, 0);
            }
        });

    }

    private void showDialogTimer(final int nowTime){
//        mInterstitialAd.show(); //전면광고
        final DialogTimer dialogLayout = new DialogTimer(this, nowTime, breathTime, bellCheck, breathCheck, breathReadyCheck, breathPercent,
                breathStartPercent, breathIncreasePercent, breathIncreaseIndex);
        dialogLayout.setLayoutResource(R.layout.layout_timer_dialog);
        dialogLayout.show();
        dialogLayout.setTimerDialogCallback(new DialogTimer.TimerDialogCallback() {

            @Override
            public void timeSet(int hour, int min, int sec) {
                int timeSum ;
                timeSum = hour * 3600;
                timeSum += min * 60;
                timeSum += sec;
                if(timeSum == 0) return;
                Log.d(TAG, "timeSum : "+ timeSum);
                playTime = timeSum;
                setter.putInt("PLAY_TIME", playTime);
                setter.commit();
            }

            @Override
            public void breathSet(int sec, boolean isBell, boolean isBreath, boolean isBreathReady, int percent, int startPercent, int increasePercent, int increaseIndex) {
                //멈춤을 시도하는 도중에 설정을 바꿨을 경우...
                //처음부터 다시 시도해야 한다.

                breathTime = sec; //옵션 저장하기.
                bellCheck = isBell;
                breathCheck = isBreath;
                breathReadyCheck = isBreathReady;
                breathPercent = percent;
                breathStartPercent = startPercent;
                breathIncreasePercent = increasePercent;
                breathIncreaseIndex = increaseIndex;

                if(flameAnimationView.isPaused()){
                    timerSetting = true;
                }

                flameAnimationView.setVariable(breathIncreaseIndex, playTime, breathCheck, breathReadyCheck, breathTime, breathPercent, breathStartPercent, breathIncreasePercent, bellCheck);
                flameAnimationView.initVariable();

                setter.putInt("BREATH_TIME", sec);
                setter.putBoolean("BELL_CHECK", isBell);
                setter.putBoolean("BREATH_CHECK", isBreath);
                setter.putBoolean("BREATH_READY_CHECK", isBreathReady);
                setter.putInt("BREATH_PERCENT", percent);
                setter.putInt("BREATH_START_PERCENT", startPercent);
                setter.putInt("BREATH_INCREASE_PERCENT", increasePercent);
                setter.putInt("BREATH_INCREASE_INDEX", increaseIndex);
                setter.commit();
            }
        });
    }

    private void showDialogAlert(){
        final DialogAlert dialogLayout = new DialogAlert(this);
        dialogLayout.setLayoutResource(R.layout.layout_alert_dialog);
        dialogLayout.setAlertDialogCallback(new DialogAlert.AlertDialogCallback() {
            @Override
            public void callBack() {
                playNowTime = 0;
//                imageObject.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.skin2_smoke));
                buttonPause.setText("");
//                imageObject.startAnimation(smokeAnim);
                breathCycle = 0;
                breathSubCycle = 0;
            }
        });
        dialogLayout.show();
        //촛불 원래 크기로 돌아오게됨.
    }

    private void showDialogExit(){
        final DialogAdBanner dialogLayout = new DialogAdBanner(this);
        dialogLayout.setLayoutResource(R.layout.layout_adbanner_dialog);
        dialogLayout.show();
        dialogLayout.btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLayout.cancel();
                finish();
            }
        });
    }

    private void initLayout() {
        // Code to be executed when an ad finishes loading.
        imageCandle = findViewById(R.id.image_candle);
        requestManager.load(R.drawable.skin2_candle).into(imageCandle);

        buttonPlay = findViewById(R.id.button_timer_play);
        buttonPause = findViewById(R.id.button_timer_pause);

        ViewGroup.LayoutParams params = buttonPause.getLayoutParams();
        params.width = (int)(displayX * 0.5);
        params.height = params.width;
        buttonPause.setLayoutParams(params);

        ViewGroup.LayoutParams params2 = buttonPlay.getLayoutParams();
        params2.width = (int)(params.width * 0.3);
        params2.height = params2.width;
        buttonPlay.setLayoutParams(params2);

        buttonSetBgm = findViewById(R.id.button_bgm);
        buttonSetTimer = findViewById(R.id.button_timer);
        buttonSetBgm.setBackgroundResource(R.drawable.xml_bgm_dialog_btn);
        buttonSetTimer.setBackgroundResource(R.drawable.xml_timer_dialog_btn);

//        buttonMenu = findViewById(R.id.button_menu);

        buttonPlay.setOnClickListener(this);
        buttonPause.setOnClickListener(this);
        buttonSetBgm.setOnClickListener(this);
        buttonSetTimer.setOnClickListener(this);

//        buttonMenu.setOnClickListener(this);
//        drawerLayout = findViewById(R.id.layout_drawer);
//        drawerView = findViewById(R.id.layout_include_drawer);

        toggleBellOnOff = findViewById(R.id.button_bell_on_off);
        toggleBgmOnOff = findViewById(R.id.button_bgm_on_off);
        toggleBgmOnOff.setBackgroundResource(R.drawable.xml_sound_toggle_btn);
        toggleBellOnOff.setBackgroundResource(R.drawable.xml_bell_toggle_btn);
        toggleBellOnOff.setOnCheckedChangeListener(this);
        toggleBgmOnOff.setOnCheckedChangeListener(this);

        flameAnimationView = findViewById(R.id.layout_flame_animation_view);
        /*Log.d(TAG, "breathIncreaseIndex : " + breathIncreaseIndex + " playTime : " + playTime + " breathCheck : " + breathCheck + " breathReadyCheck : " + breathReadyCheck
                + " breathTime : " + breathTime + " breathPercent : " + breathPercent
                + " breathStartPercent : " + breathStartPercent + " breathIncreasePercent : " + breathIncreasePercent + " bellCheck : " + bellCheck);*/
        flameAnimationView.setVariable(breathIncreaseIndex, playTime, breathCheck, breathReadyCheck, breathTime, breathPercent, breathStartPercent, breathIncreasePercent, bellCheck);
        flameAnimationView.setAnimationCallBack(animationCallBack);
        flameAnimationView.initVariable();

        if(nowMusicState == STATE_PLAY){
//            imageObject.setVisibility(View.VISIBLE);
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
            buttonSetBgm.setVisibility(View.INVISIBLE);
            buttonSetTimer.setVisibility(View.INVISIBLE);
            gyroSensor.startGyroSensor();
            //flameAnimationView.countVariable();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else if(nowMusicState == STATE_PAUSE){
//            imageObject.setVisibility(View.VISIBLE);
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
            buttonSetBgm.setVisibility(View.INVISIBLE);
            buttonSetTimer.setVisibility(View.INVISIBLE);
        }else{//none 초기상태.
//            imageObject.setVisibility(View.INVISIBLE);
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
            buttonSetBgm.setVisibility(View.VISIBLE);
            buttonSetTimer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == FILE_STRING){
            String musicPath = data.getStringExtra("FILE_OBJECT");
            dialogBgmLayout.setNowMusic(musicPath);
            dialogBgmLayout.nowBgmText.setText("BGM : " + musicPath.substring(musicPath.lastIndexOf("/") + 1));
        }else if(resultCode == INTRO){
            //광고
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    mInterstitialAd.show(); //스레드로 구현함.
                }
            }).start(); //일단이렇게 해두고..*/
            if(!setting.getBoolean("APP_FIRST_START", false)){
                startActivity(new Intent(getApplicationContext(), HelpActivity.class));//도움말 실행
                setter.putBoolean("APP_FIRST_START", true);
                setter.commit();
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            //만약 반환값이 없을 경우의 코드를 여기에 작성하세요.
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_bgm) {
                showDialogBgm();
        } else if (id == R.id.button_timer) {
                showDialogTimer(playTime);
        } else if (id == R.id.button_timer_play) { //심지 클릭.
                if(!musicService.mMediaPlayer.isPlaying()) { //음악 안나올때.
                    gyroSensor.startGyroSensor();
//                    imageObject.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.skin4_flame));
                    musicService.musicPlay();
                    musicService.setState(STATE_PLAY);
//                    imageObject.setVisibility(View.VISIBLE);
                    flameAnimationView.startAnimation(flameAnim); //떨리는 에니메이션
                    buttonPause.clearAnimation();
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonSetBgm.setVisibility(View.INVISIBLE);
                    buttonSetTimer.setVisibility(View.INVISIBLE);
                    toggleBellOnOff.setChecked(bellSoundOnOff);
                    toggleBgmOnOff.setChecked(bgmSoundOnOff);
                    setRunning(true);
//                    timerStart(); //촛불 켜기
                    if(flameAnimationView.isPaused()){
                        flameAnimationView.resume();//불꽃이 확대/축소 에니메이션.
                    }else{
                        flameAnimationView.start();//불꽃이 확대/축소 에니메이션.
                    }

                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }else{//촛불끄기 -> STOP
                    musicService.musicStop();
                    musicService.musicSet(Uri.parse(NOW_MUSIC));
                    musicService.setState(STATE_NONE);
                    playNowTime = 0;
                    setRunning(false);
                    buttonPause.setText("");
                    flameAnimationView.clearAnimation();
                    flameAnimationView.stop();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    breathCycle = 0;
                    breathSubCycle = 0;
                }
        } else if (id == R.id.button_timer_pause) {
                if(timerSetting){
                    flameAnimationView.start();
                    musicService.musicPlay();
                    setRunning(true);
                    buttonPause.clearAnimation();
                    gyroSensor.startGyroSensor();
                    flameAnimationView.startAnimation(flameAnim); //불꽃떨리는 애니메이션
                    buttonSetBgm.setVisibility(View.INVISIBLE);
                    buttonSetTimer.setVisibility(View.INVISIBLE);
                    toggleBellOnOff.setChecked(bellSoundOnOff);
                    toggleBgmOnOff.setChecked(bgmSoundOnOff);
                    musicService.setState(STATE_PLAY);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    timerSetting = false;
                    return;
                }

                if(musicService.getState() == STATE_PLAY){
//                    gyroSensor.startGyroSensor();
                    gyroSensor.releaseGyroSensor();
                    Animation pauseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_blink);
                    buttonPause.startAnimation(pauseAnimation); //텍스트 깜밖거림.
                    musicService.musicPause();
                    musicService.setState(STATE_PAUSE);
                    setRunning(false);
                    flameAnimationView.clearAnimation();
                    flameAnimationView.pause();
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonSetBgm.setVisibility(View.VISIBLE);
                    buttonSetTimer.setVisibility(View.VISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }else{
                    musicService.musicPlay();
                    setRunning(true);
//                    timerStart();
                    gyroSensor.startGyroSensor();
                    flameAnimationView.startAnimation(flameAnim); //불꽃떨리는 애니메이션
                    flameAnimationView.resume();
                    musicService.setState(STATE_PLAY);
                    buttonPause.clearAnimation();
                    buttonSetBgm.setVisibility(View.INVISIBLE);
                    buttonSetTimer.setVisibility(View.INVISIBLE);
                    toggleBellOnOff.setChecked(bellSoundOnOff);
                    toggleBgmOnOff.setChecked(bgmSoundOnOff);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
        }
    }

    @Override
    public void getGyroRotate(int rotate) {
        if(rotate > -15 && rotate < 15){
            flameAnimationView.setRotation(rotate); //test ok
//            flameAnimationView.setRotation(-rotate); //test ok
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.button_bell_on_off) {
                if(isChecked){ //종소리 활성화
                    soundManager.volumeOn();
                }else{ //종소리 비활성화
                    soundManager.volumeOff();
                }
                setter.putBoolean("BGM_SOUND_ON_OFF", isChecked);
        } else if (id == R.id.button_bgm_on_off) {
                if(isChecked){
                     //배경음 활성화
                    if(musicService.mMediaPlayer != null){
                        musicService.mMediaPlayer.setVolume(1,1);
                    }
                }else{
                    if(musicService.mMediaPlayer != null){
                        musicService.mMediaPlayer.setVolume(0,0);
                    }
                     //배경음 비활성화
                }
                setter.putBoolean("BELL_SOUND_ON_OFF", isChecked);
        }
        setter.commit();
    }

}
