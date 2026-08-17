package zit.airfood.backend.dao.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.AirlinesEntity;
import zit.airfood.backend.dao.entity.AirportsEntity;
import zit.airfood.backend.dao.entity.FlightEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirlinesRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AirlinesRepository airlinesRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirportsRepository airportsRepository;

    @Test
    void count_returnsTenSeededAirlines() {
        assertThat(airlinesRepository.count()).isEqualTo(10L);
    }

    @Test
    void findById_returnsAeroflot() {
        AirlinesEntity airline = airlinesRepository.findById(1).orElseThrow();
        assertThat(airline.getName()).isEqualTo("Аэрофлот");
        assertThat(airline.getLogin()).isEqualTo("aeroflot");
    }

    @Test
    void findById_returnsEmptyForMissing() {
        assertThat(airlinesRepository.findById(99999)).isEmpty();
    }

    @Test
    void existsById_trueForSeeded_falseForMissing() {
        assertThat(airlinesRepository.existsById(1)).isTrue();
        assertThat(airlinesRepository.existsById(99999)).isFalse();
    }

    @Test
    void findByLogin_returnsAirline() {
        assertThat(airlinesRepository.findByLogin("aeroflot"))
                .isPresent()
                .get()
                .satisfies(a -> assertThat(a.getId()).isEqualTo(1));
    }

    @Test
    void findByLogin_returnsEmptyForUnknown() {
        assertThat(airlinesRepository.findByLogin("nope")).isEmpty();
    }

    @Test
    void save_persistsNewAirline() {
        AirlinesEntity airline = new AirlinesEntity("TestAir", "testair", "hash");
        AirlinesEntity saved = airlinesRepository.save(airline);
        airlinesRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(airlinesRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(AirlinesEntity::getLogin)
                .isEqualTo("testair");
    }

    @Test
    void save_cascadePersistsFlights() {
        AirlinesEntity airline = new AirlinesEntity("CascadeAir", "cascade", "hash");
        AirportsEntity from = airportsRepository.findById(1).orElseThrow();
        AirportsEntity to = airportsRepository.findById(3).orElseThrow();
        FlightEntity flight = new FlightEntity(
                airline, "Boeing", from, to, 0, null,
                ZonedDateTime.parse("2026-07-20T10:00:00+00:00"),
                ZonedDateTime.parse("2026-07-20T08:00:00+00:00")
        );
        airline.addFlight(flight);

        airlinesRepository.saveAndFlush(airline);

        List<FlightEntity> flights = flightRepository.findByAirline(airline);
        assertThat(flights).hasSize(1);
        assertThat(flights.get(0).getAirplane()).isEqualTo("Boeing");
    }
}
