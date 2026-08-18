package zit.airfood.backend.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity(name = "ticket_entity")
@Table(name = "ticket")
@NoArgsConstructor
@Setter
@Getter
public class TicketEntity extends AbstractEntity<Integer> implements Serializable {

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private FlightEntity flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private PassengerEntity passenger;

    @Column(name = "seat", nullable = false)
    private String seat;

    @Column(name = "balance", precision = 10, scale = 2)
    private BigDecimal balance;

    public TicketEntity(BigDecimal balance, FlightEntity flight, PassengerEntity passenger, String seat, String ticketNumber) {
        setTicketNumber(ticketNumber);
        setFlight(flight);
        setPassenger(passenger);
        setSeat(seat);
        setBalance(balance);
    }
}
