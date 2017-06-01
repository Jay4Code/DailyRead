package com.lga.dailyread.bean;

/**
 * Created by Jay on 2017/6/1.
 */

public class Data {

    public Date date;
    public String author;
    public String title;
    public String digest;
    public String content;
    public int wc;
    public int readPosition;

    public Data() {
        date = new Date();
    }

    @Override
    public String toString() {
        return "Data{" +
                "date=" + date +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", digest='" + digest + '\'' +
                ", content='" + content + '\'' +
                ", wc=" + wc +
                ", readPosition=" + readPosition +
                '}';
    }
}
