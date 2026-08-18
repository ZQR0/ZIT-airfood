package zit.airfood.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AirlinesUserDetails implements UserDetails {

    private static final String AIRLINE_ROLE = "ROLE_AIRLINE";

    private Integer id;
    private String name;
    private String login;
    private String passwordHash;
    private Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(AIRLINE_ROLE));

    public AirlinesUserDetails(Integer id, String name, String login, String passwordHash) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.login;
    }
}
