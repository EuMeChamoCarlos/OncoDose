package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> user = userRepository.findByAuthUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado");
        }

        return new CustomUserDetails(user.get());
    }
}
