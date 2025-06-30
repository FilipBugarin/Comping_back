package hr.demo.demoProject.domain.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_session",schema = "core")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false)
    private User user;

    private LocalDateTime loginDate;
    private LocalDateTime logoutDate;
    private String ipAddress;
    private String authCookie;
    private LocalDateTime authCookieCreationDate;
    private LocalDateTime authCookieExpirationDate;
    private LocalDateTime modifiedDate;

}
