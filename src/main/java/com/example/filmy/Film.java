package com.example.filmy;

public class Film {
    private int id;
    private String title;
    private String genre;
    private String actors;
    private boolean watched;
    private int rating;

    public Film(String title, String genre, String actors) {
        this.title = title;
        this.genre = genre;
        this.actors = actors;
        this.watched = false;
        this.rating = 0;
    }


    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getActors() { return actors; }
    public boolean isWatched() { return watched; }
    public int getRating() { return rating; }
    public String getRatingText() {
        return rating > 0 ? rating + "/10" : "Brak";
    }


    public void setId(int id) { this.id = id; }
    public void setWatched(boolean watched) { this.watched = watched; }
    public void setRating(int rating) { this.rating = rating; }
}