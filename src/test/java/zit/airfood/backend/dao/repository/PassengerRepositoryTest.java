package zit.airfood.backend.dao.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.PassengerEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PassengerRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    void count_returnsTwentySeededPassengers() {
        assertThat(passengerRepository.count()).isEqualTo(20L);
    }

    @Test
    void findByPassportSeriesAndPassportNumber_returnsPassenger() {
        assertThat(passengerRepository.findByPassportSeriesAndPassportNumber("4510", "123456"))
                .isPresent()
                .get()
                .extracting(PassengerEntity::getFirstName)
                .isEqualTo("Иван");
    }

    @Test
    void findByPassportSeriesAndPassportNumber_returnsEmptyForUnknown() {
        assertThat(passengerRepository.findByPassportSeriesAndPassportNumber("9999", "999999")).isEmpty();
    }

    @Test
    void findByFirstNameAndLastNameAndPatronymic_returnsPassenger() {
        List<PassengerEntity> result = passengerRepository
                .findByFirstNameAndLastNameAndPatronymic("Иван", "Иванов", "Иванович");
        assertThat(result).hasSize(1);
    }

    @Test
    void findByFirstNameAndLastNameAndPatronymic_returnsEmptyForWrongPatronymic() {
        List<PassengerEntity> result = passengerRepository
                .findByFirstNameAndLastNameAndPatronymic("Иван", "Иванов", "Петрович");
        assertThat(result).isEmpty();
    }

    @Test
    void findByFirstNameAndLastName_returnsAllWithSameName() {
        List<PassengerEntity> result = passengerRepository
                .findByFirstNameAndLastName("Иван", "Иванов");
        assertThat(result).isNotEmpty();
    }

    @Test
    void save_persistsNewPassenger() {
        PassengerEntity passenger = new PassengerEntity("Новый", "Тестов", "Тестович", "7777", "888888");
        PassengerEntity saved = passengerRepository.save(passenger);
        passengerRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(passengerRepository.findByPassportSeriesAndPassportNumber("7777", "888888"))
                .isPresent();
    }
}
