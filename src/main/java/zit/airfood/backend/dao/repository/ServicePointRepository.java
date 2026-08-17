package zit.airfood.backend.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zit.airfood.backend.dao.entity.ServicePointEntity;
import zit.airfood.backend.dto.FinanceReporting.SalesByPointDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ServicePointRepository extends JpaRepository<ServicePointEntity, Integer> {

    List<ServicePointEntity> findByActiveTrue();

    List<ServicePointEntity> findByAirportId(Integer airportId);

    List<ServicePointEntity> findByAirportIdAndActiveTrue(Integer airportId);

    //Возвращает айди точки, название точки, сумму, которую потратили в этой точке и количество транзакций за период
    @Query("""
            SELECT NEW zit.airfood.backend.dto.FinanceReporting.SalesByPointDto(
                 sp.id,
                 sp.name,
                 COALESCE(SUM(t.amount), 0),
                 CAST(COUNT(t.id) AS int)
            )
            FROM service_points_entity sp
            LEFT JOIN transaction_entity t ON t.servicePoint = sp
                AND t.type = 'purchase'
                AND t.createdAt BETWEEN :start AND :end
            GROUP BY sp.id, sp.name 
            ORDER BY sum(t.amount) DESC NULLS LAST   
            """)
    List<SalesByPointDto> moneyPeriod(@Param("start")LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            SELECT NEW zit.airfood.backend.dto.FinanceReporting.SalesByPointDto(
                 sp.id,
                 sp.name,
                 COALESCE(SUM(t.amount), 0),
                 CAST(COUNT(t.id) AS int)
            )
            FROM transaction_entity t
                    JOIN t.ticket tk
                    JOIN tk.flight f
                    JOIN t.servicePoint sp
                    WHERE f.id = :flightId
                      AND t.type = 'purchase'
                    GROUP BY sp.id, sp.name
            """)
    List<SalesByPointDto> moneyFlight(@Param("flightId") int flightId);


    @Query("SELECT s FROM service_points_entity s " +
            "JOIN FETCH airports_entity airportsEntity ON s.airport.id = airportsEntity.id " +
            "WHERE airportsEntity.uniqueCode = :uniqueCode")
    List<ServicePointEntity> findAllByAirportUniqueCode(@Param("uniqueCode") String uniqueCode);
}
