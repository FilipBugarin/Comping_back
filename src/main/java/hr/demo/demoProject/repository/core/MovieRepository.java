package hr.demo.demoProject.repository.core;


import hr.demo.demoProject.domain.core.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByIdAndActive(Long id, Integer active);

    // Filtriranje i paginacija filmova po glumcima
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN MovieActor ma ON ma.movie = m " +
            "WHERE m.active = :active " +
            "AND ma.active = :active " +
            "AND ma.actor.id IN :actorIds")
    Page<Movie> findDistinctByActorsIn(
            @Param("actorIds") List<Long> actorIds,
            @Param("active") Integer active,
            Pageable pageable);

}
