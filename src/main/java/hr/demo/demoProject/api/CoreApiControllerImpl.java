package hr.demo.demoProject.api;

import hr.demo.demoProject.api.model.*;
import hr.demo.demoProject.services.CoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CoreApiControllerImpl implements CoreApi{

    private final CoreService coreService;

    /**
     * GET /actor : Get actor by ID
     *
     * @param id (required)
     * @return Single actor (status code 200)
     */
    @Override
    public ResponseEntity<ActorResponse> actorIdGet(Integer id) {
        return ResponseEntity.ok(coreService.getActorById(id));
    }

    /**
     * POST /actor : Create new actor
     *
     * @param actorRequest (required)
     * @return Actor created (status code 201)
     */
    @Override
    public ResponseEntity<ActorResponse> actorPost(ActorRequest actorRequest) {
        return ResponseEntity.status(201).body(coreService.createActor(actorRequest));
    }

    /**
     * PUT /actor : Update actor
     *
     * @param id           (required)
     * @param actorRequest (required)
     * @return Updated actor (status code 200)
     */
    @Override
    public ResponseEntity<ActorResponse> actorIdPut(Integer id, ActorRequest actorRequest) {
        return ResponseEntity.ok(coreService.updateActor(id, actorRequest));
    }

    /**
     * POST /actors/filter : Filter and paginate actors
     *
     * @param getActorsRequest (required)
     * @return Paginated list of actors (status code 200)
     */
    @Override
    public ResponseEntity<ActorsFilterPost200Response> actorsFilterPost(GetActorsRequest getActorsRequest) {
        var response = coreService.getActorsFiltered(getActorsRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /movie : Delete movie
     *
     * @param id (required)
     * @return Deleted successfully (status code 204)
     */
    @Override
    public ResponseEntity<Void> movieIdDelete(Integer id) {
        coreService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /movie : Get movie by ID
     *
     * @param id (required)
     * @return Single movie (status code 200)
     */
    @Override
    public ResponseEntity<MovieResponse> movieIdGet(Integer id) {
        return ResponseEntity.ok(coreService.getMovieById(id));
    }

    /**
     * POST /movie : Create new movie
     *
     * @param movieRequest (required)
     * @return Movie created (status code 201)
     */
    @Override
    public ResponseEntity<MovieResponse> moviePost(MovieRequest movieRequest) {
        return ResponseEntity.status(201).body(coreService.createMovie(movieRequest));
    }

    /**
     * PUT /movie : Update movie
     *
     * @param id           (required)
     * @param movieRequest (required)
     * @return Updated movie (status code 200)
     */
    @Override
    public ResponseEntity<MovieResponse> movieIdPut(Integer id, MovieRequest movieRequest) {
        return ResponseEntity.ok(coreService.updateMovie(id, movieRequest));
    }

    /**
     * POST /movies/filter : Filter and paginate movies
     *
     * @param getMoviesRequest (required)
     * @return Paginated list of movies (status code 200)
     */
    @Override
    public ResponseEntity<MoviesFilterPost200Response> moviesFilterPost(GetMoviesRequest getMoviesRequest) {
        var response = coreService.getMoviesFiltered(getMoviesRequest);
        return ResponseEntity.ok(response);
    }
}
