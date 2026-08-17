package zit.airfood.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.repository.PassengerRepository;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
}
