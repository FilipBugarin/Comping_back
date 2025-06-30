package hr.demo.demoProject.services;

import hr.demo.demoProject.config.JwtService;
import hr.demo.demoProject.config.exception.DemoProjectNotFoundException;
import hr.demo.demoProject.constants.ProjectConstants;
import hr.demo.demoProject.constants.ProjectErrorMessagesConstants;
import hr.demo.demoProject.domain.ProjectAuthUser;
import hr.demo.demoProject.domain.core.User;
import hr.demo.demoProject.domain.core.UserSession;
import hr.demo.demoProject.repository.core.UserRepository;
import hr.demo.demoProject.repository.core.UserSessionRepository;

import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Log4j2
@Service
public class UserService extends AbstractService {
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    private final JwtService jwtService;

    @Transactional
    public ProjectAuthUser loginPlainUser(String username) {
        return getDemoProjectUser(username, true, false, null);
    }

    public ProjectAuthUser getDemoProjectUser(String username, boolean persistLogin, boolean isNiasLogin, UserSession userSession) {
        User user = userRepository.findActiveUserByUsername(username, ProjectConstants.STATUS_ACTIVE).orElseThrow(()->new DemoProjectNotFoundException(ProjectErrorMessagesConstants.FIND_USER_ERROR));

        List<GrantedAuthority> authorities = new ArrayList<>();

        ProjectAuthUser authUser = new ProjectAuthUser(user.getUsername(), "", authorities);
        authUser.setEmployeeId(user.getId());
        authUser.setUserId(user.getId());
        authUser.setIpAddress(getClientIpAddress());

        authUser.setJwtToken(jwtService.generateToken(authUser.getUsername()));

        if (persistLogin) {
            persistUserLogin(authUser, user, true, true);
        }

        return authUser;
    }

    private void persistUserLogin(ProjectAuthUser authUser, User user, final boolean insert, final boolean newRefreshToken) {
        UserSession us = null;

        if (!insert) {
            us = userSessionRepository.findActiveByAuthCookie(authUser.getRefreshToken().getToken()).orElseThrow(()-> new DemoProjectNotFoundException(ProjectErrorMessagesConstants.FIND_USER_SESSION_ERROR));
        }
        if (newRefreshToken) {
            authUser.setRefreshToken(jwtService.getRefreshToken(authUser.getUsername()));
        }
        if (insert) {
            us = UserSession.builder()
                    .user(user)
                    .loginDate(authUser.getRefreshToken().getCreated())
                    .authCookie(authUser.getRefreshToken().getToken())
                    .ipAddress(authUser.getIpAddress())
                    .authCookieCreationDate(authUser.getRefreshToken().getCreated())
                    .authCookieExpirationDate(authUser.getRefreshToken().getExpiresAt())
                    .build();
        } else {
            us.setAuthCookie(authUser.getRefreshToken().getToken());
            us.setAuthCookieCreationDate(authUser.getRefreshToken().getCreated());
            us.setModifiedDate(LocalDateTime.now());
        }
        userSessionRepository.save(us);

    }

    public void logoutUser(String jwt) {
        userSessionRepository.logoutUserByJwt(jwt);
    }

}
