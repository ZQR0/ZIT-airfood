package zit.airfood.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.entity.AirlinesEntity;
import zit.airfood.backend.dao.repository.AirlinesRepository;
import zit.airfood.backend.dto.airlines.AirlinesDto;

@Service
@RequiredArgsConstructor
public class AirlinesService {

    private final AirlinesRepository airlinesRepository;

    public AirlinesDto findById(int id) {
        AirlinesEntity entity = airlinesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авиакомпания с id " + id + " не найдена"));
        return mapToDto(entity);
    }

    public AirlinesEntity findEntityById(int id) {
        return airlinesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авиакомпания с id " + id + " не найдена"));
    }

    private AirlinesDto mapToDto(AirlinesEntity entity) {
        AirlinesDto dto = new AirlinesDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLogin(entity.getLogin());
        return dto;
    }
}
