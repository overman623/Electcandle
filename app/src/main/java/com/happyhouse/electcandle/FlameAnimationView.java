package com.happyhouse.electcandle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

public class FlameAnimationView extends View {

    private static final String TAG = FlameAnimationView.class.getName();

    private Bitmap flame, smoke;
    private int flamePositionLeft = 0;
    private int flamePositionTop = 0;
    private int smokePositionLeft = 0;
    private int smokePositionTop = 0;

    private int width, height;

//    private String[] breathNumArray;
//    private int arrayIndex;
    private ValueAnimator animatorFlame = null;
    private ValueAnimator animatorSmoke = null;

    private float endPoint;
    private float inholePoint; //halfpoint가 아니라 호흡을 잘라야 할 지점임.
    private int afterOneDivideTenSec = 0;
    private int afterOneSec = -1;
    private int breathCycle = 0;
    private int breathCycleCount = 0;
    private int count = 0;

    private int playTime = 0;
    private int breathTime = 60;
    private float breathPercent;
    private boolean breathCheck = false; //애니메이션을 실행할지 물어보는것
    private boolean bellCheck = false; //종소리를 실행한건지를 체크한다.
    private boolean breathReadyCheck = false; //예비 호흡의 실행 유무를 보게 된다.
    private int breathNum = 0;
    private float startPercent;
    private float increasePercent;

    private Paint smokePaint = new Paint();
    private AnimationCallBack animationCallBack = null;


    //재생시간
    //호흡시간
    //종소리유무
    //예비 호흡시간 체크
    //시작값과 증가값
    //pause를 하고 다시 시작하면 처음부터 다시 시작하는걸로 해야할것 같음.

    public FlameAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBitmap();
        initVariable();
        initAnimation();
    }

    public void setAnimationCallBack(AnimationCallBack animationCallBack){
        this.animationCallBack = animationCallBack;
    }

    public void setVariable(int arrayIndex, int playTime, boolean breathCheck, boolean breathReadyCheck,
                            int breathTime, float breathPercent, float startPercent, float increasePercent, boolean bellCheck){
//        this.arrayIndex = arrayIndex;
        //실제로 적용해야할것.
        String[] breathNumArray = getResources().getStringArray(R.array.breath_time);
        this.breathNum = Integer.valueOf(breathNumArray[arrayIndex]);
        this.playTime = playTime;
        this.breathCheck = breathCheck;
        this.breathReadyCheck = breathReadyCheck;
        this.breathTime = breathTime;
        this.breathPercent = (float)(breathPercent * 0.01);
        this.startPercent = (float)(startPercent * 0.01);
        this.increasePercent = (float)(increasePercent * 0.01);
        this.bellCheck = bellCheck;

/*        arrayIndex = 1;
        String[] breathNumArray = getResources().getStringArray(R.array.breath_time);
        this.breathNum = Integer.valueOf(breathNumArray[arrayIndex]); //기본 0 이므로 각 5회 호흡이 될것이다.
        this.playTime = 120; //재생시간은 60초로 설정함.
        this.breathCheck = true;
        this.breathReadyCheck = true;
        this.breathTime = 10;
        this.breathPercent = 0.5f;
        this.startPercent = 0.5f;
        this.increasePercent = 0.1f;
        this.bellCheck = true;*/
    }
    //breathIncreaseIndex : 4 playTime : 3600 breathCheck : true breathReadyCheck : false breathTime : 10 breathPercent : 50 breathStartPercent : 50 breathIncreasePercent : 10 bellCheck : true

    //변수의 초기값을 설정해주고
    public void initVariable() {
        float handleBreathTime = breathTime; //duration 에반영함.
        if(breathReadyCheck){
            inholePoint = handleBreathTime * startPercent * breathPercent;
            endPoint = (handleBreathTime * startPercent); //startPercent초기값은 일단 50으로 해본다.
        }else{
            inholePoint = handleBreathTime * breathPercent;
            endPoint = handleBreathTime;
        }
        initAnimation();
    }

    private void initAnimation() {
        Log.d(TAG, "============================계산된 endPoint : " + endPoint); //처음에는 기존의 값이 나와야한다.
        animatorFlame = null;
        animatorFlame = new ValueAnimator();
        animatorFlame.setDuration((int)(endPoint * 1000)); // 1회 절하는 시간
        count = 0;
        breathCycle = 0;
        breathCycleCount = 0;
        afterOneDivideTenSec = 0;
        afterOneSec = -1;
        //초기 옵션 설정.
        animatorFlame.setValues(
                PropertyValuesHolder.ofKeyframe("dx", Keyframe.ofFloat(0,1), Keyframe.ofFloat(breathPercent,1.2f),Keyframe.ofFloat(1,1)),
                PropertyValuesHolder.ofKeyframe("dy", Keyframe.ofFloat(0,1), Keyframe.ofFloat(breathPercent,1.4f),Keyframe.ofFloat(1,1))
        );
//        animatorFlame.setCurrentFraction(0f);
        animatorFlame.setRepeatCount(ValueAnimator.INFINITE);
        animatorFlame.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                postInvalidateOnAnimation();
//                invalidate();
            }
        });

        animatorFlame.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d(TAG, "on Flame AnimationEnd");
                count = 0;
                breathCycle = 0;
                breathCycleCount = 0;
                afterOneDivideTenSec = 0;
                afterOneSec = -1;
                animatorSmoke = null;
                animatorSmoke = new ValueAnimator();
                animatorSmoke.setDuration(3000); //연기 보여주는 시간
                animatorSmoke.setRepeatCount(0); //연기 반복수는 한번.
                animatorSmoke.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        postInvalidateOnAnimation();
                    }
                });
                animatorSmoke.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Log.d(TAG, "on Smoke AnimationEnd");
                        animationCallBack.aniSmokeEnd();
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animationCallBack.aniSmokeStart();
                    }
                });
                animatorSmoke.setIntValues(100, 0); //점점 투명해짐.
                animatorSmoke.start();

            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });

        Log.d(TAG, "====================에니메이션 시작====================inholePoint : " + inholePoint + " endPoint : " + endPoint);
    }

    private void initBitmap() {
        flame = BitmapFactory.decodeResource(getResources(), R.drawable.skin1_flame);
        smoke = BitmapFactory.decodeResource(getResources(), R.drawable.skin2_smoke);

        flame = Bitmap.createScaledBitmap(flame, (flame.getWidth()), (int)(flame.getHeight() * 0.8), true); //-> 이렇게 설정하면 기본 크기가 나오게됨.
        smoke = Bitmap.createScaledBitmap(smoke, (int)(smoke.getWidth() * 0.5), (int)(smoke.getHeight() * 0.5), true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {//원래 사이즈에서 줄어들었기 때문이다. 그래서 두번 실행됨.
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        flamePositionLeft = ((w / 2) - (flame.getWidth() / 2));
        flamePositionTop = (h - flame.getHeight());
        smokePositionLeft = ((w / 2) - (smoke.getWidth() / 2));
        smokePositionTop = (h - smoke.getHeight());
        this.setPivotX(w/2);
        this.setPivotY(h);
        invalidate();
        //듀레이션이 바껴도,,, 촛불의 재생이 유연해야 한다는게 1차 목표이다.
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //기본 inhole은 0.5f로 지정함.

        if (animatorFlame.isRunning()) {
            int time = (int)(animatorFlame.getCurrentPlayTime() * 0.01);
            if(time != afterOneDivideTenSec){ //0.1초에 한번씩 실행
                float time2 = (float)(time * 0.1);

                if(count == playTime + 1){ //플레이시간에 3초정도 더하는걸로 가는게 좋을것 같다.
                    //플레이 시간이 모두 끝나면 모든걸 종료함. //모든 값을 초기값으로 돌려야함.
                    Log.d(TAG, "플레이 시간이 자나서 종료됨. : "); //test ok;
                    //종소리가 재생중이었다면 그 즉시 멈추고 완료 종소리를 내야함.

                    animatorFlame.cancel();

                    //새로운 에니메이션을 판다.
                    if(bellCheck)
                        animationCallBack.aniBellEndSound();

                    return;
                }

                if((int)time2 != afterOneSec && !breathCheck){
                    Log.d(TAG, "소요된 총 시간 Count : " + count + " time2 : " + time2);
                    animationCallBack.changeTimed(count);
                    count++;
                    afterOneSec = (int)time2; //시간 보내기
                }else{
//                    if(time2 == inholePoint - 1){
                    if(time2 == inholePoint){
                        Log.d(TAG, "절반 타종 : " + time2 ); //inhole 조정 //타종
                        if(bellCheck)
                            animationCallBack.aniBellSoundPointed();
                    }

//                    if(time2 == endPoint - 1 && count != playTime - 1){
                    if(time2 == endPoint && count != playTime){
//                        Log.d(TAG, "1회 완료 타종 : " + time2); //exhole 조정 //타종
                        if(bellCheck)
                            animationCallBack.aniBellSoundPointed();
                    }
                }
                if((int)time2 != afterOneSec && breathCheck){ //1초에 한번씩 실행.
                    Log.d(TAG, "소요된 총 시간 Count : " + count + " time2 : " + time2);

                    if((int)time2 == endPoint){
                        //float valueY = (float) animatorFlame.getAnimatedValue("dy");
                        float handleBreathTime = breathTime;
                        float nowPercent = startPercent + (breathCycle * increasePercent);
                        if(breathReadyCheck && nowPercent < 1) { //사이클을 올려야함

                            breathCycleCount++;
                            if (breathCycleCount == breathNum) {
                                breathCycle++;
                                breathCycleCount = 0; //호흡 주기가 끝남.
                            }

                            float increaseTime = (breathCycle * (handleBreathTime * increasePercent));
                            handleBreathTime = (handleBreathTime * (startPercent)); //3.5 -> 3.0
                            handleBreathTime = (handleBreathTime + increaseTime);

                        }
//                        Log.d(TAG, "handleBreathTime : " + handleBreathTime + " getAnimatedValue : " + valueY + " 소요된 총 시간 : " + count);
                        inholePoint = handleBreathTime * breathPercent;
                        endPoint = handleBreathTime;
                        animatorFlame.setDuration((int)(handleBreathTime * 1000));
                        animatorFlame.start(); //start로 했을때는 동작이 괜찮았음. //resume는 동작이 안됌.
                    }else{
                        animationCallBack.changeTimed(count);
                        count++;
                    }
                    afterOneSec = (int)time2;
                }

            }
            afterOneDivideTenSec = time;
        }

        float valueX = (float) animatorFlame.getAnimatedValue("dx");
        float valueY = (float) animatorFlame.getAnimatedValue("dy");

        if(breathCheck){
            canvas.scale(valueX, valueY, (int)(width * 0.5), height); // 최대크기 설정. //실제로 실행해 보니까 크기가 제대로 늘어나지 않았다는것을 알수 있음.
        }

        if(animatorFlame.isStarted()){
            canvas.drawBitmap(flame, flamePositionLeft, flamePositionTop, null);
        }else{
            if(animatorSmoke != null && animatorSmoke.isStarted()){
                int alpha = (int) animatorSmoke.getAnimatedValue("");
                smokePaint.setAlpha(alpha);
                canvas.drawBitmap(smoke, smokePositionLeft, smokePositionTop, smokePaint); //연기가 나오지만 잠깐 보여진다.
            }
        }

    }

    public void stop(){
        animatorFlame.cancel();
    }

    public void pause(){
        animatorFlame.pause();
    }

    public void resume(){
        animatorFlame.resume();
    }

    public void start(){
        animatorFlame.start();
    }

    public boolean isPaused(){
        return animatorFlame.isPaused();
    }

    public void initCountVariable(int count, int breathCycleCount, int breathCycle){ //보류.. 현재 재생된 시간도 세팅을 해야 이 함수는 쓸수 있음.
        this.count = count;
        this.breathCycle = breathCycle;
        this.breathCycleCount = breathCycleCount;
        float handleBreathTime = breathTime;
        float nowPercent = startPercent + (this.breathCycle * increasePercent);
        if(breathReadyCheck && nowPercent < 1) { //사이클을 올려야함

            this.breathCycleCount++;
            if (this.breathCycleCount == breathNum) {
                this.breathCycle++;
                this.breathCycleCount = 0; //호흡 주기가 끝남.
            }

            float increaseTime = (this.breathCycle * (handleBreathTime * increasePercent));
            handleBreathTime = (handleBreathTime * (startPercent)); //3.5 -> 3.0
            handleBreathTime = (handleBreathTime + increaseTime);
            inholePoint = handleBreathTime * breathPercent;
            endPoint = handleBreathTime;
        }else{
            inholePoint = handleBreathTime * breathPercent;
            endPoint = breathTime;
        }
        initAnimation();
    }


    public interface AnimationCallBack{
        void aniBellSoundPointed(); //절 1회의 절반지점
        void aniBellEndSound();
        void aniSmokeStart();
        void aniSmokeEnd();
        void changeTimed(int time);
    }

}
