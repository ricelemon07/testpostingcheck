package com.example.posting;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.example.posting.album.ImageSettings;
import com.example.posting.album.ItemTouchHelperAdapter;
import com.example.posting.album.PhotoActivity;
import com.example.posting.album.SimpleItemTouchHelperCallback;
import com.example.posting.album.WriteVO;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.listView) protected RecyclerView mListRV;

    private final int VIEW_TEXT = 0;
    private final int VIEW_IMAGE = 1;
    private LinearLayoutManager mLayoutManager;
    private ListAdapter mAdapter;
    private ArrayList<WriteVO> mDataList = new ArrayList<>();
    private LayoutInflater mInflater;

    ItemTouchHelperAdapter mTouchAdapter = new ItemTouchHelperAdapter() {
        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mDataList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mDataList, i, i - 1);
                }
            }
            mAdapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDismiss(int position) {
            mDataList.remove(position);
            mAdapter.notifyItemRemoved(position);
        }
    };


    private void init(){
        mDataList.add(0, new WriteVO(0, VIEW_TEXT, ""));
        if(mLayoutManager == null && mListRV != null){
            Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_LONG).show();
            mLayoutManager = new LinearLayoutManager(getApplicationContext());
            mLayoutManager.setOrientation(RecyclerView.VERTICAL);
            mListRV.setLayoutManager(mLayoutManager);
            mAdapter = new ListAdapter();
            ((SimpleItemAnimator) mListRV.getItemAnimator()).setSupportsChangeAnimations(false);
            mListRV.setAdapter(mAdapter);

            ItemTouchHelper.Callback callback =
                    new SimpleItemTouchHelperCallback(mTouchAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mListRV);
            mListRV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            mListRV.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    return false;
                }

                @Override
                public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                }
            });
        }
    }
    public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            return mDataList.get(position).type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(mInflater == null) mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = null;
            RecyclerView.ViewHolder vh = null;
            if(viewType == VIEW_TEXT) {
                v = mInflater.inflate(R.layout.row_edit, null);
                vh = new ListAdapter.TextHolder(v);
            }else {
                v = mInflater.inflate(R.layout.row_image, null);
                vh = new ListAdapter.ImageHolder(v);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder getHolder, int position) {
            if(getHolder instanceof ListAdapter.TextHolder){
                final ListAdapter.TextHolder holder = (ListAdapter.TextHolder) getHolder;
                holder.editET.setText(mDataList.get(position).text);
                if(mDataList.get(position).focus){
                    mDataList.get(position).focus = false;
                    holder.editET.post(new Runnable() {
                        @Override
                        public void run() {
                            if (holder.editET.requestFocus()) {
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                                InputMethodManager inputMethodManager = (InputMethodManager) holder.editET.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.showSoftInput(holder.editET, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                    });
                }
            }else{
                ListAdapter.ImageHolder holder = (ListAdapter.ImageHolder) getHolder;
                Glide.with(getApplicationContext())
                        .load(mDataList.get(position).text)
                        .into(holder.imageIV);
            }
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        protected class TextHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.edit)   public EditText editET;
            @BindView(R.id.top)    public View topLayout;
            @BindView(R.id.bottom) public View bottomLayout;

            public TextHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                editET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mDataList.get(getPosition()).text = editET.getText().toString();
                    }
                    @Override
                    public void afterTextChanged(Editable s) { }
                });
            }
        }

        public class ImageHolder extends RecyclerView.ViewHolder{
            @BindView(R.id.image)  public ImageView imageIV;
            @BindView(R.id.top)    public View topLayout;
            @BindView(R.id.bottom) public View bottomLayout;
            public ImageHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                imageIV.getLayoutParams().width = ImageSettings.getDisplayWidth(getApplicationContext()) / 3;
                topLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = getPosition();
                        if(position != 0){
                            if(mDataList.get(position-1).type == VIEW_IMAGE){
                                mDataList.add(position, new WriteVO(0, VIEW_TEXT, ""));
                                mAdapter.notifyItemInserted(position);
                            }
                        }
                    }
                });
                bottomLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = getPosition();
                        if(position != 0){
                            if(position == mDataList.size()-1){
                                mDataList.add(position+1, new WriteVO(0, VIEW_TEXT, ""));
                                mAdapter.notifyItemInserted(position+1);
                            }else{
                                if(mDataList.get(position+1).type == VIEW_IMAGE){
                                    mDataList.add(position+1, new WriteVO(0, VIEW_TEXT, ""));
                                    mAdapter.notifyItemInserted(position+1);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);
        ButterKnife.bind(this);
        ImageSettings.initImageSettings(getApplicationContext());
        ImageSettings.setPhotoMinCount(1);
        ImageSettings.setPhotoMaxCount(3);
        init();
    }

    private void getAlbum(){
        int count = 0;
        for(WriteVO vo : mDataList){
            if(vo.type == VIEW_IMAGE){
                count++;
            }
        }
        if(count < ImageSettings.PHOTO_MAX_COUNT){
            File tempFile = new File(ImageSettings.PHOTO_DIR);
            if(!tempFile.exists()){
                tempFile.mkdirs();
            }
            Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
            intent.putExtra("max", ImageSettings.PHOTO_MAX_COUNT-count);
            ImageSettings.setKeyboardHide(getApplicationContext(), this.getCurrentFocus());
            startActivityForResult(intent, ImageSettings.REQUEST_PHOTO);
        }else{
            Toast.makeText(getApplicationContext(), "Photo Max Count : " + ImageSettings.PHOTO_MAX_COUNT, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = data;
        if(intent != null){
            if(PhotoActivity.sSelectList != null && PhotoActivity.sSelectList.size() > 0){
                for(String photo : PhotoActivity.sSelectList){
                    String path = Uri.fromFile(new File(photo)).toString();
                    if(path.contains("%")){
                        try {
                            path = URLDecoder.decode(path, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    mDataList.add(new WriteVO(0, VIEW_IMAGE, path));
                    mAdapter.notifyItemInserted(mDataList.size()-1);
                }
            }
        }
    }

    private void addEditText(){
        mDataList.add(new WriteVO(0, VIEW_TEXT, ""));
        mAdapter.notifyItemInserted(mDataList.size()-1);
        mListRV.post(new Runnable() {
            @Override
            public void run() {
                mListRV.smoothScrollToPosition(mDataList.size()-1);
                try{
                    EditText et = (EditText) mListRV.getChildAt(mListRV.getChildCount()-1).findViewById(R.id.edit);
                    et.requestFocus();
                    et.setCursorVisible(true);
                    InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mImm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }catch(NullPointerException e){

                }
            }
        });
    }

    @OnClick({R.id.photo, R.id.screenLayout, R.id.ok})
    protected void onClick(View v){
        switch (v.getId()){
            case R.id.photo:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                100);
                    } else {
                        getAlbum();
                    }
                } else {
                    getAlbum();
                }
                break;
            case R.id.screenLayout:
                if(mDataList.size() == 1){
                    try{
                        EditText et = (EditText) mListRV.getChildAt(mListRV.getChildCount()-1).findViewById(R.id.edit);
                        et.requestFocus();
                        et.setCursorVisible(true);
                        InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        mImm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }catch(NullPointerException e){

                    }
                    if(mDataList.get(0).type == VIEW_IMAGE){
                        addEditText();
                    }
                }else{
                    if(mDataList.size() == 0){
                        addEditText();
                    }else{
                        if(mDataList.get(mDataList.size()-1).type == VIEW_TEXT){

                        }else{
                            mDataList.add(new WriteVO(0, VIEW_TEXT, ""));
                            mAdapter.notifyItemInserted(mDataList.size()-1);
                            mListRV.post(new Runnable() {
                                @Override
                                public void run() {
                                    mListRV.smoothScrollToPosition(mDataList.size()-1);
                                    try{
                                        EditText et = (EditText) mListRV.getChildAt(mListRV.getChildCount()-1).findViewById(R.id.edit);
                                        et.requestFocus();
                                        et.setCursorVisible(true);
                                        InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        mImm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                    }catch(NullPointerException e){

                                    }
                                }
                            });
                        }
                    }
                }
                break;
            case R.id.ok:
                int count = 0;
                for(WriteVO vo : mDataList){
                    if(vo.type == VIEW_IMAGE) count++;
                }
                if (count < ImageSettings.PHOTO_MIN_COUNT) {
                    Toast.makeText(getApplicationContext(), "you need choice minimum " + ImageSettings.PHOTO_MIN_COUNT + " photos", Toast.LENGTH_LONG).show();
                } else {
                    StringBuilder sb = new StringBuilder();
                    for(WriteVO vo : mDataList){
                        sb.append(vo.text + "\n");
                    }
                    Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
