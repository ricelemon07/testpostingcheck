package com.example.posting.album;

import java.io.Serializable;

public class WriteVO implements Serializable{
    public int no;
    public int type;
    public String text;
    public boolean focus = true;

    public WriteVO(int no, int type, String text) {
        this.no = no;
        this.type = type;
        this.text = text;
    }
}
