package com.movieDB.RHerreraMovieDB.ControllerMovieDB;

import com.movieDB.RHerreraMovieDB.ServiceMovieDB.ServiceTheMovieDB;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavbarModelAdvice {

    @Autowired
    ServiceTheMovieDB auth;

    @ModelAttribute
    public void exposeSessionAttrs(HttpSession session, Model model) {
        String sid = (String) session.getAttribute("tmdb_session");
        model.addAttribute("tmdbSession", sid);
        model.addAttribute("tmdbUser", session.getAttribute("tmdb_username"));
    }
}
