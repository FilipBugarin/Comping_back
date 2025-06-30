package hr.demo.demoProject.domain;

import hr.demo.demoProject.config.TokenData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class ProjectAuthUser extends User {

    private String ipAddress;
    private TokenData jwtToken;

    private TokenData refreshToken;

    private String firstname;

    private String lastname;

    private String pin;

    private Long userId;

    private Long employeeId;

    public ProjectAuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

}
