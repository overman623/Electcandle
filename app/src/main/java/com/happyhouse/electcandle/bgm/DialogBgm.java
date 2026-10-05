package com.happyhouse.electcandle.bgm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.happyhouse.electcandle.R;

import java.util.ArrayList;
import java.util.Set;

import static com.happyhouse.electcandle.MainActivity.PACKAGE_NAME;
import static com.happyhouse.electcandle.MainActivity.displayX;
import static com.happyhouse.electcandle.MainActivity.displayY;


public class DialogBgm extends AlertDialog {

    public String nowMusic;

    public static final String TAG = "DialogBgm";
    private int layoutResource;
    private BgmDialogCallback bgmDialogCallback;

    private Button getBgm;
    private Button saveBgm;
    private Button back;
    private ListView bgmList;
    private Set<String> bgmSet;
    private Context context;
    public TextView nowBgmText;


    private int nowMusicIndex;

    public DialogBgm(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
    }


    public DialogBgm(Context context,  String nowMusic) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.context = context;
        this.nowMusic = nowMusic;

    }


    protected DialogBgm(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setLayoutResource(int layoutResource){
        this.layoutResource = layoutResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(this.layoutResource);
        setLayout();
    }

    BgmListAdapter bgmListAdapter;

    private void setLayout(){

        nowBgmText = findViewById(R.id.now_bgm_text);

        back = findViewById(R.id.btn_back_arrow);
        back.setBackgroundResource(R.drawable.ic_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        saveBgm = findViewById(R.id.bgm_set);
        saveBgm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bgmDialogCallback.bgmSet(nowMusic);
                cancel();
            }
        });
        getBgm = findViewById(R.id.bgm_inner_storage);
        bgmList = findViewById(R.id.bgm_list);

        //파일 가져오고 // 음악 선택하고
        //내부 파일 가져와야한다.
        bgmListItemArrayList.add(new BgmListItem(false, "NONE", R.raw.empty));
        bgmListItemArrayList.add(new BgmListItem(false, "Sound01", R.raw.music1));
        bgmListItemArrayList.add(new BgmListItem(false, "Sound02", R.raw.music2));
        bgmListItemArrayList.add(new BgmListItem(false, "Sound03", R.raw.music3));
        bgmListItemArrayList.add(new BgmListItem(false, "Sound04", R.raw.music4));
        bgmListItemArrayList.add(new BgmListItem(false, "Sound05", R.raw.music5));

        if(nowMusic.contains("android.resource://")){
            switch (nowMusic.substring(nowMusic.lastIndexOf("/") + 1)){
                case "empty":
                    nowBgmText.setText("BGM : none");
                    break;
                case "music1":
                    nowBgmText.setText("BGM : Sound01");
                    break;
                case "music2":
                    nowBgmText.setText("BGM : Sound02");
                    break;
                case "music3":
                    nowBgmText.setText("BGM : Sound03");
                    break;
                case "music4":
                    nowBgmText.setText("BGM : Sound04");
                    break;
                case "music5":
                    nowBgmText.setText("BGM : Sound05");
                    break;

            }
        }else{
            nowBgmText.setText("BGM : " + nowMusic.substring(nowMusic.lastIndexOf("/") + 1));
        }

        bgmListAdapter = new BgmListAdapter(bgmListItemArrayList);
        bgmList.setAdapter(bgmListAdapter);

    }

    public void setNowMusic(String nowMusic) {
        this.nowMusic = nowMusic;
    }

    public void setGetBgmOnClickListener(View.OnClickListener listener){
        getBgm.setOnClickListener(listener);
    }

    public void setBgmDialogCallback(BgmDialogCallback bgmDialogCallback){
        this.bgmDialogCallback = bgmDialogCallback;
    }

    public interface BgmDialogCallback{
        void bgmSet(String musicString);
    }
    private ArrayList<BgmListItem> bgmListItemArrayList = new ArrayList<>();

    class BgmListAdapter extends BaseAdapter{

        private ArrayList<BgmListItem> bgmListItemArrayList;
        private Set<String> set = null;

        public BgmListAdapter() {
        }

        public BgmListAdapter(ArrayList<BgmListItem> bgmListItemArrayList) {
            this.bgmListItemArrayList = bgmListItemArrayList;
        }

        public BgmListAdapter(Set<String> set) {
            this.set = set;

            /*
            if(set != null){
                HashSet<String> bgmSet = (HashSet<String>) this.set;
                //그동안 불러왔던 bgm을 불러올수 있음.
                //이걸 불러와서 추가로 listview에 add하도록 한다.
                Iterator<String> iter = bgmSet.iterator();
                while(iter.hasNext()){
                    //"title 부분에는 파일 이름을 넣어야함."
                    this.bgmListItemArrayList.add(new BgmListItem(true, "No BGM", R.raw.empty));
                }
            }
            */
        }

        @Override
        public int getCount() {
            return bgmListItemArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return (bgmListItemArrayList.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(BgmListItem item){
            bgmListItemArrayList.add(item);
        }

        public void deleteItemId(int position){
            bgmListItemArrayList.remove(position);
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            BgmListItem item = (BgmListItem)getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_list_item_bgm, null);
                holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.bgm_title);
                holder.bgmButton = convertView.findViewById(R.id.bgm_button);
                holder.bgmButton.setMinimumHeight(displayY / 10);
                holder.bgmDelete = convertView.findViewById(R.id.bgm_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(((BgmListItem) getItem(position)).getTitle());
            holder.bgmButton.setOnClickListener(new MyOnClickListener(position, convertView));

            if(item.isStorage()){
                holder.bgmDelete.setVisibility(View.VISIBLE);
                holder.bgmDelete.setBackgroundResource(R.drawable.ic_delete);
            }

            return convertView;
        }

        private int beforeSelectedPosition = 0;
        private View beforeSelectedView;
        private int selectedPosition = 0;
        class MyOnClickListener implements View.OnClickListener{

            private int position;
            private View selectedView;

            public MyOnClickListener(int position, View view) {
                this.position = position;
                this.selectedView = view;
            }

            @Override
            public void onClick(View v) {
                TextView title = null;
                Button bgmDelete = null;

                int viewId = v.getId();
                if (viewId == R.id.bgm_button) {
                        //클릭시 배경 on/off
                        if(beforeSelectedView != null){
                            BgmListItem item = ((BgmListItem)getItem(beforeSelectedPosition));
                            if(item.isStorage()){
                                bgmDelete = beforeSelectedView.findViewById(R.id.bgm_delete);
                                bgmDelete.setBackgroundResource(R.drawable.ic_delete);
                            }
                            beforeSelectedView.setBackgroundColor(Color.BLACK);
                            title = beforeSelectedView.findViewById(R.id.bgm_title);
                            title.setTextColor(Color.WHITE); //전에 클릭한건 원상태로 돌아가고
                        }

                        BgmListItem item2 = ((BgmListItem)getItem(position));
                        if(item2.isStorage()){
                            bgmDelete = selectedView.findViewById(R.id.bgm_delete);
                            bgmDelete.setBackgroundResource(R.drawable.ic_delete_click);
                        }

                        selectedView.setBackgroundColor(Color.WHITE);
                        title = selectedView.findViewById(R.id.bgm_title);
                        title.setTextColor(Color.BLACK); //클릭한 리스트 뷰는 색상을 바꾼다.
                        nowBgmText.setText("BGM : " + title.getText());
                        beforeSelectedPosition = position;
                        beforeSelectedView = selectedView;
                        nowMusicIndex = position; //여기는 원래 상태로 돌려놓음(클릭 후 상태로)
                        setNowMusic("android.resource://" + PACKAGE_NAME +"/raw/" + getMusicTitle(item2.getResource()));
                        //selectPosition을 기반으로 배경음악을 선택하는 것이다.
                } else if (viewId == R.id.bgm_delete) {
                        //해당하는 목록이 없어짐. 기본 목록은 이 버튼이 존재하지 않음.
                        deleteItemId(position);
                }
            }
        }

        public String getMusicTitle(int resource){
            if (resource == R.raw.empty) {
                    nowBgmText.setText("BGM : NONE");
                    return "empty";
            } else if (resource == R.raw.music1) {
                    nowBgmText.setText("BGM : Sound01");
                    return "music1";
            } else if (resource == R.raw.music2) {
                    nowBgmText.setText("BGM : Sound02");
                    return "music2";
            } else if (resource == R.raw.music3) {
                    nowBgmText.setText("BGM : Sound03");
                    return "music3";
            } else if (resource == R.raw.music4) {
                    nowBgmText.setText("BGM : Sound04");
                    return "music4";
            } else if (resource == R.raw.music5) {
                    nowBgmText.setText("BGM : Sound05");
                    return "music5";
            }
            return "empty";
        }

        class ViewHolder{
            TextView title = null;
            Button bgmButton = null;
            Button bgmDelete = null;
        }

    }



}
