package zit.airfood.backend.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity(name = "service_points_entity")
@Table(name = "service_points")
@NoArgsConstructor
@Setter
@Getter
public class ServicePointEntity extends AbstractEntity<Integer> implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id", nullable = false)
    private AirportsEntity airport;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "is_active")
    private boolean active;

    public ServicePointEntity(boolean active, AirportsEntity airport, String contactPhone, String name) {
        setActive(active);
        setName(name);
        setAirport(airport);
        setContactPhone(contactPhone);
    }
}
