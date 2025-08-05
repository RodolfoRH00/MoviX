package com.movieDB.RHerreraMovieDB.ControllerMovieDB;

import com.movieDB.RHerreraMovieDB.ML.*;
import com.movieDB.RHerreraMovieDB.ServiceMovieDB.ServiceTheMovieDB;
import jakarta.servlet.http.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControllerTheMovieDB {

    @Autowired
    private ServiceTheMovieDB service;
    @Autowired
    private HttpServletRequest request;

    @Value("${tmdb.default-language}")
    private String defaultLanguage;

    /* ---------- idiomas disponibles & actual ---------- */
    private static final Set<String> SUPPORTED = Set.of(
            "en", "es", "fr", "de", "it", "pt", "ja", "ko", "zh", "ru"
    );

    @ModelAttribute
    public void addLangData(Model model, HttpSession session) {
        List<Lenguaje> langs = service.getAvailableLanguages()
                .stream()
                .filter(l -> SUPPORTED.contains(l.getIso_639_1()))
                .sorted(Comparator.comparing(Lenguaje::getEnglish_name))
                .collect(Collectors.toList());

        model.addAttribute("languages", langs);

        String cur = (String) session.getAttribute("tmdb_language");
        model.addAttribute("currentLanguage",
                (cur != null ? cur : defaultLanguage));
    }

    @GetMapping("/language")
    public String changeLanguage(@RequestParam String lang, HttpSession session) {
        session.setAttribute("tmdb_language", lang);
        return "redirect:" + Optional.ofNullable(request.getHeader("Referer")).orElse("/");
    }

    /* ---------- login personalizado ---------- */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra) {
        try {
            String rt = service.newRequestToken().getRequest_token();
            Token validated = service.validateWithLogin(username, password, rt);
            String sessId = service.createSession(validated.getRequest_token());
            session.setAttribute("tmdb_session", sessId);

            Cuenta acct = service.getAccount(sessId);
            session.setAttribute("tmdb_username", acct.getUsername());
            session.setAttribute("tmdb_accountId", (int) acct.getId());
            return "redirect:/";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Credenciales inválidas o error de conexión.");
            return "redirect:/login";
        }
    }

    /* ---------- logout ---------- */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Optional.ofNullable((String) session.getAttribute("tmdb_session"))
                .ifPresent(service::deleteSession);
        session.invalidate();
        return "redirect:/";
    }

    /* ---------- helpers favoritos ---------- */
    private Set<Integer> currentFavIds(HttpSession s) {
        String sid = (String) s.getAttribute("tmdb_session");
        Integer acc = (Integer) s.getAttribute("tmdb_accountId");
        if (sid == null || acc == null) {
            return Collections.emptySet();
        }
        return service.getFavoriteMovies(sid, acc).stream()
                .map(Pelicula::getId).collect(Collectors.toSet());
    }

    /* ---------- home ---------- */
    @GetMapping({"/", "/home"})
    public String home(Model m, HttpSession session) {
        String lang = currentLang(session);
        List<Pelicula> populares = service.getPopularMovies(lang);
        List<Pelicula> topRatedMovie = service.getTopRatedMovies(lang);
        List<Pelicula> nowPlayingMovie = service.getNowPlayingMovies(lang);
        List<Pelicula> upComingMovie = service.getUpcomingMovies(lang);
        m.addAttribute("populares", populares);
        m.addAttribute("mejorValoradas", topRatedMovie);
        m.addAttribute("cartelera", nowPlayingMovie);
        m.addAttribute("proximas", upComingMovie);
        m.addAttribute("favoriteIds", currentFavIds(session));
        return "Netflix";
    }

    /* ---------- categorías ---------- */
    @GetMapping("/popular")
    public String popular(Model m, HttpSession s) {
        String lang = currentLang(s);
        m.addAttribute("pelis", service.getPopularMovies(lang));
        m.addAttribute("favoriteIds", currentFavIds(s));
        return "populares";
    }

    @GetMapping("/top-rated")
    public String topRated(Model m, HttpSession s) {
        String lang = currentLang(s);
        m.addAttribute("pelis", service.getTopRatedMovies(lang));
        m.addAttribute("favoriteIds", currentFavIds(s));
        return "lista";
    }

    @GetMapping("/now-playing")
    public String nowPlaying(Model m, HttpSession s) {
        String lang = currentLang(s);
        m.addAttribute("pelis", service.getNowPlayingMovies(lang));
        m.addAttribute("favoriteIds", currentFavIds(s));
        return "lista";
    }

    @GetMapping("/upcoming")
    public String upcoming(Model m, HttpSession s) {
        String lang = currentLang(s);
        m.addAttribute("pelis", service.getUpcomingMovies(lang));
        m.addAttribute("favoriteIds", currentFavIds(s));
        return "lista";
    }

    /* ---------- detalles ---------- */
    @GetMapping("/movie/{id}")
    public String verDetalles(@PathVariable int id, Model m, HttpSession s) {
        String lang = currentLang(s);
        m.addAttribute("pelicula", service.getMovieDetails(id, lang));
        return "movie-details";
    }

    /* ---------- favoritos ---------- */
    @PostMapping("/favorite")
    public String favorite(@RequestParam int mediaId,
            @RequestParam(defaultValue = "true") boolean favorite,
            HttpSession session,
            RedirectAttributes ra) {

        String sid = (String) session.getAttribute("tmdb_session");
        Integer acc = (Integer) session.getAttribute("tmdb_accountId");
        if (sid == null || acc == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión.");
            return "redirect:/login";
        }

        service.markAsFavorite(sid, acc, mediaId, favorite);

        // mensaje para la vista
        String msg = favorite ? "Añadida a tu lista" : "Quitada de tu lista";
        ra.addFlashAttribute("toast", msg);

        // volver a la misma página
        String back = Optional.ofNullable(request.getHeader("Referer")).orElse("/");
        return "redirect:" + back;
    }


    /* ---------- lista personal ---------- */
    @GetMapping("/lista")
    public String miLista(Model m, HttpSession s, RedirectAttributes ra) {
        String sid = (String) s.getAttribute("tmdb_session");
        Integer acc = (Integer) s.getAttribute("tmdb_accountId");
        if (sid == null || acc == null) {
            ra.addFlashAttribute("error", "Inicia sesión para ver tu lista.");
            return "redirect:/login";
        }
        m.addAttribute("favoritos", service.getFavoriteMovies(sid, acc));
        return "lista";
    }

    private String currentLang(HttpSession session) {
        String lang = (String) session.getAttribute("tmdb_language");
        return (lang != null ? lang : defaultLanguage);   // «es-ES» por defecto
    }
}
