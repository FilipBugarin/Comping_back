package hr.demo.demoProject.repository.core;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hr.demo.demoProject.domain.core.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query(value = FIND_ACTIVE_USER_BY_USERNAME)
    Optional<User> findActiveUserByUsername(@Param("username") String username, @Param("status") Integer status);

    String FIND_ACTIVE_USER_BY_USERNAME = """            
            FROM
                User u
            WHERE
                u.username like :username AND
                u.active = :status
            """;

}