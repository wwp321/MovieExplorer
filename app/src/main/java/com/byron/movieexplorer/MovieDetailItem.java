package com.byron.movieexplorer;

public class MovieDetailItem {
    String title;
    String thumbnailLink;
    String titleDetail;
    String downloadLink;
    String capture;

    public MovieDetailItem(String title, String thumbnailLink, String titleDetail, String downloadLink, String capture) {
        this.title = title;
        this.thumbnailLink = thumbnailLink;
        this.titleDetail = titleDetail;
        this.downloadLink = downloadLink;
        this.capture = capture;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    public String getTitleDetail() {
        return titleDetail;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getCapture() {
        return capture;
    }
}
