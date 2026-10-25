package pennywise.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {
	private final AppUserRepository users;

	public PreferenceController(AppUserRepository users) { this.users = users; }

	@PutMapping
	@ResponseStatus(HttpStatus.OK)
	public AuthResponse update(@Valid @RequestBody CurrencyRequest request, Authentication authentication) {
		AppUser user = users.findByEmail(authentication.getName()).orElseThrow();
		user.setCurrencyCode(request.currencyCode());
		return AuthResponse.from(users.save(user));
	}

	public record CurrencyRequest(@NotNull @Pattern(regexp = "USD|CAD|GBP|EUR") String currencyCode) {}
}
