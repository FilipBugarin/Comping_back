package hr.demo.demoProject.repository.core;

import hr.demo.demoProject.domain.core.UserSession;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserSessionRepository extends CrudRepository<UserSession, Long> {

    @Transactional
    @Modifying
    @Query(value = LOGOUT_USER)
    void logoutUserByJwt(@Param("token") String token);

    @Query(value = "FROM UserSession us WHERE us.logoutDate is NULL AND us.authCookieExpirationDate > CURRENT_TIMESTAMP")
    Optional<UserSession> findActiveByAuthCookie(String authCookie);

    @Query("FROM UserSession us WHERE us.logoutDate IS NULL AND us.authCookie = :refreshToken")
    Optional<UserSession> findActiveByRefreshToken(@Param("refreshToken") String refreshToken);

    String LOGOUT_USER = """ 
            UPDATE
                UserSession us
            SET
                us.logoutDate = CURRENT_TIMESTAMP
            WHERE
                us.authCookie = :token
            """;
}