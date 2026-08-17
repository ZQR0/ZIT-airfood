package zit.airfood.backend.dao.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.AirlinesEntity;
import zit.airfood.backend.dao.entity.AirportsEntity;
import zit.airfood.backend.dao.entity.FlightEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlightRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirlinesRepository airlinesRepository;

    @Autowired
    private AirportsRepository airportsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void count_returnsTwentySeededFlights() {
        assertThat(flightRepository.count()).isEqualTo(20L);
    }

    @Test
    void findById_returnsSeededFlight() {
        FlightEntity flight = flightRepository.findById(1).orElseThrow();
        assertThat(flight.getAirplane()).isEqualTo("Airbus A320");
    }

    @Test
    void findByAirline_returnsFlightsForAirline() {
        AirlinesEntity airline = airlinesRepository.findById(1).orElseThrow();
        List<FlightEntity> flights = flightRepository.findByAirline(airline);
        assertThat(flights).hasSize(3);
    }

    @Test
    void findByAirline_returnsEmptyForAirlineWithNoFlights() {
        AirlinesEntity detached = entityManager.getReference(AirlinesEntity.class, 99999);
        assertThat(flightRepository.findByAirline(detached)).isEmpty();
    }

    @Test
    void findByAirportFrom_returnsFlights() {
        AirportsEntity svo = airportsRepository.findByUniqueCode("SVO").orElseThrow();
        List<FlightEntity> flights = flightRepository.findByAirportFrom(svo);
        assertThat(flights).isNotEmpty();
    }

    @Test
    void findByAirportFrom_returnsEmptyForUnusedAirport() {
        AirportsEntity detached = entityManager.getReference(AirportsEntity.class, 99999);
        assertThat(flightRepository.findByAirportFrom(detached)).isEmpty();
    }

    @Test
    void findByAirportTo_returnsFlights() {
        AirportsEntity svo = airportsRepository.findByUniqueCode("SVO").orElseThrow();
        List<FlightEntity> flights = flightRepository.findByAirportTo(svo);
        assertThat(flights).isNotEmpty();
    }

    @Test
    void save_persistsNewFlight() {
        AirlinesEntity airline = airlinesRepository.findById(1).orElseThrow();
        AirportsEntity from = airportsRepository.findById(1).orElseThrow();
        AirportsEntity to = airportsRepository.findById(3).orElseThrow();
        FlightEntity flight = new FlightEntity(
                airline, "TestPlane", from, to, 0, null,
                ZonedDateTime.parse("2026-07-25T12:00:00+00:00"),
                ZonedDateTime.parse("2026-07-25T10:00:00+00:00")
        );

        FlightEntity saved = flightRepository.save(flight);
        flightRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(flightRepository.findById(saved.getId())).isPresent();
    }
}
