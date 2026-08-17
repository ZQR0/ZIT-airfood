package zit.airfood.backend.dao.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import zit.airfood.backend.dao.entity.FlightEntity;
import zit.airfood.backend.dao.entity.PassengerEntity;
import zit.airfood.backend.dao.entity.TicketEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TicketRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void count_returnsTwentyFourSeededTickets() {
        assertThat(ticketRepository.count()).isEqualTo(24L);
    }

    @Test
    void findByTicketNumber_returnsTicket() {
        assertThat(ticketRepository.findByTicketNumber("TKT202607140001"))
                .isPresent()
                .get()
                .extracting(TicketEntity::getBalance)
                .isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void findByTicketNumber_returnsEmptyForUnknown() {
        assertThat(ticketRepository.findByTicketNumber("UNKNOWN")).isEmpty();
    }

    @Test
    void findByPassengerId_returnsTicketsForPassenger() {
        List<TicketEntity> tickets = ticketRepository.findByPassengerId(1);
        assertThat(tickets).hasSize(2);
    }

    @Test
    void findByPassengerId_returnsEmptyForUnknown() {
        assertThat(ticketRepository.findByPassengerId(99999)).isEmpty();
    }

    @Test
    void findByFlightId_returnsTicketsForFlight() {
        List<TicketEntity> tickets = ticketRepository.findByFlightId(20);
        assertThat(tickets).hasSize(2);
    }

    @Test
    void findByFlightIdAndSeat_returnsTicket() {
        assertThat(ticketRepository.findByFlightIdAndSeat(1, "12A"))
                .isPresent()
                .get()
                .extracting(TicketEntity::getId)
                .isEqualTo(1);
    }

    @Test
    void findByFlightIdAndSeat_returnsEmptyForWrongSeat() {
        assertThat(ticketRepository.findByFlightIdAndSeat(1, "99Z")).isEmpty();
    }

    @Test
    void save_persistsNewTicket() {
        FlightEntity flight = entityManager.getReference(FlightEntity.class, 1);
        PassengerEntity passenger = entityManager.getReference(PassengerEntity.class, 1);
        TicketEntity ticket = new TicketEntity(
                new BigDecimal("100.00"),
                flight,
                passenger,
                "99X",
                "TKT-NEW-9999"
        );

        TicketEntity saved = ticketRepository.save(ticket);
        ticketRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(ticketRepository.findByTicketNumber("TKT-NEW-9999")).isPresent();
    }
}
