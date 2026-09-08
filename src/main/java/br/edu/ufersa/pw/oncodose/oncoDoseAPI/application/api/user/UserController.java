package br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.common.ResponseDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.AuthRequestDTO;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.AuthenticatedUserResponse;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.application.api.user.dto.UserRegistrationRequest;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.CustomUserDetails;
import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest user) {
        ResponseDTO<?> response = new ResponseDTO<>(userService.register(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO authRequest) {
        ResponseDTO<?> response = new ResponseDTO<>(userService.login(authRequest.username(), authRequest.password()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<AuthenticatedUserResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userAuthentication
    ) {
        return ResponseEntity.ok(
                new ResponseDTO<>(
                        AuthenticatedUserResponse.fromUser(userAuthentication.getUser())
                )
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @AuthenticationPrincipal CustomUserDetails userAuthentication
    ) {
        userService.deleteById(userAuthentication.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        ResponseDTO<?> response = new ResponseDTO<>(userService.getUserById(userId));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteById(userId);
        return ResponseEntity.noContent().build();
    }


}
