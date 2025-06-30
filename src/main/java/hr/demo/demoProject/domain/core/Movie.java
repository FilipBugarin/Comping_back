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
@Table(name = "movie",schema = "core")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Movie extends AbstractDomain {

    private String name;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieActor> actorsXmovies = new HashSet<>();
}
