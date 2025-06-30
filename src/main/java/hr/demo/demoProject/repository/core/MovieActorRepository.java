package hr.demo.demoProject.repository.core;

import hr.demo.demoProject.domain.core.Actor;
import hr.demo.demoProject.domain.core.Movie;
import hr.demo.demoProject.domain.core.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieActorRepository extends JpaRepository<MovieActor, Long> {
    List<MovieActor> findAllByMovieAndActive(Movie movie, Integer active);
    List<MovieActor> findAllByActorAndActive(Actor actor, Integer active);
}
