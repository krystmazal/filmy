package com.example.filmy;

public class Film {
    private int id;
    private String title;
    private String genre;
    private String actors;
    private boolean watched;
    private int actorsRating;
    private int plotRating;
    private int sceneryRating;
    private double averageRating;

    public Film(String title, String genre, String actors) {
        this.title = title;
        this.genre = genre;
        this.actors = actors;
        this.watched = false;
        this.actorsRating = 0;
        this.plotRating = 0;
        this.sceneryRating = 0;
        this.averageRating = 0.0;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getActors() { return actors; }
    public boolean isWatched() { return watched; }
    public int getActorsRating() { return actorsRating; }
    public int getPlotRating() { return plotRating; }
    public int getSceneryRating() { return sceneryRating; }
    public double getAverageRating() { return averageRating; }

    public String getRatingText() {
        return averageRating > 0 ? String.format("%.1f/10", averageRating) : "Brak";
    }


    public void setId(int id) { this.id = id; }
    public void setWatched(boolean watched) { this.watched = watched; }
    public void setActorsRating(int actorsRating) {
        this.actorsRating = actorsRating;
        calculateAverageRating();
    }
    public void setPlotRating(int plotRating) {
        this.plotRating = plotRating;
        calculateAverageRating();
    }
    public void setSceneryRating(int sceneryRating) {
        this.sceneryRating = sceneryRating;
        calculateAverageRating();
    }

    private void calculateAverageRating() {
        int count = 0;
        int sum = 0;

        if (actorsRating > 0) { sum += actorsRating; count++; }
        if (plotRating > 0) { sum += plotRating; count++; }
        if (sceneryRating > 0) { sum += sceneryRating; count++; }

        this.averageRating = count > 0 ? (double) sum / count : 0.0;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}