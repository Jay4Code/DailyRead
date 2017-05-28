package com.lga.dailyread;

/**
 * Created by Jay on 2017/5/24.
 */

public class Article {

    public String curr;
    public String prev;
    public String next;
    public String author;
    public String title;
    public String digest;
    public String content;
    public int wc;
    public int readPosition;

    public Article() {
    }

    public String getFormatedContent() {
        String tmp = this.content.substring(0, this.content.length() - 4);
        tmp = tmp.replace("<p>", "\u3000\u3000");
        tmp = tmp.replaceAll("</p>", "\n");
        return tmp;
    }

    @Override
    public String toString() {
        return "Article{" +
                "curr='" + curr + '\'' +
                ", prev='" + prev + '\'' +
                ", next='" + next + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", digest='" + digest + '\'' +
                ", content='" + content + '\'' +
                ", wc=" + wc +
                '}';
    }
}
