package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth.Auth;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role.Role;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldExposeSecurityDetailsFromUserState() {
        Auth auth = new Auth();
        auth.setUsername("admin@oncodose.com");
        auth.setPassword("encoded-password");

        Role role = new Role();
        role.setName("ADMIN");

        User user = new User();
        user.setAuth(auth);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        assertThat(user.getUsername()).isEqualTo("admin@oncodose.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void shouldDisableInactiveUser() {
        User user = new User();
        user.setStatus(UserStatus.INACTIVE);

        assertThat(user.isEnabled()).isFalse();
    }
}
