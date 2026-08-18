package zit.airfood.backend.dto.flights;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentDelayDto {
    private Integer flightId;
    private String delayFormatted;
    private Long delayMinutes;
}
