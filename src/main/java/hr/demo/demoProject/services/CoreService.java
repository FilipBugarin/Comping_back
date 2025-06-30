package hr.demo.demoProject.services;

import hr.demo.demoProject.api.model.*;
import hr.demo.demoProject.config.exception.DemoProjectException;
import hr.demo.demoProject.config.exception.DemoProjectNotFoundException;
import hr.demo.demoProject.constants.ProjectErrorMessagesConstants;
import hr.demo.demoProject.domain.core.*;
import hr.demo.demoProject.dto.core.CoreMapper;
import hr.demo.demoProject.repository.core.ActorRepository;
import hr.demo.demoProject.repository.core.MovieActorRepository;
import hr.demo.demoProject.repository.core.MovieRepository;
import hr.demo.demoProject.repository.core.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
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

    private final EntityManager entityManager;

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

    public MoviesFilterPost200Response getMoviesFiltered(GetMoviesRequest request) {
        log.debug("Started {} with input : {}", "getMoviesFiltered", request);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // === DATA QUERY ===
        CriteriaQuery<Movie> criteriaQuery = cb.createQuery(Movie.class);
        Root<Movie> root = criteriaQuery.from(Movie.class);

        List<Predicate> predicates = buildMoviePredicates(cb, root, request);

        criteriaQuery.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        criteriaQuery.orderBy(cb.asc(root.get("name")));

        TypedQuery<Movie> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(request.getOffset() * request.getLimit());
        typedQuery.setMaxResults(request.getLimit());

        List<Movie> movies = typedQuery.getResultList();

        // === COUNT QUERY ===
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Movie> countRoot = countQuery.from(Movie.class);
        List<Predicate> countPredicates = buildMoviePredicates(cb, countRoot, request);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<MovieResponse> movieResponses = movies.stream().map(movie -> {
            List<MovieActor> links = movieActorRepository.findAllByMovieAndActive(movie, STATUS_ACTIVE);
            return CoreMapper.toMovieResponse(movie, links);
        }).toList();

        PagingInfo pagingInfo = PagingInfo.builder()
                .length(total)
                .pageSize(request.getLimit())
                .pageIndex(request.getOffset())
                .build();

        return new MoviesFilterPost200Response().data(movieResponses).pagingInfo(pagingInfo);
    }

    private List<Predicate> buildMoviePredicates(CriteriaBuilder cb, Root<Movie> root, GetMoviesRequest request) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("active"), STATUS_ACTIVE));

        if (request.getName() != null && !request.getName().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%%%s%%".formatted(request.getName().toLowerCase())));
        }

        if (request.getActorIds() != null && !request.getActorIds().isEmpty()) {
            Join<Movie, MovieActor> join = root.join("actorsXmovies", JoinType.LEFT);
            predicates.add(cb.equal(join.get("active"), STATUS_ACTIVE)); // Filter only active links
            predicates.add(join.get("actor").get("id").in(request.getActorIds())); // Filter by actor IDs
        }

        return predicates;
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

    public ActorsFilterPost200Response getActorsFiltered(GetActorsRequest request) {
        log.debug("Started {} with input : {}", "getActorsFiltered", request);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // === DATA QUERY ===
        CriteriaQuery<Actor> criteriaQuery = cb.createQuery(Actor.class);
        Root<Actor> root = criteriaQuery.from(Actor.class);

        List<Predicate> predicates = buildActorPredicates(cb, root, request);

        criteriaQuery.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        criteriaQuery.orderBy(cb.asc(root.get("description")));

        TypedQuery<Actor> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(request.getOffset() * request.getLimit());
        typedQuery.setMaxResults(request.getLimit());

        List<Actor> actors = typedQuery.getResultList();

        // === COUNT QUERY ===
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Actor> countRoot = countQuery.from(Actor.class);
        List<Predicate> countPredicates = buildActorPredicates(cb, countRoot, request);
        countQuery.select(cb.count(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<ActorResponse> actorResponses = actors.stream().map(actor -> {
            List<MovieActor> links = movieActorRepository.findAllByActorAndActive(actor, STATUS_ACTIVE);
            return CoreMapper.toActorResponse(actor, links);
        }).toList();

        PagingInfo pagingInfo = PagingInfo.builder()
                .length(total)
                .pageSize(request.getLimit())
                .pageIndex(request.getOffset())
                .build();

        return new ActorsFilterPost200Response().data(actorResponses).pagingInfo(pagingInfo);
    }

    private List<Predicate> buildActorPredicates(CriteriaBuilder cb, Root<Actor> root, GetActorsRequest request) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("active"), STATUS_ACTIVE));

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("description")), "%%%s%%".formatted(request.getDescription().toLowerCase())));
        }

        if (request.getMovieIds() != null && !request.getMovieIds().isEmpty()) {
            Join<Actor, MovieActor> join = root.join("moviesXActors", JoinType.LEFT);
            predicates.add(cb.equal(join.get("active"), STATUS_ACTIVE)); // Filter only active links
            predicates.add(join.get("movie").get("id").in(request.getMovieIds())); // Filter by movie IDs
        }

        return predicates;
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

    private void setTypedQueryPagination(TypedQuery<?> query, int limit, int offset) {
        query.setFirstResult(offset * limit);
        query.setMaxResults(limit);
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
