package zit.airfood.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.repository.FlightRepository;
import zit.airfood.backend.dao.repository.ServicePointRepository;
import zit.airfood.backend.dto.FinanceReporting.DelayReasonDto;
import zit.airfood.backend.dto.FinanceReporting.SalesByPointDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ServicePointRepository servicePointRepository;
    private final FlightRepository flightRepository;

    public List<SalesByPointDto> getSalesByPoints(LocalDateTime start, LocalDateTime end) {
        return servicePointRepository.moneyPeriod(start, end);
    }

    public List<SalesByPointDto> getSalesByFlight(int flightId) {
        return servicePointRepository.moneyFlight(flightId);
    }

    public Integer averageDelayByPeriod(ZonedDateTime start, ZonedDateTime end) {
        return flightRepository.averageDelayByPeriod(start, end);
    }

    public BigDecimal percentOnTimeByAirline(int airlineId) {
        BigDecimal percent = flightRepository.percentOnTimeByAirline(airlineId);

        if (percent.compareTo(new BigDecimal(100)) > 0 || percent.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invalid percent value");
        }

        return percent;
    }

    public List<DelayReasonDto> mostCommonDelayReasons() {
        return flightRepository.mostCommonDelayReasons();
    }
}
