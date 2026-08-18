package zit.airfood.backend.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity(name = "flight_entity")
@Table(name = "flight")
@NoArgsConstructor
@Setter
@Getter
public class FlightEntity extends AbstractEntity<Integer> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private AirlinesEntity airline;

    @Column(name = "airplane", length = 100)
    private String airplane;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_from", nullable = false)
    private AirportsEntity airportFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_to", nullable = false)
    private AirportsEntity airportTo;

    @Column(name = "time_out", nullable = false)
    private ZonedDateTime timeOut;

    @Column(name = "time_in", nullable = false)
    private ZonedDateTime timeIn;

    @Column(name = "delay_minutes", nullable = false)
    private Integer delayMinutes;

    @Column(name = "reason_delay")
    private String reasonDelay;

    public FlightEntity(AirlinesEntity airline,
                        String airplane,
                        AirportsEntity airportFrom,
                        AirportsEntity airportTo,
                        Integer delayMinutes,
                        String reasonDelay,
                        ZonedDateTime timeIn,
                        ZonedDateTime timeOut) {
        this.airline = airline;
        this.airplane = airplane;
        this.airportFrom = airportFrom;
        this.airportTo = airportTo;
        this.delayMinutes = delayMinutes;
        this.reasonDelay = reasonDelay;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }
}

