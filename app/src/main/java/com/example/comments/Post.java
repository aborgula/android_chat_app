package com.example.comments;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("id")
    private String id;

    @SerializedName("date")
    private String date;

    @SerializedName("login")
    private String login;

    @SerializedName("content")
    private String content;

    public Post(String id, String date, String login, String content) {
        this.id = id;
        this.date = date;
        this.login = login;
        this.content = content;
    }

    public Post() {
        this.id = "";
        this.date = "";
        this.login = "";
        this.content = "";
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setContent(String content) {
        this.content = content;
    }
}