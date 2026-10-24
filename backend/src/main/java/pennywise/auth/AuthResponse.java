package pennywise.auth;

public record AuthResponse(Long id, String email) {
	public static AuthResponse from(AppUser user) {
		return new AuthResponse(user.getId(), user.getEmail());
	}
}
