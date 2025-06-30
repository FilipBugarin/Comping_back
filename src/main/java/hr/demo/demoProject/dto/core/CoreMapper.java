package hr.demo.demoProject.dto.core;

import hr.demo.demoProject.api.model.ActorResponse;
import hr.demo.demoProject.api.model.MovieResponse;
import hr.demo.demoProject.domain.core.Actor;
import hr.demo.demoProject.domain.core.Movie;
import hr.demo.demoProject.domain.core.MovieActor;

import java.util.List;

public class CoreMapper {

    public static MovieResponse toMovieResponse(Movie movie, List<MovieActor> links) {
        return MovieResponse.builder()
                .id(movie.getId().intValue())
                .name(movie.getName())
                .actors(links.stream()
                        .map(link -> {
                            Actor actor = link.getActor();
                            return ActorResponse.builder()
                                    .id(actor.getId().intValue())
                                    .description(actor.getDescription())
                                    .build();
                        })
                        .toList())
                .build();
    }

    public static ActorResponse toActorResponse(Actor actor, List<MovieActor> links) {
        return ActorResponse.builder()
                .id(actor.getId().intValue())
                .description(actor.getDescription())
                .movies(
                        links.stream()
                                .map(link -> {
                                    Movie movie = link.getMovie();
                                    return MovieResponse.builder()
                                            .id(movie.getId().intValue())
                                            .name(movie.getName())
                                            .build();
                                })
                                .toList()
                )
                .build();
    }
}
