package com.example.posting.album;

public class ImageVO {
    public boolean isCheck;
    public String path;
    public String folderName;
    public int size;
    public int id;

    public ImageVO(){

    }

    public ImageVO(int id, String path, boolean isCheck) {
        this.id = id;
        this.path = path;
        this.isCheck = isCheck;
    }

}
