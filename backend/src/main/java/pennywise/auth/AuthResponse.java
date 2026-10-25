package pennywise.auth;

public record AuthResponse(Long id, String email, String currencyCode) {
	public static AuthResponse from(AppUser user) {
		return new AuthResponse(user.getId(), user.getEmail(), user.getCurrencyCode());
	}
}
