package zit.airfood.backend.dto.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequestsDto {
    private BigDecimal amount;
    private Integer servicePointId;
}
