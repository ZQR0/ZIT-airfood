package zit.airfood.backend.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.entity.AirportsEntity;
import zit.airfood.backend.dao.entity.ServicePointEntity;
import zit.airfood.backend.dao.repository.AirportsRepository;
import zit.airfood.backend.dao.repository.ServicePointRepository;
import zit.airfood.backend.dto.servicePoints.ServicePointDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicePointService {

    private final ServicePointRepository servicePointRepository;
    private final AirportsRepository airportsRepository;

    @Transactional
    public ServicePointDto validatePoint(int pointId) {
        ServicePointEntity point = servicePointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Точка обслуживания с id " + pointId + " не найдена"));

        point.setActive(true);
        ServicePointEntity saved = servicePointRepository.save(point);

        return mapToDto(saved);
    }

    public ServicePointEntity findEntityById(int id) {
        return servicePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Точка обслуживания с id " + id + " не найдена"));
    }

    public List<ServicePointDto> findAllByAirportUniqueCode(String uniqueCode) {
        AirportsEntity airportsEntity = this.airportsRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new RuntimeException("Такого аэропорта не существует"));

        return this.servicePointRepository.findAllByAirportUniqueCode(uniqueCode)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private ServicePointDto mapToDto(ServicePointEntity entity) {
        ServicePointDto dto = new ServicePointDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAirportCode(entity.getAirport().getUniqueCode());
        dto.setContactPhone(entity.getContactPhone());
        dto.setActive(entity.isActive());
        return dto;
    }
}
