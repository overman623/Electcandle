package com.happyhouse.electcandle;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

import static com.happyhouse.electcandle.MainActivity.displayX;
import static com.happyhouse.electcandle.MainActivity.displayY;


class DialogTimer extends AlertDialog implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    public static final String TAG = "DialogTimer";
    private int layoutResource;

    private Button btnSave;
    private Button btnCancel;
    private Button btnBack;

    private NumberPicker pickerHour;
    private NumberPicker pickerMin;
    private NumberPicker pickerSec;
    private NumberPicker pickerBreathTime;

    private SeekBar seekBreathPercent;
    private TextView textBreathPercent;
    private CheckBox checkBreath;
    private CheckBox checkBellSound;
    private CheckBox checkBreathReady;
    private EditText editBreathStartPercent;
    private EditText editBreathIncreasePercent;
    private Spinner spinnerBreathTime;

    private LinearLayout layoutTimer;

    private int nowTime;
    private int breathTime;
    private boolean bellCheck;
    private boolean breathCheck;
    private boolean breathReadyCheck;
    private int breathPercent;
    private int breathStartPercent;
    private int breathIncreasePercent;
    private int breathIncreaseIndex;

    private TimerDialogCallback timerDialogCallback = null;

    public DialogTimer(Context context, int nowTime, int breathTime, boolean bellCheck, boolean breathCheck, boolean breathReadyCheck, int breathPercent, int breathStartPercent, int breathIncreasePercent, int breathIncreaseIndex) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.nowTime = nowTime;
        this.breathTime = breathTime;
        this.bellCheck = bellCheck;
        this.breathCheck = breathCheck;
        this.breathReadyCheck = breathReadyCheck;
        this.breathPercent = breathPercent;
        this.breathStartPercent = breathStartPercent;
        this.breathIncreasePercent = breathIncreasePercent;
        this.breathIncreaseIndex = breathIncreaseIndex;
    }

    protected DialogTimer(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setLayoutResource(int layoutResource){
        this.layoutResource = layoutResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 1.0f;
//        lpWindow.dimAmount = 0.8f;

        getWindow().setAttributes(lpWindow);
        getWindow().setLayout(displayX, (int)(displayY * 0.95));
//        getWindow().setLayout(displayX, displayY);
        getWindow().setGravity(Gravity.TOP);
        setContentView(this.layoutResource);
        setLayout();
    }

    private void setLayout(){
        //타이머 시간 > 호흡 시간
        //예비 호흡에서 초기값과 증가값을 입력할때 크기를 재야함.

        //이 변수로 다이얼로그 타임을 세팅할것임.
        Log.d(TAG, "now time : " + nowTime);
        pickerHour = findViewById(R.id.picker_hour);
        pickerMin = findViewById(R.id.picker_min);
        pickerSec = findViewById(R.id.picker_sec);
        pickerBreathTime = findViewById(R.id.picker_breath_time);
        pickerBreathTime.setValue(this.breathTime);

        pickerHour.setMaxValue(99);
        pickerHour.setMinValue(0);
        pickerMin.setMaxValue(59);
        pickerMin.setMinValue(0);
        pickerSec.setMaxValue(59);
        pickerSec.setMinValue(0);
        pickerBreathTime.setMaxValue(999);
        pickerBreathTime.setMinValue(10);
        changeDividerColor(pickerMin, Color.BLACK);
        changeDividerColor(pickerHour, Color.BLACK);
        changeDividerColor(pickerSec, Color.BLACK);
        changeDividerColor(pickerBreathTime, Color.BLACK);

        textBreathPercent = findViewById(R.id.text_breath_percent);
        textBreathPercent.setText(this.breathPercent + "%");
        seekBreathPercent = findViewById(R.id.seek_breath_percent);
        seekBreathPercent.setProgress(this.breathPercent);
        seekBreathPercent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textBreathPercent.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        checkBreath = findViewById(R.id.check_breath);
        checkBellSound = findViewById(R.id.check_bell_sound);
        checkBreathReady = findViewById(R.id.check_breath_ready);
        textBreathPercent = findViewById(R.id.text_breath_percent);
        spinnerBreathTime = findViewById(R.id.spinner_breath_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.breath_time, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.layout_spinner_text_color);
        // Apply the adapter to the spinner
        spinnerBreathTime.setAdapter(adapter);
        spinnerBreathTime.setOnItemSelectedListener(this);
        spinnerBreathTime.setSelection(breathIncreaseIndex);

        layoutTimer = findViewById(R.id.layout_setting_time);

        editBreathStartPercent = findViewById(R.id.edit_breath_start_percent);
        editBreathIncreasePercent = findViewById(R.id.edit_breath_increase_percent);
        editBreathStartPercent.setText(""+this.breathStartPercent);
        editBreathIncreasePercent.setText(""+this.breathIncreasePercent);
        editBreathStartPercent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    layoutTimer.setVisibility(View.GONE);
                }else{
                    layoutTimer.setVisibility(View.VISIBLE);
                }
            }
        });
        editBreathIncreasePercent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    layoutTimer.setVisibility(View.GONE);
                }else{
                    layoutTimer.setVisibility(View.VISIBLE);
                }
            }
        });

        checkBreath.setOnCheckedChangeListener(this);
        checkBellSound.setOnCheckedChangeListener(this);
        checkBreathReady.setOnCheckedChangeListener(this);

        checkBellSound.setChecked(this.bellCheck);
        checkBreathReady.setChecked(this.breathReadyCheck);
        checkBreath.setChecked(this.breathCheck); //순서가 좀 중요함.
        if(!this.breathCheck){
            checkBreathReady.setChecked(false);
            checkBreathReady.setEnabled(false);
            pickerBreathTime.setEnabled(false);
            seekBreathPercent.setEnabled(false);
            editBreathStartPercent.setEnabled(false);
            editBreathIncreasePercent.setEnabled(false);
            spinnerBreathTime.setEnabled(false);
        }

        int modTime = nowTime;
        int mod;

        mod = (modTime % 3600);
        modTime = (modTime / 3600);
        pickerHour.setValue(modTime);
        modTime = mod;
        modTime = modTime / 60;
        mod = mod % 60;
        pickerMin.setValue(modTime);
        pickerSec.setValue(mod);
        pickerBreathTime.setValue(breathTime);
        btnSave = findViewById(R.id.timer_save);
        btnCancel = findViewById(R.id.timer_cancel);
        btnBack = findViewById(R.id.btn_back_arrow);
        btnBack.setBackgroundResource(R.drawable.ic_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = pickerHour.getValue();
                int min = pickerMin.getValue();
                int sec = pickerSec.getValue();
                timerDialogCallback.timeSet(hour, min, sec);
                timerDialogCallback.breathSet(pickerBreathTime.getValue(),checkBellSound.isChecked(), checkBreath.isChecked(), checkBreathReady.isChecked(),
                        seekBreathPercent.getProgress(), Integer.valueOf(editBreathStartPercent.getText().toString()),
                        Integer.valueOf(editBreathIncreasePercent.getText().toString()), breathIncreaseIndex);
                cancel();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
//        RelativeLayout layoutDialogTimer = findViewById(R.id.layout_dialog_timer);

//        ViewGroup.LayoutParams textParam = (ViewGroup.LayoutParams) layoutDialogTimer.getLayoutParams();
//        textParam.
//        layoutHeader.setLayoutParams(parameter);
        RelativeLayout layoutDialogTimer = findViewById(R.id.layout_dialog_timer);
        registerView(layoutDialogTimer);
    }

    private void changeDividerColor(NumberPicker picker, int color) {
        try {
            Field mField = NumberPicker.class.getDeclaredField("mSelectionDivider");
            mField.setAccessible(true);
            ColorDrawable colorDrawable = new ColorDrawable(color);
            mField.set(picker, colorDrawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTimerDialogCallback(TimerDialogCallback timerDialogCallback){
        this.timerDialogCallback = timerDialogCallback;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.check_breath) {
                if(isChecked){
                    checkBreathReady.setEnabled(true);
                    pickerBreathTime.setEnabled(true);
                    seekBreathPercent.setEnabled(true);
                }else{
                    checkBreathReady.setChecked(false);
                    checkBreathReady.setEnabled(false);
                    pickerBreathTime.setEnabled(false);
                    seekBreathPercent.setEnabled(false);
                    editBreathStartPercent.setEnabled(false);
                    editBreathIncreasePercent.setEnabled(false);
                    spinnerBreathTime.setEnabled(false);
                }
        } else if (id == R.id.check_breath_ready) {
                if(isChecked){
                    //호흡점차시간 활성화 하기
                    editBreathStartPercent.setEnabled(true);
                    editBreathIncreasePercent.setEnabled(true);
                    spinnerBreathTime.setEnabled(true);
                }else{
                    //호흡점차시간 비활성화 하기
                    editBreathStartPercent.setEnabled(false);
                    editBreathIncreasePercent.setEnabled(false);
                    spinnerBreathTime.setEnabled(false);
                }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //String str = (String) spinner.getSelectedItem();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    interface TimerDialogCallback{
        void timeSet(int hour, int min, int sec);
        void breathSet(int sec, boolean bellCheck, boolean breathCheck, boolean breathReadyCheck, int percent, int startPercent, int increasePercent, int num);
    }

    int keyboardThreshold;
    boolean mIsKeyboardVisible;
    ViewTreeObserver.OnGlobalLayoutListener mGlobalListener;
    private void registerView(final View rootView) {

        if (mGlobalListener == null) {
            mGlobalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    // 해당 루트뷰에서 윈도우가 보이는 영역을 얻어옴
                    rootView.getWindowVisibleDisplayFrame(r);

                    // 루트뷰의 실제 높이와, 윈도우 영역의 높이를 비교
                    // 키보드는 윈도우 영역에 위치하므로 뷰와 윈도우의 높이비교를 통해 키보드의 여부를 알 수 있다.
                    int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);

                    // keyboardThreshold는 윈도우가 기본적으로 차지하고있는 영역(StatusBar / Soft Back Button)
                    int keybordHeight = heightDiff - keyboardThreshold;
                    if (heightDiff > keyboardThreshold) {
                        if (!mIsKeyboardVisible) {
                            mIsKeyboardVisible = true;
                            //TODO::Keyboard invisible -> visible
                            Log.d(TAG,"key visible");
//                            layoutHeader.setVisibility(View.GONE);
                        }
                    } else {
                        if (mIsKeyboardVisible) {
                            mIsKeyboardVisible = false;
                            //TODO::Keyboard visible -> invisible
                            Log.d(TAG,"key invisible");
                            layoutTimer.setVisibility(View.VISIBLE);
                            editBreathStartPercent.clearFocus();
                            editBreathIncreasePercent.clearFocus();
                        }
                    }
                }
            };
        }
        // ViewTreeObserver에 리스너 등록
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalListener);
    }

    public static String getTimerString(int time){
        StringBuffer timeText = new StringBuffer();
        int modTime = time;
        int mod;
        mod = (modTime % 3600);
        modTime = (modTime / 3600);
        timeText.append(String.format("%02d", modTime));
        timeText.append(":");
        modTime = mod;
        modTime = modTime / 60;
        mod = mod % 60;
        timeText.append(String.format("%02d", modTime));
        timeText.append(":");
        timeText.append(String.format("%02d", mod));
        return timeText.toString();
    }

}
