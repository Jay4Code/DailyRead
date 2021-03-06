package com.lga.dailyread.bean;

/**
 * Created by Jay on 2017/5/24.
 */
public class Article {

    public Data data;

    public Article() {
        data = new Data();
    }

    public String getCurr() {
        return data.date.curr;
    }

    public void setCurr(String curr) {
        data.date.curr = curr;
    }

    public String getPrev() {
        return data.date.prev;
    }

    public void setPrev(String prev) {
        data.date.prev = prev;
    }

    public String getNext() {
        return data.date.next;
    }

    public void setNext(String next) {
        data.date.next = next;
    }

    public String getAuthro() {
        return data.author;
    }

    public void setAuthor(String author) {
        data.author = author;
    }

    public String getTitle() {
        return data.title;
    }

    public void setTitle(String title) {
        data.title = title;
    }

    public String getDigest() {
        return data.digest;
    }

    public void setDigest(String digest) {
        data.digest = digest;
    }

    public String getContent() {
        return data.content;
    }

    public void setContent(String content) {
        data.content = content;
    }

    public int getWc() {
        return data.wc;
    }

    public void setWc(int wc) {
        data.wc = wc;
    }

    public int getReadPosition() {
        return data.readPosition;
    }

    public void setReadPosition(int readPosition) {
        data.readPosition = readPosition;
    }

    public String getFormatedContent() {
        String content = getContent();
        String tmp = content.substring(0, content.length() - 4);
        tmp = tmp.replace("<p>", "\u3000\u3000");
        tmp = tmp.replaceAll("</p>", "\n");
        return tmp;
    }

    @Override
    public String toString() {
        return "Article{" +
                "data=" + data +
                '}';
    }
}