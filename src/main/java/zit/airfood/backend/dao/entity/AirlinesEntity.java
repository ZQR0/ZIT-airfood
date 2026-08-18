package zit.airfood.backend.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "airlines_entity")
@Table(name = "airlines")
@NoArgsConstructor
@Setter
@Getter
public class AirlinesEntity extends AbstractEntity<Integer> implements Serializable {

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "login", nullable = false, unique = true)
    String login;

    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    @OneToMany(mappedBy = "airline", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    List<FlightEntity> flights;

    public AirlinesEntity(String name, String login, String passwordHash) {
        setName(name);
        setLogin(login);
        setPasswordHash(passwordHash);
        this.flights = new ArrayList<>();
    }

    public AirlinesEntity(String name, String login, String passwordHash, List<FlightEntity> flights) {
        setName(name);
        setLogin(login);
        setPasswordHash(passwordHash);
        setFlights(flights);
    }

    public boolean addFlight(FlightEntity flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null");
        }
        return this.flights.add(flight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirlinesEntity that = (AirlinesEntity) o;
        return Objects.equals(login, that.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

}
