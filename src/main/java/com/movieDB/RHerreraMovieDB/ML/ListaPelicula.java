package com.movieDB.RHerreraMovieDB.ML;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ListaPelicula {

    @JsonProperty("results")        // ←  nombre exacto del campo TMDb
    private List<Pelicula> results;

    public List<Pelicula> getResults() {
        return results;
    }

    public void setResults(List<Pelicula> results) {
        this.results = results;
    }
}
