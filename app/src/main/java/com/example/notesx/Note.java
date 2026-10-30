package com.example.notesx;

public class Note {
    private int id;
    private String title;
    private String content;
    private String date;
    private String category;

    public Note(int id, String title, String content, String date, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.category = category;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getCategory() { return category != null ? category : "Personal"; }
}