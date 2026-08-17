package zit.airfood.backend.dto.FinanceReporting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelayReasonDto {
    private String reason;
    private Long count;
}