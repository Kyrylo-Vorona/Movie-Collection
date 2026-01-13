package dk.easv.moviecollection.be;

import java.time.LocalDate;

public class Movie {
    private int id;
    private String name;
    private float imdbRating;
    private int personalRating;
    private String filelink;
    private LocalDate lastview;

    public Movie(int  id, String name, float imdbRating, int personalRating, String filelink, LocalDate lastview) {
        this.id = id;
        this.name = name;
        this.imdbRating = imdbRating;
        this.personalRating = personalRating;
        this.filelink = filelink;
        this.lastview = lastview;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public float getImdbRating() {
        return imdbRating;
    }
    public int getPersonalRating() {
        return personalRating;
    }
    public String getFilelink() {
        return filelink;
    }
    public LocalDate getLastview() {
        return lastview;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setImdbRating(float imdbRating) {
        this.imdbRating = imdbRating;
    }
    public void setPersonalRating(int personalRating) {
        this.personalRating = personalRating;
    }
    public void setFilelink(String filelink) {
        this.filelink = filelink;
    }
    public void setLastview(LocalDate lastview) {
        this.lastview = lastview;
    }
    @Override
    public String toString() {
        return name;
    }
}
