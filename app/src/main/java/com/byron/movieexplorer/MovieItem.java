package com.byron.movieexplorer;

public class MovieItem {
    String title;
    String date;
    String description;
    String link;

    public MovieItem(String title, String date, String description, String link) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }
}
