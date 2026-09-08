package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.UserRegistrationRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth.AuthService;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.profile.Profile;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth.Auth;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role.Role;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role.RoleRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.jwt.JwtGenerator;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.services.utils.DocumentValidatorUtil;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.services.utils.StringSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;



    @Transactional
    public User register(UserRegistrationRequest user) {
        String sanitizedUsername = StringSanitizer.sanitizeString(user.getUsername());

        if (userRepository.existsByAuthUsername(sanitizedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O nome de usuário já esta em uso!");
        }

        User newUser = new User();

        if(user.getStatus() != null)
            newUser.setStatus(user.getStatus());

        Profile profile = new Profile();
        profile.setName(user.getName());
        profile.setDocument(user.getDocument());
        profile.setPhone(user.getPhone());
        profile.setBirthDate(user.getBirthDate());
        newUser.setProfile(profile);

        Auth authRequest = new Auth();
        authRequest.setUsername(sanitizedUsername);
        authRequest.setPassword(user.getPassword());
        newUser.setAuth(authRequest);

        Role role = roleRepository.findByName(user.getRole().getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role não encontrada!"));
        newUser.setRole(role);

        return save(newUser);
    }

    @Transactional
    public User save(User user) {
        if (user.getProfile() != null && user.getProfile().getDocument() != null) {
            String document = user.getProfile().getDocument();
            if (document.length() == 11 && !new DocumentValidatorUtil().checkCpf(document)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento inválido!");
            } else if (document.length() == 14 && !new DocumentValidatorUtil().checkCnpj(document)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento inválido!");
            }
        }
        try {
            Auth auth = authService.createAuth(user.getAuth().getUsername(), user.getAuth().getPassword()).getBody();
            user.setAuth(auth);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Erro ao criar autenticação: " + e.getReason());
        }
        Optional<User> userSaved = Optional.ofNullable(userRepository.save(user));

        if (!userSaved.isPresent()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar usuário!");
        }

        return userSaved.get();
    }

    @Transactional
    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            return jwtGenerator.generateToken(authentication);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
    }

    public User getUserById (UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado!"));
    }

    @Transactional
    public void deleteById(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado!"
            );
        }

        userRepository.deleteById(userId);
    }



//    public Optional<User> findById(UUID id) {
//        return userRepository.findById(id);
//    }
//
//    public Optional<User> findByUsername(String username) {
//        return userRepository.findByAuthUsername(username);
//    }
//
//    public List<User> findAll() {
//        return userRepository.findAll();
//    }
//
//    @Transactional
//    public void changePassword(UUID id, String rawPassword) {
//        User user = requireUser(id);
//        user.getAuth().setPassword(passwordEncoder.encode(rawPassword));
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public void setStatus(UUID id, UserStatus status) {
//        User user = requireUser(id);
//        user.setStatus(status);
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public void assignRole(UUID id, Role role) {
//        User user = requireUser(id);
//        user.setRole(role);
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public void addPrivileges(UUID id, List<Privilege> privileges) {
//        User user = requireUser(id);
//        if (user.getPrivileges() == null) {
//            user.setPrivileges(new ArrayList<>());
//        }
//        privileges.forEach(p -> {
//            if (!user.getPrivileges().contains(p)) {
//                user.getPrivileges().add(p);
//            }
//        });
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public void removePrivilege(UUID id, Privilege privilege) {
//        User user = requireUser(id);
//        if (user.getPrivileges() != null) {
//            user.getPrivileges().remove(privilege);
//        }
//        userRepository.save(user);
//    }
//
//    private User requireUser(UUID id) {
//        return userRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
//    }
}
