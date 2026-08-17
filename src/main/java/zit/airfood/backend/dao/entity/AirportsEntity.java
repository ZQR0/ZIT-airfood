package zit.airfood.backend.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "airports_entity")
@Table(name = "airports")
@NoArgsConstructor
@Setter
@Getter
public class AirportsEntity extends AbstractEntity<Integer> implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unique_code", nullable = false, unique = true)
    private String uniqueCode;

    @Column(name = "town")
    private String town;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "airportFrom", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<FlightEntity> departingFlights;

    @OneToMany(mappedBy = "airportTo", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<FlightEntity> arrivingFlights;

    public AirportsEntity(String name, String uniqueCode, String town, String address) {
        setName(name);
        setUniqueCode(uniqueCode);
        setTown(town);
        setAddress(address);
        this.departingFlights = new ArrayList<>();
        this.arrivingFlights = new ArrayList<>();
    }

    public AirportsEntity(String name, String uniqueCode, String town, String address, List<FlightEntity> fromAirports, List<FlightEntity> toAirports) {
        setName(name);
        setUniqueCode(uniqueCode);
        setTown(town);
        setAddress(address);
        setDepartingFlights(fromAirports);
        setArrivingFlights(toAirports);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirportsEntity that = (AirportsEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(uniqueCode, that.uniqueCode) && Objects.equals(town, that.town) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uniqueCode, town, address);
    }
}
