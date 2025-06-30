package hr.demo.demoProject.repository.core;


import hr.demo.demoProject.domain.core.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByIdAndActive(Long id, Integer active);
    List<Movie> findAllByActive(Integer active);
}
