package com.movieDB.RHerreraMovieDB.ML;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Lenguaje {

    @JsonProperty("iso_639_1")
    private String iso_639_1;

    @JsonProperty("english_name")
    private String english_name;

    @JsonProperty("name")
    private String name;

    // Constructor vacío requerido por Jackson
    public Lenguaje() {
    }

    /* --- getters & setters --- */
    public String getIso_639_1() {
        return iso_639_1;
    }

    public void setIso_639_1(String iso) {
        this.iso_639_1 = iso;
    }

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String en) {
        this.english_name = en;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }
}
