package com.movieDB.RHerreraMovieDB.ServiceMovieDB;

import com.movieDB.RHerreraMovieDB.ML.Cuenta;
import com.movieDB.RHerreraMovieDB.ML.ListaPelicula;
import com.movieDB.RHerreraMovieDB.ML.Pelicula;
import com.movieDB.RHerreraMovieDB.ML.Session;
import com.movieDB.RHerreraMovieDB.ML.Token;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
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

    private RestTemplate rt() {
        return new RestTemplate();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearer);
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    public Token newRequestToken() {
        var entity = new HttpEntity<Void>(jsonHeaders());
        return rt()
                .exchange(baseUrl + "/authentication/token/new",
                        HttpMethod.GET, entity, Token.class)
                .getBody();
    }

    public String authUrl(String requestToken) {
        try {
            String rt = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
            return "https://www.themoviedb.org/authenticate/"
                    + requestToken + "?redirect_to=" + rt;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String createSession(String approvedToken) {
        Map<String, String> body = Map.of("request_token", approvedToken);
        var entity = new HttpEntity<>(body, jsonHeaders());
        Session s = rt()
                .exchange(baseUrl + "/authentication/session/new",
                        HttpMethod.POST, entity, Session.class)
                .getBody();
        return s.getSession_id();
    }

    public Cuenta getAccount(String sessionId) {
        var entity = new HttpEntity<Void>(jsonHeaders());
        String url = baseUrl + "/account?session_id="
                + UriUtils.encode(sessionId, StandardCharsets.UTF_8);
        return rt()
                .exchange(url, HttpMethod.GET, entity, Cuenta.class)
                .getBody();
    }

    public boolean deleteSession(String sessionId) {
        var entity = new HttpEntity<>(Map.of("session_id", sessionId), jsonHeaders());
        Map resp = rt()
                .exchange(baseUrl + "/authentication/session",
                        HttpMethod.DELETE, entity, Map.class)
                .getBody();
        return Boolean.TRUE.equals(resp.get("success"));
    }

    public List<Pelicula> getPopularMovies() {
        var entity = new HttpEntity<Void>(jsonHeaders());
        ListaPelicula lp = rt()
                .exchange(baseUrl + "/movie/popular", HttpMethod.GET, entity, ListaPelicula.class)
                .getBody();
        return lp.getResults();
    }

    public List<Pelicula> getTopRatedMovies() {
        var entity = new HttpEntity<Void>(jsonHeaders());
        ListaPelicula lp = rt()
                .exchange(baseUrl + "/movie/top_rated", HttpMethod.GET, entity, ListaPelicula.class)
                .getBody();
        return lp.getResults();
    }

    public List<Pelicula> getNowPlayingMovies() {
        var entity = new HttpEntity<Void>(jsonHeaders());
        ListaPelicula lp = rt()
                .exchange(baseUrl + "/movie/now_playing", HttpMethod.GET, entity, ListaPelicula.class)
                .getBody();
        return lp.getResults();
    }

    public List<Pelicula> getUpcomingMovies() {
        var entity = new HttpEntity<Void>(jsonHeaders());
        ListaPelicula lp = rt()
                .exchange(baseUrl + "/movie/upcoming", HttpMethod.GET, entity, ListaPelicula.class)
                .getBody();
        return lp.getResults();
    }

    public boolean markAsFavorite(String sessionId, int accountId, int mediaId, boolean favorite) {
        String url = String.format("%s/account/%d/favorite?session_id=%s",
                baseUrl, accountId, UriUtils.encode(sessionId, StandardCharsets.UTF_8));

        Map<String, Object> body = Map.of(
                "media_type", "movie",
                "media_id", mediaId,
                "favorite", favorite
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, jsonHeaders());
        Map<?, ?> resp = rt()
                .exchange(url, HttpMethod.POST, entity, Map.class)
                .getBody();

        return Boolean.TRUE.equals(resp.get("success"));
    }

    public List<Pelicula> getFavoriteMovies(String sessionId, int accountId) {
        String url = String.format("%s/account/%d/favorite/movies?session_id=%s",
                baseUrl,
                accountId,
                UriUtils.encode(sessionId, StandardCharsets.UTF_8));

        HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
        ListaPelicula lp = rt()
                .exchange(url, HttpMethod.GET, entity, ListaPelicula.class)
                .getBody();

        return lp.getResults();
    }

    public Pelicula getMovieDetails(int movieId) {
        String url = String.format("%s/movie/%d", baseUrl, movieId);
        HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
        ResponseEntity<Pelicula> res = rt()
                .exchange(url, HttpMethod.GET, entity, Pelicula.class);
        return res.getBody();
    }
    
    public Token validateWithLogin(String username, String password, String requestToken) {
        String url = baseUrl + "/authentication/token/validate_with_login";
        Map<String, String> body = Map.of(
                "username", username,
                "password", password,
                "request_token", requestToken
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, jsonHeaders());
        ResponseEntity<Token> res = rt()
                .exchange(url, HttpMethod.POST, entity, Token.class);
        return res.getBody();
    }

}
