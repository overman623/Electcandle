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
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;


public class DrawerView extends View {

    private static final String TAG = DrawerView.class.getName();

    private Bitmap flame, smoke;
    private int flamePositionLeft = 0;
    private int flamePositionTop = 0;
    private int smokePositionLeft = 0;
    private int smokePositionTop = 0;
    private boolean flameLive = true;
    private boolean smokeLive = false;
    private int width = 0;
    private int height = 0;
    private String[] breathNumArray;
    private int arrayIndex;
    private ValueAnimator animator = new ValueAnimator();


    private float endPoint;
    private float inholePoint; //halfpoint가 아니라 호흡을 잘라야 할 지점임.
    private int afterOneDivideTenSec = 0;
    private int afterOneSec = 0;
    private int breathCycle = 0;
    private int breathCycleCount = 0;
    private int count = 0;

    private int playTime = 0;
    private int breathTime = 0;
    private float breathPercent;
    private boolean breathCheck = false; //애니메이션을 실행할지 물어보는것
    private boolean bellCheck = false; //종소리를 실행한건지를 체크한다.
    private boolean breathReadyCheck = false; //예비 호흡의 실행 유무를 보게 된다.
    private int breathNum = 0;
    private float startPercent;
    private float increasePercent;

    //재생시간
    //호흡시간
    //종소리유무
    //예비 호흡시간 체크
    //시작값과 증가값
    //pause를 하고 다시 시작하면 처음부터 다시 시작하는걸로 해야할것 같음.

    public DrawerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBitmap();
        initVariable();
        initAnimation();
    }

    //변수의 초기값을 설정해주고
    private void initVariable() {
        arrayIndex = 1;
        breathNumArray = getResources().getStringArray(R.array.breath_time);
        breathNum = Integer.valueOf(breathNumArray[arrayIndex]); //기본 0 이므로 각 5회 호흡이 될것이다.
        playTime = 160; //재생시간은 60초로 설정함.
        breathCheck = true;
        breathReadyCheck = true;
        breathTime = 10;
        breathPercent = 0.5f;
        startPercent = 0.5f;
        increasePercent = 0.1f;
        bellCheck = false;

        //float a = (1 - startPercent) / increasePercent;
        //increase 10 5번
        //increase 20 2번

    }

    private void initAnimation() {
        float handleBreathTime = breathTime; //duration 에반영함.
        if(breathReadyCheck){
            inholePoint = handleBreathTime * startPercent * breathPercent;
            endPoint = (handleBreathTime * startPercent); //startPercent초기값은 일단 50으로 해본다.
        }else{
            inholePoint = handleBreathTime * breathPercent;
            endPoint = handleBreathTime;
        }

        Log.d(TAG, "============================계산된 endPoint : " + endPoint); //처음에는 기존의 값이 나와야한다.
        animator.setDuration((int)(endPoint * 1000)); // 1회 절하는 시간
        //초기 옵션 설정.
        animator.setValues(
                PropertyValuesHolder.ofKeyframe("dx", Keyframe.ofFloat(0,1), Keyframe.ofFloat(breathPercent,1.2f),Keyframe.ofFloat(1,1)),
                PropertyValuesHolder.ofKeyframe("dy", Keyframe.ofFloat(0,1), Keyframe.ofFloat(breathPercent,1.4f),Keyframe.ofFloat(1,1))
        );

        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                postInvalidateOnAnimation();
//                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Log.d(TAG, "onAnimationEnd");
            }

            @Override
            public void onAnimationRepeat(Animator animation) { //반복 주기가 일정하지 않음. //repeat Count를이용할 수도 있을 것같다.
                super.onAnimationRepeat(animation);
                Log.d(TAG, "onAnimationRepeat : " + testTime);
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
        animator.start();
        Log.d(TAG, "====================에니메이션 시작====================inholePoint : " + inholePoint + " endPoint : " + endPoint);
    }

    private void initBitmap() {
        flame = BitmapFactory.decodeResource(getResources(), R.drawable.skin1_flame);
        smoke = BitmapFactory.decodeResource(getResources(), R.drawable.skin2_smoke);
//        flame = Bitmap.createScaledBitmap(flame, (flame.getWidth()), (int)(flame.getHeight() * 0.8), true);
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
        invalidate();
        //듀레이션이 바껴도,,, 촛불의 재생이 유연해야 한다는게 1차 목표이다.
    }

    int testTime = 0;
    float handleBreathTimeTest = 5;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //기본 inhole은 0.5f로 지정함.

        if (animator.isRunning()) {
            int time = (int)(animator.getCurrentPlayTime() * 0.01);
            if(time != afterOneDivideTenSec){ //0.1초에 한번씩 실행
                float time2 = (float)(time * 0.1);

                if((int)time2 == playTime){ //플레이시간에 3초정도 더하는걸로 가는게 좋을것 같다.
                    //플레이 시간이 모두 끝나면 모든걸 종료함. //모든 값을 초기값으로 돌려야함.
                    Log.d(TAG, "플레이 시간이 자나서 종료됨. : " + (int)time2); //test ok;
                    //종소리가 재생중이었다면 그 즉시 멈추고 완료 종소리를 내야함.
                    count = 0;
                    breathCycle = 0;
                    breathCycleCount = 0;
                    animator.end();
                    canvas.drawBitmap(smoke, smokePositionLeft, smokePositionTop, null); //연기가 나오지만 잠깐 보여진다.
                    float handleBreathTime = breathTime; //duration 에반영함.
                    if(breathCheck){
                        inholePoint = handleBreathTime * startPercent * breathPercent;
                        endPoint = handleBreathTime * startPercent; //startPercent초기값은 일단 0.5 로 해본다.
                    }else{
                        inholePoint = handleBreathTime * breathPercent;
                        endPoint = handleBreathTime;
                    }
                    return;
                }

                if(!breathCheck){
                    afterOneSec = (int)time2; //시간 보내기
                }else{
                    /*
                    if(time2 == inholePoint - 1){
                        //inhole 조정 //타종
                        Log.d(TAG, "절반 타종 : " + time2);
                    }
                    if(time2 == endPoint - 1 && time2 != playTime - 1){
                        //exhole 조정 //타종
                        Log.d(TAG, "1회 완료 타종 : " + time2);
                    }
                    */
                    if(time2 == inholePoint){
                        float valueY = (float) animator.getAnimatedValue("dy");
                        //Log.d(TAG, "절반 타종 : " + time2 + " 크기 값 : " + valueY); //inhole 조정 //타종
                    }

                    if(time2 == endPoint && time2 != playTime - 1){
                        float valueY = (float) animator.getAnimatedValue("dy");
                        //Log.d(TAG, "1회 완료 타종 : " + time2 + " 크기 값 : " + valueY); //exhole 조정 //타종
                    }
                }

                if((int)time2 != afterOneSec && breathCheck){ //1초에 한번씩 실행.

//                    Log.d(TAG, "animator.getDuration() : " + animator.getDuration());
                    if(count + 1 == (int)endPoint){ //호흡이 1회씩 올라간다고 생각하면됨.
                        //inholePoint도 조정됨
                        //예비 호흡에 따라서 endPoint 가 조정될것 같음.
                        //breathCycle에서는
                        //breathCheck가 true일때
                        float handleBreathTime = breathTime;
                        float nowPercent = startPercent + (breathCycle * increasePercent);
                        if(breathReadyCheck && nowPercent < 1) { //사이클을 올려야함

                            breathCycleCount++;
                            if (breathCycleCount == breathNum) { //breathNum은 사이클이 하나 올라가기 위한 호흡수를 말한다. 초기에는 breathNum은 1로 설정되었음.
                                breathCycle++;
                                breathCycleCount = 0; //호흡 주기가 끝남.
//                                animator.pause();
                            }

                            float increaseTime = (breathCycle * (handleBreathTime * increasePercent));
                            handleBreathTime = (handleBreathTime * (startPercent)); //3.5 -> 3.0
                            handleBreathTime = (handleBreathTime + increaseTime);


                            animator.setDuration((int)(handleBreathTimeTest * 1000)); //여기는 정상..
                            animator.start();
                        }
//                        handleBreathTimeTest = handleBreathTime;
                        animator.setDuration((int)(handleBreathTimeTest * 1000)); //여기는 정상..
                       // Log.d(TAG, "get duration : " + animator.getDuration() + " duration time : " + (int)(handleBreathTime * 1000));

                        inholePoint = endPoint + (handleBreathTime * breathPercent);
                        endPoint = endPoint + handleBreathTime;
                        //Log.d(TAG, "다음 호흡시간이 설정됨=========time2 : " + (int)time2 + " inholePoint : " + inholePoint + " endPoint : " + endPoint);
                    }

                    count++;
                    testTime = (int)time2;
                    afterOneSec = (int)time2;
                    //시간 보내기
                }
                afterOneDivideTenSec = time;
            }

            float valueX = (float) animator.getAnimatedValue("dx");
            float valueY = (float) animator.getAnimatedValue("dy");

            if(breathCheck){
                canvas.scale(valueX, valueY, (int)(width * 0.5), height); // 최대크기 설정.
            }
            if(animator.getAnimatedFraction() == 1){
                Log.d(TAG, "animator.getAnimatedFraction() : " + animator.getAnimatedFraction());
            }
            canvas.drawBitmap(flame, flamePositionLeft, flamePositionTop, null);

        }
    }
}
