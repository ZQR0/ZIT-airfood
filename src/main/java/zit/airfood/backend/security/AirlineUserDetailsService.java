package zit.airfood.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import zit.airfood.backend.dao.entity.AirlinesEntity;
import zit.airfood.backend.dao.repository.AirlinesRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirlineUserDetailsService implements UserDetailsService {

    private static final String AIRLINE_ROLE = "ROLE_AIRLINE";

    private final AirlinesRepository airlinesRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        AirlinesEntity airline = airlinesRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Airline not found: " + login));
        log.info("Airline entity created");
        return new AirlinesUserDetails(
                airline.getId(),
                airline.getName(),
                airline.getLogin(),
                airline.getPasswordHash()
        );
    }
}
