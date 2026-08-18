package zit.airfood.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.repository.AirportsRepository;

@Service
@RequiredArgsConstructor
public class AirportsService {

    private final AirportsRepository airportsRepository;
}
