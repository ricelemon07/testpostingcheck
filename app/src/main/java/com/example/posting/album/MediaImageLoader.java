package com.example.posting.album;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class MediaImageLoader extends AsyncTask<Integer, Void, String> {
    private Context context;

    MediaImageLoader(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(Integer... voids) {
        String path = "";
        try{
            String thumbProj[] = { MediaStore.Images.Thumbnails.DATA };
            Cursor thumbCursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(context.getContentResolver(), voids[0], MediaStore.Images.Thumbnails.MINI_KIND, thumbProj);
            if(thumbCursor != null && thumbCursor.moveToFirst()){
                path = thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            }
            thumbCursor.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        //ImageLoader.getInstance().displayImage("file://" + path, mHolder.mImageView, mOptions);

        return path;
    }
}
