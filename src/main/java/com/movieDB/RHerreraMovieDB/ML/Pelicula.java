package com.movieDB.RHerreraMovieDB.ML;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pelicula {

    @JsonProperty("title")
    private String Title;

    @JsonProperty("overview")
    private String Overview;

    @JsonProperty("release_date")
    private String Release_Date;

    @JsonProperty("vote_average")
    private double Vote_Average;

    @JsonProperty("poster_path")
    private String Poster_Path;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getOverview() {
        return Overview;
    }

    public void setOverview(String Overview) {
        this.Overview = Overview;
    }

    public String getRelease_Date() {
        return Release_Date;
    }

    public void setRelease_Date(String Release_Date) {
        this.Release_Date = Release_Date;
    }

    public double getVote_Average() {
        return Vote_Average;
    }

    public void setVote_Average(double Vote_Average) {
        this.Vote_Average = Vote_Average;
    }

    public String getPoster_Path() {
        return Poster_Path;
    }

    public void setPoster_Path(String Poster_Path) {
        this.Poster_Path = Poster_Path;
    }

}
