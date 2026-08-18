package zit.airfood.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zit.airfood.backend.dto.FinanceReporting.SalesByPointDto;
import zit.airfood.backend.service.AnalyticsService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Отчетность и аналитика", description = "API для бизнес-показателей и статистики")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Общая выручка за период",
            description = "Возвращает общую выручку по точкам, а так же уникальный идентификатор каждой точки, и количество транзакций")
    @ApiResponse(responseCode = "200", description = "Статистика успешно получена")
    @GetMapping("/sales/by-points")
    public ResponseEntity<List<SalesByPointDto>> getSalesByPoints(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime start,
                                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") LocalDateTime end) {
        return ResponseEntity.ok(analyticsService.getSalesByPoints(start, end));
    }
}
