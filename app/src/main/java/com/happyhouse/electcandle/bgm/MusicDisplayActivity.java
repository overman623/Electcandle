package com.happyhouse.electcandle.bgm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.happyhouse.electcandle.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import static com.happyhouse.electcandle.MainActivity.FILE_STRING;

public class MusicDisplayActivity extends AppCompatActivity {

    public static final String TAG = "MusicDisplayActivity";

    private static final String MEDIA_PATH = new String("/sdcard/"); //우리가 흔히 알고 있는 root파일 절대 경로
    private ArrayList<String> songs = new ArrayList();
    private ArrayList<BgmStorageListItem> bgmStorageListItems = new ArrayList();

    private ListView listView;
    private Button selectBgm;
    private Button backButton;
    private TextView textDirection;

    private File nowDirection;
    private File nowSelectedFile;
    private int selectedPosition;

    int displayY;

    static class Mp3Filter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3")); // 확장자가 mp3인지 확인
        }
    }

    //리스트 요소에서 폴더와 파일의 구분이 필요하다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_display);

//        try {
//            super.onCreate(savedInstanceState);
//
//            updateSongList();
//        } catch (NullPointerException e) {
//            Log.e(getString(R.string.app_name), e.getMessage()); // 로그에 에러메시지 기록
//        }

        listView = findViewById(R.id.music_list);
        selectBgm = findViewById(R.id.bgm_select);//선택한 화면을 종료하고 배경화면으로 가서 자기가 선택한 음악을 알려준다. //이 창을 종료한다.
        backButton = findViewById(R.id.back_button);
        backButton.setBackgroundResource(R.drawable.ic_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "getParent : " + nowDirection.getParent());
                if(nowDirection.getParent().equals("/")){
                    onBackPressed();
                }else{
                    updateSongList(nowDirection.getParent());
                }
            }
        });
        textDirection = findViewById(R.id.text_direction);
        selectBgm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //음악이 선택되었으면. 선택된 상태로 인텐트를 종료하고 인텐트의 값을 전에 있는 엑티비티에 전해줌.
                //선택된 음악을 알리고
                //해당 엑티비티 종료
                //다이얼로그도 종료 - 취소 버튼을 강제로 누름
                //메인 엑티비티에서 현재 음악을 저장
                //다이얼로그 실행시 알려진 현재 음악으로 타이틀 지정.
                if(nowSelectedFile != null){
//                    Log.d(TAG, "select music : " + nowSelectedFile.getAbsolutePath());

                    Intent data = new Intent();
                    data.putExtra("FILE_OBJECT", nowSelectedFile.getAbsolutePath());
                    setResult(FILE_STRING, data);
                    finish();
                    //뒤로가기
                }
            }
        });
        updateSongList(MEDIA_PATH);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayY = size.y; // 2094
    }


    //폴더의 이름과 속성:디렉토리 -> 클릭시 해당 디렉토리를 경로로 삼아서 함수 실행
    //파일의 이름과 속성:파일     -> 클릭시 선택 활성화


    BgmListAdapter bgmListAdapter = null;
    public void updateSongList(String filePath) {

        //디렉토리 로직 먼저 돌림.
        //파일 로직을 돌림.

        bgmStorageListItems.clear(); //리스트 초기화
        File mainDirectory = new File(filePath); //초기 파일경로 넣고.
        nowDirection = mainDirectory;
        textDirection.setText(mainDirectory.getAbsolutePath());

        for (File directory : mainDirectory.listFiles()) {
            if (directory.isDirectory()) { //디렉토리면 디렉토리로 추가함.
                bgmStorageListItems.add(new BgmStorageListItem(directory, false));
            }
        }
        for (File file : mainDirectory.listFiles(new Mp3Filter())) {
            bgmStorageListItems.add(new BgmStorageListItem(file, true));
        }

        bgmListAdapter = new BgmListAdapter(bgmStorageListItems);
        bgmListAdapter.notifyDataSetChanged();
        listView.setAdapter(bgmListAdapter); // ArrayAdapter를 ListView에 바인딩

        //리스트 업데이트

    }


    class BgmListAdapter extends BaseAdapter {

        private ArrayList<BgmStorageListItem> fileList;
        public BgmListAdapter(ArrayList<BgmStorageListItem> fileList) {
            this.fileList = fileList;
        }

        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public Object getItem(int position) {
            return fileList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            BgmStorageListItem item = (BgmStorageListItem)getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_list_storage_item_bgm, null);
                holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.bgm_title);
                holder.imageIcon = convertView.findViewById(R.id.icon_image);
                holder.bgmButton = convertView.findViewById(R.id.bgm_button);
//                holder.bgmButton.setMinimumHeight(180);
                holder.bgmButton.setMinimumHeight(displayY / 11);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(item.getFile().getName());

            holder.bgmButton.setOnClickListener(new MyOnClickListener(position, convertView));

            if(item.isFile()){
                holder.imageIcon.setImageResource(R.drawable.ic_mp3_white); //파일일때
            }else{
                holder.imageIcon.setImageResource(R.drawable.ic_directory_white);
            }
            return convertView;
        }



        private int beforeSelectedPosition = 0;
        private View beforeSelectedView;
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
                ImageView imageIcon = null;

                BgmStorageListItem item = (BgmStorageListItem)getItem(position);
                if(item.isFile()){
                    //선택된 상태에서 레이아웃이 수정됨.
                    if(beforeSelectedView != null){
                        beforeSelectedView.setBackgroundColor(Color.BLACK);
                        title = beforeSelectedView.findViewById(R.id.bgm_title);
                        title.setTextColor(Color.WHITE); //전에 클릭한건 원상태로 돌아가고
                        imageIcon = beforeSelectedView.findViewById(R.id.icon_image);
                        imageIcon.setImageResource(R.drawable.ic_mp3_white); //파일일때
                    }

                    selectedView.setBackgroundColor(Color.WHITE);
                    title = selectedView.findViewById(R.id.bgm_title);
                    title.setTextColor(Color.BLACK); //클릭한 리스트 뷰는 색상을 바꾼다.
                    imageIcon = selectedView.findViewById(R.id.icon_image);
                    imageIcon.setImageResource(R.drawable.ic_mp3); //파일일때
                    //selectPosition을 기반으로 배경음악을 선택하는 것이다.
                    nowSelectedFile = item.getFile();
                }else{
                    //그 파일 다시 찾아서 리스트로 뿌려줌.
                    //Log.d(TAG, "dictory path : " + item.getFile().getPath());
                    updateSongList(item.getFile().getPath());
                }

                beforeSelectedPosition = position;
                beforeSelectedView = selectedView;

            }
        }

        class ViewHolder{
            TextView title = null;
            Button bgmButton = null;
            ImageView imageIcon = null;
        }
    }

}
