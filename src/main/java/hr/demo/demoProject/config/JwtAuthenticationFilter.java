package hr.demo.demoProject.config;

import hr.demo.demoProject.services.DemoProjectUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final DemoProjectUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain) throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    // Bypass JWT validation for refresh token endpoint
    if (requestURI.contains("/api/auth/refresh")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");
    String jwt = null;
    String username = null;

    // Extract JWT from Authorization Header
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      jwt = authHeader.substring(7);
    } else {
      // Fallback to extracting JWT from cookies if Authorization header is missing
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if ("Jwt".equals(cookie.getName())) {
            jwt = cookie.getValue();
            break;
          }
        }
      }
    }

    // If no JWT found, continue request
    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      if (jwtService.isTokenValid(jwt)) {
        username = jwtService.extractUsername(jwt);
      }
    } catch (ExpiredJwtException e) {
      log.warn("Expired JWT: {}", jwt);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
      return;
    } catch (MalformedJwtException e) {
      log.warn("Malformed JWT: {}", jwt);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
      return;
    }

    // Authenticate user if JWT is valid and no authentication is present
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities()
      );
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
  }

}
