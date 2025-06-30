package hr.demo.demoProject.api;

import hr.demo.demoProject.api.model.ActorRequest;
import hr.demo.demoProject.api.model.ActorResponse;
import hr.demo.demoProject.api.model.MovieRequest;
import hr.demo.demoProject.api.model.MovieResponse;
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
     * GET /actors : Get all actors
     *
     * @return List of actors (status code 200)
     */
    @Override
    public ResponseEntity<List<ActorResponse>> actorsGet() {
        return ResponseEntity.ok(coreService.getAllActors());
    }


    /**
     * DELETE /actors/{id} : Delete actor
     *
     * @param id (required)
     * @return Deleted successfully (status code 204)
     */
    @Override
    public ResponseEntity<Void> actorsIdDelete(Integer id) {
        coreService.deleteActor(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /actors/{id} : Get actor by ID
     *
     * @param id (required)
     * @return Single actor (status code 200)
     */
    @Override
    public ResponseEntity<ActorResponse> actorsIdGet(Integer id) {
        return ResponseEntity.ok(coreService.getActorById(id));
    }

    /**
     * PUT /actors/{id} : Update actor
     *
     * @param id           (required)
     * @param actorRequest (required)
     * @return Updated actor (status code 200)
     */
    @Override
    public ResponseEntity<ActorResponse> actorsIdPut(Integer id, ActorRequest actorRequest) {
        return ResponseEntity.ok(coreService.updateActor(id, actorRequest));
    }

    /**
     * POST /actors : Create new actor
     *
     * @param actorRequest (required)
     * @return Actor created (status code 201)
     */
    @Override
    public ResponseEntity<ActorResponse> actorsPost(ActorRequest actorRequest) {
        return ResponseEntity.status(201).body(coreService.createActor(actorRequest));
    }

    /**
     * GET /movies : Get all movies
     *
     * @return List of movies (status code 200)
     */
    public ResponseEntity<List<MovieResponse>> moviesGet() {
        return ResponseEntity.ok(coreService.getAllMovies());
    }

    /**
     * DELETE /movies/{id} : Delete movie
     *
     * @param id (required)
     * @return Deleted successfully (status code 204)
     */
    @Override
    public ResponseEntity<Void> moviesIdDelete(Integer id) {
        coreService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /movies/{id} : Get movie by ID
     *
     * @param id (required)
     * @return Single movie (status code 200)
     */
    public ResponseEntity<MovieResponse> moviesIdGet(Integer id) {
        return ResponseEntity.ok(coreService.getMovieById(id));
    }

    /**
     * PUT /movies/{id} : Update movie
     *
     * @param id           (required)
     * @param movieRequest (required)
     * @return Updated movie (status code 200)
     */
    public ResponseEntity<MovieResponse> moviesIdPut(Integer id, MovieRequest movieRequest) {
        return ResponseEntity.ok(coreService.updateMovie(id, movieRequest));
    }

    /**
     * POST /movies : Create new movie
     *
     * @param movieRequest (required)
     * @return Movie created (status code 201)
     */
    @Override
    public ResponseEntity<MovieResponse> moviesPost(MovieRequest movieRequest) {
        return ResponseEntity.status(201).body(coreService.createMovie(movieRequest));
    }
}
