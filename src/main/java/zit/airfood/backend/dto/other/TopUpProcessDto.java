package zit.airfood.backend.dto.other;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUpProcessDto {
    private boolean success;
    private String message;
    private BigDecimal amount;
    private Integer servicePoint;
    private int ticketsAffected;
}