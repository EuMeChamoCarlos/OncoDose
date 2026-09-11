package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.usecases.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.UserRegistrationRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth.Auth;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth.AuthService;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.DocumentoInvalidoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoDuplicadoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.exception.RecursoNaoEncontradoException;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.profile.Profile;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role.Role;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.role.RoleRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.User;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.UserRepository;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.services.utils.DocumentValidatorUtil;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.services.utils.StringSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: registrar usuário. Único dono de @Transactional na escrita.
 */
@Service
public class RegistrarUsuarioUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;

    public RegistrarUsuarioUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authService = authService;
    }

    @Transactional
    public User executar(UserRegistrationRequest request) {
        String sanitizedUsername = StringSanitizer.sanitizeString(request.getUsername());

        if (userRepository.existsByAuthUsername(sanitizedUsername)) {
            throw new RecursoDuplicadoException("Usuário", "username", sanitizedUsername);
        }

        User newUser = new User();

        if (request.getStatus() != null) {
            newUser.setStatus(request.getStatus());
        }

        Profile profile = new Profile();
        profile.setName(request.getName());
        profile.setDocument(request.getDocument());
        profile.setPhone(request.getPhone());
        profile.setBirthDate(request.getBirthDate());
        newUser.setProfile(profile);

        if (profile.getDocument() != null) {
            String document = profile.getDocument();
            if (document.length() == 11 && !new DocumentValidatorUtil().checkCpf(document)) {
                throw new DocumentoInvalidoException(document);
            } else if (document.length() == 14 && !new DocumentValidatorUtil().checkCnpj(document)) {
                throw new DocumentoInvalidoException(document);
            }
        }

        Auth authRequest = new Auth();
        authRequest.setUsername(sanitizedUsername);
        authRequest.setPassword(request.getPassword());
        newUser.setAuth(authRequest);

        Role role = roleRepository.findByName(request.getRole().getName())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Role", request.getRole().getName()));
        newUser.setRole(role);

        Auth auth = authService.createAuth(
                newUser.getAuth().getUsername(), newUser.getAuth().getPassword());
        newUser.setAuth(auth);

        return userRepository.save(newUser);
    }
}
