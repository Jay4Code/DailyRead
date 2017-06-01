package com.lga.dailyread.bean;

/**
 * Created by Jay on 2017/6/1.
 */

public class Date {

    public String curr;
    public String prev;
    public String next;

    public Date() {}

    @Override
    public String toString() {
        return "Date{" +
                "curr='" + curr + '\'' +
                ", prev='" + prev + '\'' +
                ", next='" + next + '\'' +
                '}';
    }
}
