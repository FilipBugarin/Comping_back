package hr.demo.demoProject.auth;

import hr.demo.demoProject.api.AuthApi;
import hr.demo.demoProject.api.model.AuthenticationRequest;
import hr.demo.demoProject.api.model.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthApiControllerImpl implements AuthApi {
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @Override
    public ResponseEntity<AuthenticationResponse> refresh(AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.refreshToken(authenticationRequest));
    }
}
