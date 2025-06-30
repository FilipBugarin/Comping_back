package hr.demo.demoProject.repository.core;

import hr.demo.demoProject.domain.core.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByIdAndActive(Long id, Integer active);
    List<Actor> findAllByActive(Integer active);
}
