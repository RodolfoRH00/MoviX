package com.movieDB.RHerreraMovieDB.ML;

import java.util.List;

public class ListaPelicula {

    private int Page;
    private List<Pelicula> Results;

    public int getPage() {
        return Page;
    }

    public void setPage(int Page) {
        this.Page = Page;
    }

    public List<Pelicula> getResults() {
        return Results;
    }

    public void setResults(List<Pelicula> results) {
        this.Results = results;
    }

}
