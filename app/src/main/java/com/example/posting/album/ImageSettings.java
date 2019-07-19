package com.example.posting.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class ImageSettings {
    public static final int REQUEST_PHOTO = 101;
    public static final String PHOTO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/posting/photo";
    public static int PHOTO_MAX_COUNT = 0;
    public static int PHOTO_MIN_COUNT = 0;

    public static void initImageSettings(Context context){

    }

    public static int getDisplayWidth(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getWidth();
    }

    public static void setPhotoMinCount(int count){
        PHOTO_MIN_COUNT = count;
    }

    public static void setPhotoMaxCount(int count){
        PHOTO_MAX_COUNT = count;
    }

    public static void setKeyboardHide(Context context, View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
