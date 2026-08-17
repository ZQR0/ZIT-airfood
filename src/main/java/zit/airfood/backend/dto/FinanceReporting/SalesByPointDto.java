package zit.airfood.backend.dto.FinanceReporting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesByPointDto {
    private Integer point_id;
    private String point_name;
    private BigDecimal sum_trans;
    private Integer count_trans;
}
