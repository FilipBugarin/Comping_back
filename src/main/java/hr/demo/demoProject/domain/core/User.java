package hr.demo.demoProject.domain.core;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user",schema = "core")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class User extends AbstractDomain{
    private String username;
}
