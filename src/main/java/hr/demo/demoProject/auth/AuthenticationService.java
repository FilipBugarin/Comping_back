package hr.demo.demoProject.auth;

import hr.demo.demoProject.api.model.AuthenticationRequest;
import hr.demo.demoProject.api.model.AuthenticationResponse;
import hr.demo.demoProject.config.JwtService;
import hr.demo.demoProject.config.exception.DemoProjectNotFoundException;
import hr.demo.demoProject.constants.ProjectErrorMessagesConstants;
import hr.demo.demoProject.domain.ProjectAuthUser;
import hr.demo.demoProject.domain.core.User;
import hr.demo.demoProject.domain.core.UserSession;
import hr.demo.demoProject.config.TokenData;
import hr.demo.demoProject.repository.core.UserSessionRepository;
import hr.demo.demoProject.services.AbstractService;
import hr.demo.demoProject.services.UserService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthenticationService extends AbstractService {
    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        ProjectAuthUser authUser = userService.loginPlainUser(request.getUsername());
        authUser.setJwtToken(jwtService.generateToken(authUser.getUsername()));
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(usernamePasswordAuthenticationToken);

        Cookie cookie = new Cookie("rt", authUser.getRefreshToken().getToken());
        cookie.setMaxAge(JwtService.REFRESH_TOKEN_VALID_TIME_SECONDS);
        cookie.setPath("/");
        getResponse().addCookie(cookie);

        log.info("User: {} loged at time: {}, token: {} ", authUser.getUsername(), authUser.getJwtToken().getCreated(), authUser.getJwtToken().getToken());

        return AuthenticationResponse.builder()
                .userId(authUser.getUserId())
                .token(authUser.getJwtToken().getToken())
                .expiresAt(authUser.getJwtToken().getExpiresAt())
                .refreshToken(authUser.getRefreshToken().getToken())
                .refreshTokenExpiresAt(authUser.getRefreshToken().getExpiresAt())
                .username(authUser.getUsername())
                .roles(authUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build();
    }

    public AuthenticationResponse refreshToken(AuthenticationRequest authenticationRequest) {
        String refreshToken = authenticationRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new DemoProjectNotFoundException("Refresh token is missing");
        }

        UserSession us = userSessionRepository.findActiveByRefreshToken(refreshToken).orElseThrow(() -> new DemoProjectNotFoundException(ProjectErrorMessagesConstants.FIND_USER_SESSION_ERROR));

        User user = us.getUser();
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (!us.getAuthCookieExpirationDate().isBefore(LocalDateTime.now())) {
            ProjectAuthUser authUser = new ProjectAuthUser(user.getUsername(), "", authorities);
            TokenData newAccessToken = jwtService.generateToken(user.getUsername());
            TokenData newRefreshToken = jwtService.getRefreshToken(user.getUsername());

            us = UserSession.builder()
                    .user(user)
                    .loginDate(newRefreshToken.getCreated())
                    .authCookie(newRefreshToken.getToken())
                    .ipAddress(getClientIpAddress())
                    .authCookieCreationDate(newRefreshToken.getCreated())
                    .authCookieExpirationDate(newRefreshToken.getExpiresAt())
                    .build();

            userSessionRepository.save(us);

            authUser.setJwtToken(newAccessToken);
            authUser.setRefreshToken(newRefreshToken);

            return AuthenticationResponse.builder()
                    .userId(authUser.getUserId())
                    .token(authUser.getJwtToken().getToken())
                    .expiresAt(authUser.getJwtToken().getExpiresAt())
                    .refreshToken(authUser.getRefreshToken().getToken())
                    .refreshTokenExpiresAt(authUser.getRefreshToken().getExpiresAt())
                    .username(authUser.getUsername())
                    .roles(authUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .build();
        }

        return null;
    }
}
