package com.example.posting.album;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.posting.MainActivity;
import com.example.posting.R;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.posting.album.PhotoActivity.sSelectList;

public class ImageSelectActivity extends Activity{
    @BindView(R.id.list)     protected RecyclerView mListGV;
    @BindView(R.id.nowCount) protected TextView mNowCntTV;
    @BindView(R.id.maxCount) protected TextView mMaxCntTV;

    // Variable
    private int mMaxCnt = 0;
    private int mNowCnt = 0;
    private ArrayList<ImageVO> mPhotoList;
    private PhotoAdapter mPhotoAdapter;
    private LayoutInflater mInflater;

    private ArrayList<String> mTempSelectList;

    private void photoAddCheck(String path){
        if(path.endsWith(".gif")){
            File gifFile = new File(path);
            long size = gifFile.length()/1024;
            if(size >= 1024){
                long mb = size/1024;
                if(mb > 10){
                    Toast.makeText(getApplicationContext(), "gif file size is big\nyou can uplioad max 10mb", Toast.LENGTH_LONG).show();
                    return;
                }
            }else{
            }
        }
        if(mNowCnt < mMaxCnt){
            mTempSelectList.add(path);
        }
    }

    private void countTextChange(){
        mNowCnt = sSelectList.size();
        mNowCntTV.setText(Integer.toString(mNowCnt));
        mMaxCntTV.setText(Integer.toString(mMaxCnt));
    }

    public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int mSize = 0;

        public PhotoAdapter() {
            mSize = (((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth()) / 3;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(mInflater == null) mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = mInflater.inflate(R.layout.row_photo, null);
            RecyclerView.ViewHolder vh = new PhotoAdapter.PhotoHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder getHolder, int position) {
            PhotoHolder holder = (PhotoHolder) getHolder;
            String path = mPhotoList.get(position).path;
            int id = mPhotoList.get(position).id;

            if(mTempSelectList.size() > 0){
                int count = 1;
                for(String tempPath : mTempSelectList){
                    if(tempPath.equals(path)){
                        holder.mDimLayout.setVisibility(View.VISIBLE);
                        if(mMaxCnt > 1){
                            holder.mCountTV.setText(Integer.toString(count));
                        }
                        break;
                    }else{
                        holder.mDimLayout.setVisibility(View.GONE);
                    }
                    count++;
                }
            }else{
                holder.mDimLayout.setVisibility(View.GONE);
            }

            if(holder.mPath == null || !holder.mPath.equals(path)){
                holder.mPath = path;
                Glide.with(getApplicationContext())
                        .load("file://" + path)
                        .into(holder.mImageView);
            }

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mSize, mSize);
            holder.mImageView.setLayoutParams(params);
            holder.mDimLayout.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return mPhotoList.size();
        }

        public class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.image) public ImageView      mImageView;
            @BindView(R.id.dim)   public RelativeLayout mDimLayout;
            @BindView(R.id.count)   public TextView mCountTV;
            public String mPath;
            public PhotoHolder(View itemView){
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                int size = mTempSelectList.size();
                if(size > 0){
                    for(int i=0; i<size; i++){
                        if(mTempSelectList.get(i).equals(mPhotoList.get(position).path)){
                            mTempSelectList.remove(i);
                            break;
                        }else if(i == (size-1)){
                            photoAddCheck(mPhotoList.get(position).path);
                        }
                    }
                }else{
                    photoAddCheck(mPhotoList.get(position).path);
                }
                mPhotoAdapter.notifyDataSetChanged();
                countTextChange();
                if(mMaxCnt == 1){
                    setResult(101);
                    finish();
                }
            }
        }

    }


    protected Cursor mCursor;
    protected class PhotoLoadThread extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String[] columns = { MediaStore.Images.Media.DATA , MediaStore.Images.Media._ID};
            String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            mCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, "bucket_display_name='"+params[0]+"'", null, orderBy + " DESC");
            while(mCursor.moveToNext()){
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                int id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
                mPhotoList.add(new ImageVO(id, path, false));
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(mPhotoList.size() > 0){
                mPhotoAdapter = new PhotoAdapter();
                mListGV.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
                mListGV.setAdapter(mPhotoAdapter);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        ButterKnife.bind(this);
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        Intent intent = getIntent();
        mTempSelectList = sSelectList;
        if(intent != null){
            String folderName = intent.getStringExtra("folderName");
            mMaxCnt = intent.getIntExtra("maxCount", 0);
            mNowCnt = intent.getIntExtra("nowCount", 0);
            countTextChange();
            mPhotoList = new ArrayList<>();
            new PhotoLoadThread().execute(folderName);
        }
    }

    @OnClick(R.id.ok)
    protected void ok(){
        if(sSelectList.size() == 0){
            Toast.makeText(getApplicationContext(), "Plz Select Photo", Toast.LENGTH_LONG).show();
        }else{
            setResult(200);
            finish();
        }
    }

    @OnClick(R.id.back)
    protected void back(){
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
