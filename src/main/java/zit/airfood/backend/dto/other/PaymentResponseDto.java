package zit.airfood.backend.dto.other;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private boolean success;
    private String message;
    private String ticketNumber;
    private BigDecimal amount;
    private Integer servicePoint;
    private BigDecimal newBalance;
}