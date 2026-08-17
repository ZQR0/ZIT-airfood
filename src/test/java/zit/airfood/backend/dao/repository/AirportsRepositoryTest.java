package zit.airfood.backend.dao.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.AirportsEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirportsRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private AirportsRepository airportsRepository;

    @Test
    void count_returnsFourteenSeededAirports() {
        assertThat(airportsRepository.count()).isEqualTo(15L);
    }

    @Test
    void findByUniqueCode_returnsSheremetyevo() {
        assertThat(airportsRepository.findByUniqueCode("SVO"))
                .isPresent()
                .get()
                .extracting(AirportsEntity::getTown)
                .isEqualTo("Москва");
    }

    @Test
    void findByUniqueCode_returnsEmptyForUnknown() {
        assertThat(airportsRepository.findByUniqueCode("XYZ")).isEmpty();
    }

    @Test
    void existsByUniqueCode_trueForSVO_falseForXYZ() {
        assertThat(airportsRepository.existsByUniqueCode("SVO")).isTrue();
        assertThat(airportsRepository.existsByUniqueCode("XYZ")).isFalse();
    }

    @Test
    void findByTown_returnsMoscowAirports() {
        List<AirportsEntity> moscow = airportsRepository.findByTown("Москва");
        assertThat(moscow).hasSize(2);
        assertThat(moscow).extracting(AirportsEntity::getUniqueCode)
                .containsExactlyInAnyOrder("SVO", "DME");
    }

    @Test
    void findByTown_returnsEmptyForUnknownTown() {
        assertThat(airportsRepository.findByTown("НетТакогоГорода")).isEmpty();
    }

    @Test
    void save_persistsNewAirport() {
        AirportsEntity airport = new AirportsEntity("Новый аэропорт", "NEW", "Новгород", "ул. Тестовая, 1");
        AirportsEntity saved = airportsRepository.save(airport);
        airportsRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(airportsRepository.findByUniqueCode("NEW"))
                .isPresent()
                .get()
                .extracting(AirportsEntity::getId)
                .isEqualTo(saved.getId());
    }
}
