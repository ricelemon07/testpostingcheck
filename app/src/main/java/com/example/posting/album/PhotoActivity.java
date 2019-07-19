package com.example.posting.album;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.posting.R;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoActivity extends Activity{
    @BindView(R.id.list)     protected RecyclerView mListRV;
    @BindView(R.id.nowCount) protected TextView     mNowCntTV;
    @BindView(R.id.maxCount) protected TextView     mMaxCntTV;

    public static ArrayList<String> sSelectList;
    private LayoutInflater mInflater;
    private ListAdapter mFolderAdapter;
    private int mNowCnt = 0;
    private int mMaxCnt = 1;

    private ProgressDialog mPd;

    private String mMemberId = "";
    private String mMethod = "";

    private void countTextChange(){
        if(sSelectList != null) mNowCnt = sSelectList.size();
        mNowCntTV.setText(Integer.toString(mNowCnt));
        mMaxCntTV.setText(Integer.toString(mMaxCnt));
    }

    private ArrayList<ImageVO> mFolderList;
    protected Cursor mCursor;

    protected class GetImageFolderThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFolderList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Cursor folderCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , new String[] {"DISTINCT " + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA}
                    , MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " IS NOT NULL" + ") GROUP BY (" + MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, null, MediaStore.Images.Media.DATE_ADDED + " desc");
            String[] columns = { MediaStore.Images.Media.DATA , MediaStore.Images.Media._ID};
            String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            while(folderCursor.moveToNext()){
                ImageVO vo = new ImageVO();
                String folderName = folderCursor.getString(folderCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                vo.folderName = folderName;
                mCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, "bucket_display_name='"+folderName+"'", null, orderBy + " DESC");
                int count = 0;
                while(mCursor.moveToNext()){
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    int id = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
                    if(count == 0){
                        String thumbProj[] = { MediaStore.Images.Thumbnails.DATA };
                        Cursor thumbCursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, thumbProj);
                        if(thumbCursor != null && thumbCursor.moveToFirst()){
                            path = thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                        }
                        thumbCursor.close();
                        vo.path = path;
                    }
                    count++;
                }
                vo.size = count;
                mFolderList.add(vo);
            }
            folderCursor.close();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mPd.dismiss();
            initListView();
        }
    }


    private void initListView(){
        StaggeredGridLayoutManager sgm = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        sgm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        sgm.supportsPredictiveItemAnimations();
        mFolderAdapter = new ListAdapter();
        mListRV.setLayoutManager(sgm);
        mListRV.setAdapter(mFolderAdapter);
        mListRV.setHasFixedSize(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sSelectList != null){
            countTextChange();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if(intent != null){
            mMemberId = intent.getStringExtra("memberId");
            mMethod = intent.getStringExtra("method");
            mMaxCnt = intent.getIntExtra("max", 1);
        }
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        sSelectList = new ArrayList<>();

        mPd = new ProgressDialog(PhotoActivity.this);
        mPd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPd.setMessage("Loading...");
        mPd.setCancelable(false);
        new GetImageFolderThread().execute();
    }

    protected class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        protected int mSize;

        public ListAdapter() {
            mSize = (((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth()) / 3;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(mInflater == null) mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View v = mInflater.inflate(R.layout.row_photo_folder, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ImageVO vo = mFolderList.get(position);
            String path = vo.path;
            holder.mNameTV.setText(vo.folderName + "(" + vo.size + ")");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mSize, mSize);
            holder.mImageIV.setLayoutParams(params);
            if(holder.mPath == null || !holder.mPath.equals(path)){
                holder.mPath = path;
                Glide.with(getApplicationContext())
                        .load("file:///" + path)
                        .into(holder.mImageIV);
            }
        }

        @Override
        public int getItemCount() {
            return mFolderList.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            @BindView(R.id.image) public ImageView mImageIV;
            @BindView(R.id.name)  public TextView  mNameTV;
            public String mPath;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int position = getPosition();
                startActivityForResult(new Intent(PhotoActivity.this, ImageSelectActivity.class)
                        .putExtra("folderName", mFolderList.get(position).folderName)
                        .putExtra("maxCount", mMaxCnt)
                        .putExtra("nowCount", mNowCnt)
                        , ImageSettings.REQUEST_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ImageSettings.REQUEST_PHOTO && resultCode == 101 && mMaxCnt == 1){
            Intent intent = new Intent();
            if(sSelectList.size() > 0 && mMaxCnt == 1){
                intent.putExtra("photo", sSelectList.get(0));
            }else if(sSelectList.size() > 0){

            }
            setResult(ImageSettings.REQUEST_PHOTO, intent);
            finish();
        }else if(requestCode == ImageSettings.REQUEST_PHOTO && resultCode == 200){
            ok();
        }
    }

    @OnClick(R.id.back)
    protected void back(){
        finish();
    }

    @OnClick(R.id.ok)
    protected void ok(){
        if(sSelectList.size() == 0){
            Toast.makeText(getApplicationContext(), "Plz Select Photo.", Toast.LENGTH_LONG).show();
        }else{
            Intent intent = new Intent();
            if(sSelectList.size() > 0){
                intent.putExtra("photo", sSelectList.get(0));
            }
            setResult(ImageSettings.REQUEST_PHOTO, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
