package pennywise.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AppUserRepository users;
	private final PasswordEncoder passwordEncoder;

	public AuthController(AppUserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest) {
		String email = normalizeEmail(request.email());
		if (users.existsByEmail(email)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthError("An account with this email already exists."));
		}
		AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password())));
		return ResponseEntity.status(HttpStatus.CREATED).body(authenticate(user, servletRequest));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletRequest servletRequest) {
		return users.findByEmail(normalizeEmail(request.email()))
				.filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
				.<ResponseEntity<?>>map(user -> ResponseEntity.ok(authenticate(user, servletRequest)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new AuthError("Email or password is incorrect.")));
	}

	@GetMapping("/me")
	public AuthResponse me(Authentication authentication) {
		return users.findByEmail(authentication.getName()).map(AuthResponse::from).orElseThrow();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		if (request.getSession(false) != null) request.getSession(false).invalidate();
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}

	private AuthResponse authenticate(AppUser user, HttpServletRequest request) {
		request.getSession();
		request.changeSessionId();
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
				user.getEmail(), null, java.util.List.of());
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		new HttpSessionSecurityContextRepository().saveContext(context, request, null);
		return AuthResponse.from(user);
	}

	private String normalizeEmail(String email) { return email.trim().toLowerCase(java.util.Locale.ROOT); }

	public record AuthError(String message) {}
}
