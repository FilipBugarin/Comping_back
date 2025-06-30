package hr.demo.demoProject.services;

import hr.demo.demoProject.api.model.ActorRequest;
import hr.demo.demoProject.api.model.MovieRequest;
import hr.demo.demoProject.constants.ProjectConstants;
import hr.demo.demoProject.domain.ProjectAuthUser;
import hr.demo.demoProject.domain.core.*;
import hr.demo.demoProject.repository.core.ActorRepository;
import hr.demo.demoProject.repository.core.MovieActorRepository;
import hr.demo.demoProject.repository.core.MovieRepository;
import hr.demo.demoProject.repository.core.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CoreServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private MovieActorRepository movieActorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CoreService coreService;

    private User mockUser;

    @BeforeEach
    void setupSecurityContext() {
        ProjectAuthUser mockUser = new ProjectAuthUser("admin", "password", Collections.emptyList());
        mockUser.setUserId(1L);

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext context = Mockito.mock(SecurityContext.class);

        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        Mockito.when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coreService = new CoreService(movieActorRepository, movieRepository, actorRepository, userRepository);

        mockUser = new User();
        mockUser.setId(1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
    }

    @Test
    void testCreateMovie() {
        MovieRequest request = new MovieRequest();
        request.setName("Test Movie");
        request.setActorIds(Collections.emptyList());

        Movie saved = new Movie();
        saved.setId(1L);
        saved.setName("Test Movie");

        when(movieRepository.save(any(Movie.class))).thenReturn(saved);

        var response = coreService.createMovie(request);
        assertNotNull(response);
        assertEquals("Test Movie", response.getName());
    }

    @Test
    void testDeleteActor_softDelete() {
        Actor actor = new Actor();
        actor.setId(1L);
        actor.setActive(1);

        when(actorRepository.findByIdAndActive(eq(1L), eq(ProjectConstants.STATUS_ACTIVE)))
                .thenReturn(Optional.of(actor));

        coreService.deleteActor(1);

        assertEquals(ProjectConstants.STATUS_INACTIVE, actor.getActive());
        verify(actorRepository).save(actor);
    }

    @Test
    void testDeleteMovie_softDelete() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setActive(1);

        MovieActor link = new MovieActor();
        link.setMovie(movie);
        link.setActor(new Actor());
        link.setActive(1);

        when(movieRepository.findByIdAndActive(eq(1L), eq(ProjectConstants.STATUS_ACTIVE)))
                .thenReturn(Optional.of(movie));
        when(movieActorRepository.findAllByMovieAndActive(eq(movie), eq(1)))
                .thenReturn(Collections.singletonList(link));

        coreService.deleteMovie(1);

        assertEquals(ProjectConstants.STATUS_INACTIVE, movie.getActive());
        assertEquals(ProjectConstants.STATUS_INACTIVE, link.getActive());

        verify(movieRepository).save(movie);
        verify(movieActorRepository).save(link);
    }

    @Test
    void testCreateActor() {
        ActorRequest request = new ActorRequest();
        request.setDescription("Actor Desc");
        request.setMovieIds(Collections.emptyList());

        Actor actor = new Actor();
        actor.setId(1L);
        actor.setDescription("Actor Desc");

        when(actorRepository.save(any(Actor.class))).thenReturn(actor);

        var response = coreService.createActor(request);
        assertNotNull(response);
        assertEquals("Actor Desc", response.getDescription());
    }
}
