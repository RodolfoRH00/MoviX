package com.movieDB.RHerreraMovieDB.ControllerMovieDB;

import com.movieDB.RHerreraMovieDB.ML.Pelicula;
import com.movieDB.RHerreraMovieDB.ServiceMovieDB.ServiceMovieDB;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/inicio")
public class ControllerMovieDB {

    @Autowired
    private ServiceMovieDB serviceMovieDB;

    @GetMapping("/peliculas-populares")
    public String mostrarPeliculasPopulares(Model model) {
        List<Pelicula> peliculas = serviceMovieDB.PeliculasPopulares();
        for (Pelicula pelicula : peliculas) {
            if (pelicula.getPoster_Path() != null) {
                pelicula.setPoster_Path("https://image.tmdb.org/t/p/w500" + pelicula.getPoster_Path());
            }
        }

        model.addAttribute("peliculas", peliculas);
        return "Netflix";
    }

}
