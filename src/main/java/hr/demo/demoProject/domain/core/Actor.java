package hr.demo.demoProject.domain.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "actor",schema = "core")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Actor extends AbstractDomain {

    private String description;

    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieActor> moviesXActors = new HashSet<>();
}