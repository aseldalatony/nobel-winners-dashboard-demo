package trials.java.spring.nobelwinners.dashboard.demo.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class RawWinner {
    @Id
    private String id;
    private String firstname;
    private String surname;
    private String born;
    private String died;
    private String bornCountry;
    private String bornCountryCode;
    private String bornCity;
    private String diedCountry;
    private String diedCountryCode;
    private String diedCity;
    private String gender;
    private String year;
    private String category;
    private String overallMotivation;
    private String share;
    @Column(length = 512)
    private String motivation;
    private String name;
    private String city;
    private String country;
}
