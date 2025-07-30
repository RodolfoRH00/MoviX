package com.movieDB.RHerreraMovieDB.ControllerMovieDB;

import com.movieDB.RHerreraMovieDB.ML.Cuenta;
import com.movieDB.RHerreraMovieDB.ML.Pelicula;
import com.movieDB.RHerreraMovieDB.ML.Token;
import com.movieDB.RHerreraMovieDB.ServiceMovieDB.ServiceTheMovieDB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Controller
public class ControllerTheMovieDB {

    @Autowired
    private ServiceTheMovieDB service;

// Mostrar formulario
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }

// Procesar credenciales
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra
    ) {
        try {
            // 1) Nuevo request_token
            String rt = service.newRequestToken().getRequest_token();
            // 2) Validar con usuario/clave
            Token validated = service.validateWithLogin(username, password, rt);
            // 3) Crear sesión
            String sessionId = service.createSession(validated.getRequest_token());
            session.setAttribute("tmdb_session", sessionId);

            // 4) Cargar cuenta
            Cuenta acct = service.getAccount(sessionId);
            session.setAttribute("tmdb_username", acct.getUsername());
            session.setAttribute("tmdb_accountId", (int) acct.getId());

            return "redirect:/";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Credenciales inválidas o error de conexión.");
            return "redirect:/login";
        }
    }

    @GetMapping("/auth/callback")
    public String callback(@RequestParam(name = "approved", required = false) Boolean approved,
            @RequestParam("request_token") String token,
            HttpSession session, RedirectAttributes ra) {
        if (approved == null || !approved) {
            ra.addFlashAttribute("mensaje", "Acceso denegado por el usuario.");
            return "redirect:/error-auth";
        }
        String sessionId = service.createSession(token);
        session.setAttribute("tmdb_session", sessionId);
        try {
            Cuenta acct = service.getAccount(sessionId);
            session.setAttribute("tmdb_username", acct.getUsername());
            session.setAttribute("tmdb_accountId", (int) acct.getId());
        } catch (Exception ignored) {
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String sid = (String) session.getAttribute("tmdb_session");
        if (sid != null) {
            service.deleteSession(sid);
        }
        session.invalidate();
        return "redirect:/";
    }

    // ——— home + categorías ———
    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        List<Pelicula> populares = service.getPopularMovies();
        List<Pelicula> mejorValoradas = service.getTopRatedMovies();
        List<Pelicula> cartelera = service.getNowPlayingMovies();
        List<Pelicula> proximas = service.getUpcomingMovies();

        model.addAttribute("populares", populares);
        model.addAttribute("mejorValoradas", mejorValoradas);
        model.addAttribute("cartelera", cartelera);
        model.addAttribute("proximas", proximas);

        // Calcula favoritos del usuario
        String sessionId = (String) session.getAttribute("tmdb_session");
        Integer accountId = (Integer) session.getAttribute("tmdb_accountId");
        Set<Integer> favIds = Collections.emptySet();
        if (sessionId != null && accountId != null) {
            favIds = service.getFavoriteMovies(sessionId, accountId)
                    .stream().map(Pelicula::getId)
                    .collect(Collectors.toSet());
        }
        model.addAttribute("favoriteIds", favIds);

        return "Netflix";
    }

    @GetMapping("/popular")
    public String popular(Model model, HttpSession session) {
        List<Pelicula> pelis = service.getPopularMovies();
        model.addAttribute("pelis", pelis);

        String sessionId = (String) session.getAttribute("tmdb_session");
        Integer accountId = (Integer) session.getAttribute("tmdb_accountId");
        Set<Integer> favIds = Collections.emptySet();
        if (sessionId != null && accountId != null) {
            favIds = service.getFavoriteMovies(sessionId, accountId)
                    .stream().map(Pelicula::getId)
                    .collect(Collectors.toSet());
        }
        model.addAttribute("favoriteIds", favIds);
        return "populares";
    }

    @GetMapping("/top-rated")
    public String topRated(Model model) {
        model.addAttribute("pelis", service.getTopRatedMovies());
        return "lista";
    }

    @GetMapping("/now-playing")
    public String nowPlaying(Model model) {
        model.addAttribute("pelis", service.getNowPlayingMovies());
        return "lista";
    }

    @GetMapping("/upcoming")
    public String upcoming(Model model) {
        model.addAttribute("pelis", service.getUpcomingMovies());
        return "lista";
    }

    @PostMapping("/favorite")
    public String favorite(
            @RequestParam("mediaId") int mediaId,
            @RequestParam(value = "favorite", defaultValue = "true") boolean fav,
            HttpSession session,
            RedirectAttributes ra,
            HttpServletRequest request // <<< nuevo
    ) {
        String sessionId = (String) session.getAttribute("tmdb_session");
        Integer accountId = (Integer) session.getAttribute("tmdb_accountId");
        if (sessionId == null || accountId == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para añadir favoritos.");
            return "redirect:/login";
        }

        boolean ok = service.markAsFavorite(sessionId, accountId, mediaId, fav);
        if (!ok) {
            ra.addFlashAttribute("error", "No se pudo actualizar favorito.");
        }

        // redirige de nuevo a la página que hizo la petición
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/lista")
    public String verMiLista(Model model, HttpSession session, RedirectAttributes ra) {
        String sessionId = (String) session.getAttribute("tmdb_session");
        Integer accountId = (Integer) session.getAttribute("tmdb_accountId");
        if (sessionId == null || accountId == null) {
            ra.addFlashAttribute("error", "Inicia sesión para ver tu lista.");
            return "redirect:/login";
        }

        List<Pelicula> favoritos = service.getFavoriteMovies(sessionId, accountId);
        model.addAttribute("favoritos", favoritos);
        return "lista";
    }

    @GetMapping("/movie/{id}")
    public String verDetalles(@PathVariable("id") int id,
            Model model,
            HttpSession session,
            RedirectAttributes ra) {
        Pelicula pelicula = service.getMovieDetails(id);
        model.addAttribute("pelicula", pelicula);
        return "movie-details";
    }

}
