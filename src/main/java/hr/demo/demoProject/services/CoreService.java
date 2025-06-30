package hr.demo.demoProject.services;

import hr.demo.demoProject.api.model.ActorRequest;
import hr.demo.demoProject.api.model.ActorResponse;
import hr.demo.demoProject.api.model.MovieRequest;
import hr.demo.demoProject.api.model.MovieResponse;
import hr.demo.demoProject.config.exception.DemoProjectException;
import hr.demo.demoProject.config.exception.DemoProjectNotFoundException;
import hr.demo.demoProject.constants.ProjectErrorMessagesConstants;
import hr.demo.demoProject.domain.core.*;
import hr.demo.demoProject.dto.core.CoreMapper;
import hr.demo.demoProject.repository.core.ActorRepository;
import hr.demo.demoProject.repository.core.MovieActorRepository;
import hr.demo.demoProject.repository.core.MovieRepository;
import hr.demo.demoProject.repository.core.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static hr.demo.demoProject.constants.ProjectConstants.*;

@RequiredArgsConstructor
@Log4j2
@Service
public class CoreService extends AbstractService {

    private final MovieActorRepository movieActorRepository;
    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final UserRepository userRepository;

    private User getAuthUser() {
        return userRepository.findById(getProjectAuthUser().getUserId())
                .orElseThrow(() -> new DemoProjectException(ProjectErrorMessagesConstants.NOT_AUTHORIZED));
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // === MOVIES ===

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAllByActive(STATUS_ACTIVE).stream()
                .map(movie -> {
                    List<MovieActor> links = movieActorRepository.findAllByMovieAndActive(movie, STATUS_ACTIVE);
                    return CoreMapper.toMovieResponse(movie, links);
                })
                .toList();
    }

    public MovieResponse getMovieById(Integer id) {
        Movie movie = movieRepository.findByIdAndActive(id.longValue(), STATUS_ACTIVE)
                .orElseThrow(() -> new DemoProjectNotFoundException("Movie not found"));

        List<MovieActor> links = movieActorRepository.findAllByMovieAndActive(movie, STATUS_ACTIVE);
        return CoreMapper.toMovieResponse(movie, links);
    }

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setName(request.getName());
        setAuditOnCreate(movie);
        movie = movieRepository.save(movie);

        List<MovieActor> links = new ArrayList<>();
        if (request.getActorIds() != null) {
            for (Integer actorId : request.getActorIds()) {
                Actor actor = actorRepository.findByIdAndActive(actorId.longValue(), STATUS_ACTIVE)
                        .orElseThrow(() -> new DemoProjectNotFoundException("Actor not found"));

                MovieActor link = MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .build();
                setAuditOnCreate(link);
                links.add(movieActorRepository.save(link));
            }
        }

        log.info("Created Movie ID {} with {} actor links", movie.getId(), links.size());
        return CoreMapper.toMovieResponse(movie, links);
    }

    public MovieResponse updateMovie(Integer id, MovieRequest request) {
        Movie movie = movieRepository.findByIdAndActive(id.longValue(), STATUS_ACTIVE)
                .orElseThrow(() -> new DemoProjectNotFoundException("Movie not found"));

        movie.setName(request.getName());
        setAuditOnUpdate(movie);
        movie = movieRepository.save(movie);

        List<MovieActor> existingLinks = movieActorRepository.findAllByMovieAndActive(movie, STATUS_ACTIVE);
        for (MovieActor link : existingLinks) {
            link.setActive(STATUS_INACTIVE);
            setAuditOnUpdate(link);
            movieActorRepository.save(link);
        }

        List<MovieActor> links = new ArrayList<>();
        for (Integer actorId : request.getActorIds()) {
            Actor actor = actorRepository.findByIdAndActive(actorId.longValue(), STATUS_ACTIVE)
                    .orElseThrow(() -> new DemoProjectNotFoundException("Actor not found"));

            MovieActor link = MovieActor.builder()
                    .movie(movie)
                    .actor(actor)
                    .build();
            setAuditOnCreate(link);
            links.add(movieActorRepository.save(link));
        }

        log.info("Updated Movie ID {} with {} actor links", movie.getId(), links.size());
        return CoreMapper.toMovieResponse(movie, links);
    }

    public void deleteMovie(Integer id) {
        Movie movie = movieRepository.findByIdAndActive(id.longValue(), STATUS_ACTIVE)
                .orElseThrow(() -> new DemoProjectNotFoundException("Movie not found"));

        movie.setActive(STATUS_INACTIVE);
        setAuditOnUpdate(movie);
        movieRepository.save(movie);

        List<MovieActor> links = movieActorRepository.findAllByMovieAndActive(movie, STATUS_ACTIVE);
        for (MovieActor link : links) {
            link.setActive(STATUS_INACTIVE);
            setAuditOnUpdate(link);
            movieActorRepository.save(link);
        }

        log.info("Soft-deleted Movie ID {} and {} actor link(s)", movie.getId(), links.size());
    }

    // === ACTORS ===

    public List<ActorResponse> getAllActors() {
        return actorRepository.findAllByActive(STATUS_ACTIVE).stream()
                .map(actor -> {
                    List<MovieActor> links = movieActorRepository.findAllByActorAndActive(actor, STATUS_ACTIVE);
                    return CoreMapper.toActorResponse(actor, links);
                })
                .toList();
    }

    public ActorResponse getActorById(Integer id) {
        Actor actor = actorRepository.findByIdAndActive(id.longValue(), STATUS_ACTIVE)
                .orElseThrow(() -> new DemoProjectNotFoundException("Actor not found"));

        List<MovieActor> links = movieActorRepository.findAllByActorAndActive(actor, STATUS_ACTIVE);
        return CoreMapper.toActorResponse(actor, links);
    }

    public ActorResponse createActor(ActorRequest request) {
        Actor actor = new Actor();
        actor.setDescription(request.getDescription());
        setAuditOnCreate(actor);
        actor = actorRepository.save(actor);

        List<MovieActor> links = new ArrayList<>();
        if (request.getMovieIds() != null) {
            for (Integer movieId : request.getMovieIds()) {
                Movie movie = movieRepository.findByIdAndActive(movieId.longValue(), STATUS_ACTIVE)
                        .orElseThrow(() -> new DemoProjectNotFoundException("Movie not found"));

                MovieActor link = MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .build();
                setAuditOnCreate(link);
                links.add(movieActorRepository.save(link));
            }
        }

        log.info("Created Actor ID {} with {} movie links", actor.getId(), links.size());
        return CoreMapper.toActorResponse(actor, links);
    }

    public ActorResponse updateActor(Integer id, ActorRequest request) {
        Actor actor = actorRepository.findByIdAndActive(id.longValue(), STATUS_ACTIVE)
                .orElseThrow(() -> new DemoProjectNotFoundException("Actor not found"));

        actor.setDescription(request.getDescription());
        setAuditOnUpdate(actor);
        actor = actorRepository.save(actor);

        List<MovieActor> existingLinks = movieActorRepository.findAllByActorAndActive(actor, STATUS_ACTIVE);
        for (MovieActor link : existingLinks) {
            link.setActive(STATUS_INACTIVE);
            setAuditOnUpdate(link);
            movieActorRepository.save(link);
        }

        List<MovieActor> links = new ArrayList<>();
        if (request.getMovieIds() != null) {
            for (Integer movieId : request.getMovieIds()) {
                Movie movie = movieRepository.findByIdAndActive(movieId.longValue(), STATUS_ACTIVE)
                        .orElseThrow(() -> new DemoProjectNotFoundException("Movie not found"));

                MovieActor link = MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .build();
                setAuditOnCreate(link);
                links.add(movieActorRepository.save(link));
            }
        }

        log.info("Updated Actor ID {} with {} movie links", actor.getId(), links.size());
        return CoreMapper.toActorResponse(actor, links);
    }

    public void deleteActor(Integer id) {
        Actor actor = actorRepository.findByIdAndActive(id.longValue(), STATUS_ACTIVE)
                .orElseThrow(() -> new DemoProjectNotFoundException("Actor not found"));

        actor.setActive(STATUS_INACTIVE);
        setAuditOnUpdate(actor);
        actorRepository.save(actor);

        List<MovieActor> links = movieActorRepository.findAllByActorAndActive(actor, STATUS_ACTIVE);
        for (MovieActor link : links) {
            link.setActive(STATUS_INACTIVE);
            setAuditOnUpdate(link);
            movieActorRepository.save(link);
        }

        log.info("Soft-deleted Actor ID {} and {} movie link(s)", actor.getId(), links.size());
    }

    // === AUDIT HELPERS ===

    private void setAuditOnCreate(AbstractDomain entity) {
        entity.setCreatedBy(getAuthUser());
        entity.setCreatedDate(now());
        entity.setActive(STATUS_ACTIVE);
    }

    private void setAuditOnUpdate(AbstractDomain entity) {
        entity.setModifiedBy(getAuthUser());
        entity.setModifiedDate(now());
    }
}
