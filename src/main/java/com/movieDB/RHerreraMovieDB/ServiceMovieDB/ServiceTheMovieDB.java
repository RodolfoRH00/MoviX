package com.movieDB.RHerreraMovieDB.ServiceMovieDB;

import com.movieDB.RHerreraMovieDB.ML.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@Service
public class ServiceTheMovieDB {

    @Value("${tmdb.base-url}")
    private String baseUrl;
    @Value("${tmdb.redirect}")
    private String redirectUrl;
    @Value("${tmdb.bearer-token}")
    private String bearer;
    @Value("${tmdb.default-language}")
    private String defaultLanguage;

    /* ---------- utilidades ---------- */
    private RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(bearer);
        header.setAccept(List.of(MediaType.APPLICATION_JSON));
        header.setContentType(MediaType.APPLICATION_JSON);
        return header;
    }

    /* ---------- autenticacion ---------- */
    public Token newRequestToken() {
        return restTemplate().exchange(
                baseUrl + "/authentication/token/new",
                HttpMethod.GET, new HttpEntity<>(jsonHeaders()), Token.class
        ).getBody();
    }

    public String authUrl(String requestToken) {
        String rt = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        return "https://www.themoviedb.org/authenticate/" + requestToken + "?redirect_to=" + rt;
    }

    public String createSession(String approvedToken) {
        Map<String, String> body = Map.of("request_token", approvedToken);
        Session session = restTemplate().exchange(
                baseUrl + "/authentication/session/new",
                HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), Session.class
        ).getBody();
        return session.getSession_id();
    }

    public Cuenta getAccount(String sessionId) {
        String url = baseUrl + "/account?session_id="
                + UriUtils.encode(sessionId, StandardCharsets.UTF_8);
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), Cuenta.class).getBody();
    }

    public boolean deleteSession(String sessionId) {
        Map<String, String> body = Map.of("session_id", sessionId);
        Map<?, ?> restTemplate = restTemplate().exchange(
                baseUrl + "/authentication/session",
                HttpMethod.DELETE, new HttpEntity<>(body, jsonHeaders()), Map.class
        ).getBody();
        return Boolean.TRUE.equals(restTemplate.get("success"));
    }

    public Token validateWithLogin(String user, String pass, String reqToken) {
        Map<String, String> body = Map.of(
                "username", user,
                "password", pass,
                "request_token", reqToken
        );
        return restTemplate().exchange(
                baseUrl + "/authentication/token/validate_with_login",
                HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), Token.class
        ).getBody();
    }

    /* ---------- idioma ---------- */
    public List<Lenguaje> getAvailableLanguages() {
        return restTemplate().exchange(
                baseUrl + "/configuration/languages",
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                new ParameterizedTypeReference<List<Lenguaje>>() {
        }
        ).getBody();
    }

    /* ---------- peliculas por categoria ---------- */
    private String langOrDefault(String lang) {
        return (lang != null && !lang.isBlank()) ? lang : defaultLanguage;
    }

    public List<Pelicula> getPopularMovies(String language) {
        String url = String.format("%s/movie/popular?language=%s", baseUrl, langOrDefault(language));
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), ListaPelicula.class).getBody().getResults();
    }

    public List<Pelicula> getTopRatedMovies(String language) {
        String url = String.format("%s/movie/top_rated?language=%s", baseUrl, langOrDefault(language));
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), ListaPelicula.class).getBody().getResults();
    }

    public List<Pelicula> getNowPlayingMovies(String language) {
        String url = String.format("%s/movie/now_playing?language=%s", baseUrl, langOrDefault(language));
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), ListaPelicula.class).getBody().getResults();
    }

    public List<Pelicula> getUpcomingMovies(String language) {
        String url = String.format("%s/movie/upcoming?language=%s", baseUrl, langOrDefault(language));
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), ListaPelicula.class).getBody().getResults();
    }

    /* ---------- detalles ---------- */
    public Pelicula getMovieDetails(int movieId, String language) {
        String url = String.format("%s/movie/%d?language=%s", baseUrl, movieId, langOrDefault(language));
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), Pelicula.class).getBody();
    }

    /* ---------- favoritos ---------- */
    public boolean markAsFavorite(String sessionId, int accountId, int mediaId, boolean favorite) {
        String url = String.format("%s/account/%d/favorite?session_id=%s",
                baseUrl, accountId, UriUtils.encode(sessionId, StandardCharsets.UTF_8));

        Map<String, Object> body = Map.of(
                "media_type", "movie",
                "media_id", mediaId,
                "favorite", favorite
        );
        Map<?, ?> resp = restTemplate().exchange(url, HttpMethod.POST,
                new HttpEntity<>(body, jsonHeaders()), Map.class).getBody();
        return Boolean.TRUE.equals(resp.get("success"));
    }

    public List<Pelicula> getFavoriteMovies(String sessionId, int accountId) {
        String url = String.format("%s/account/%d/favorite/movies?session_id=%s",
                baseUrl, accountId, UriUtils.encode(sessionId, StandardCharsets.UTF_8));
        return restTemplate().exchange(url, HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()), ListaPelicula.class).getBody().getResults();
    }
}
