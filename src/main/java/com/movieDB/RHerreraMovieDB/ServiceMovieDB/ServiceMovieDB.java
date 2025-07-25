package com.movieDB.RHerreraMovieDB.ServiceMovieDB;

import com.movieDB.RHerreraMovieDB.ML.ListaPelicula;
import com.movieDB.RHerreraMovieDB.ML.Pelicula;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceMovieDB {

    private static final String Token = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiNzUwNjg0NmIyZjQ3OTM4YmEzYTdlMmE4ZWIyODA0ZiIsIm5iZiI6MTc1MzQ2Nzg5Mi4xNDIwMDAyLCJzdWIiOiI2ODgzY2JmNDk4YzU5N2YxMWE4YTYzOWEiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.4n982JEsK0jCNafZmri_QfJMBuHP2fJyOnNcsmTNixI";
    private static final String UrlBase = "https://api.themoviedb.org/3";

    
    public List<Pelicula> PeliculasPopulares() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ListaPelicula> response = restTemplate.exchange(
                UrlBase + "/discover/movie",
                HttpMethod.GET,
                entity,
                ListaPelicula.class
        );

        return response.getBody().getResults();
    }
}
