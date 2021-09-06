package trials.java.spring.nobelwinners.dashboard.demo.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Setter
@Getter
public class Winner {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String firstName;
    private String surName;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate birthDate;
    private String birthCountry;
}
