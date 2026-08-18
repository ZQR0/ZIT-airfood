package zit.airfood.backend.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zit.airfood.backend.dao.entity.AirlinesEntity;

import java.util.Optional;

public interface AirlinesRepository extends JpaRepository<AirlinesEntity, Integer> {

    Optional<AirlinesEntity> findByLogin(String login);
}
