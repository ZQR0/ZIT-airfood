package zit.airfood.backend.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.CharJdbcType;

import java.io.Serializable;

@Entity(name = "passenger_entity")
@Table(name = "passenger")
@NoArgsConstructor
@Setter
@Getter
public class PassengerEntity extends AbstractEntity<Integer> implements Serializable {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "passport_series", nullable = false, length = 4)
    @JdbcType(CharJdbcType.class)
    private String passportSeries;

    @Column(name = "passport_number", nullable = false, length = 6)
    @JdbcType(CharJdbcType.class)
    private String passportNumber;

    public PassengerEntity(String firstName, String lastName, String patronymic, String passportSeries, String passportNumber) {
        setFirstName(firstName);
        setLastName(lastName);
        setPatronymic(patronymic);
        setPassportSeries(passportSeries);
        setPassportNumber(passportNumber);
    }

    public PassengerEntity(String passportSeries, String passportNumber, String lastName, String firstName) {
        setFirstName(firstName);
        setLastName(lastName);
        setPassportSeries(passportSeries);
        setPassportNumber(passportNumber);
    }
}
