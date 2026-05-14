package com.eps.portal.controlador;

import com.eps.portal.modelo.dto.request.LoginRequest;
import com.eps.portal.modelo.dto.request.RegistroMedicoRequest;
import com.eps.portal.modelo.dto.request.RegistroPacienteRequest;
import com.eps.portal.modelo.dto.response.JwtResponse;
import com.eps.portal.modelo.dto.response.MensajeResponse;
import com.eps.portal.modelo.entity.Usuario;
import com.eps.portal.modelo.repository.UsuarioRepository;
import com.eps.portal.security.JwtUtils;
import com.eps.portal.modelo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 1. Validamos las credenciales contra la base de datos
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // 2. Si es correcto, guardamos la sesiÃ³n
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Generamos el Token JWT
            String jwt = jwtUtils.generarJwtToken(authentication);

            // 4. Extraemos los detalles para la respuesta
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String rol = userDetails.getAuthorities().iterator().next().getAuthority();

            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return ResponseEntity.ok(new JwtResponse(jwt, usuario.getId(), usuario.getEmail(), rol));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensajeResponse("Credenciales incorrectas. Verifique su correo y contraseÃ±a."));
        }
    }

    @PostMapping("/registro/paciente")
    public ResponseEntity<?> registrarPaciente(@Valid @RequestBody RegistroPacienteRequest request) {
        authService.registrarPaciente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeResponse("Paciente registrado exitosamente"));
    }

    @PostMapping("/registro/medico")
    public ResponseEntity<?> registrarMedico(@Valid @RequestBody RegistroMedicoRequest request) {
        authService.registrarMedico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeResponse("MÃ©dico registrado exitosamente"));
    }
}
