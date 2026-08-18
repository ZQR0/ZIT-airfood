package zit.airfood.backend.dao.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.AirportsEntity;
import zit.airfood.backend.dao.entity.ServicePointEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServicePointRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ServicePointRepository servicePointRepository;

    @Autowired
    private AirportsRepository airportsRepository;

    @Test
    void count_returnsTwentySeededServicePoints() {
        assertThat(servicePointRepository.count()).isEqualTo(21L);
    }

    @Test
    void findByActiveTrue_returnsOnlyActive() {
        List<ServicePointEntity> active = servicePointRepository.findByActiveTrue();
        assertThat(active).hasSize(19);
        assertThat(active).allMatch(ServicePointEntity::isActive);
    }

    @Test
    void findByAirportId_returnsAllServicePointsAtAirport() {
        List<ServicePointEntity> atSvo = servicePointRepository.findByAirportId(1);
        assertThat(atSvo).hasSize(3);
    }

    @Test
    void findByAirportId_returnsEmptyForUnknownAirport() {
        assertThat(servicePointRepository.findByAirportId(99999)).isEmpty();
    }

    @Test
    void findByAirportIdAndActiveTrue_returnsActiveOnesAtAirport() {
        List<ServicePointEntity> activeAtSvo = servicePointRepository.findByAirportIdAndActiveTrue(1);
        assertThat(activeAtSvo).hasSize(3);
        assertThat(activeAtSvo).allMatch(ServicePointEntity::isActive);
    }

    @Test
    void save_persistsNewServicePoint() {
        AirportsEntity airport = airportsRepository.findById(1).orElseThrow();
        ServicePointEntity sp = new ServicePointEntity(true, airport, "+7 (000) 000-00-00", "TestCafe");
        ServicePointEntity saved = servicePointRepository.save(sp);
        servicePointRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(servicePointRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(ServicePointEntity::getName)
                .isEqualTo("TestCafe");
    }
}
