package pennywise.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "varchar(3) not null default 'USD'")
	private String currencyCode = "USD";

	protected AppUser() {}

	public AppUser(String email, String passwordHash) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.createdAt = Instant.now();
	}

	public Long getId() { return id; }
	public String getEmail() { return email; }
	public String getPasswordHash() { return passwordHash; }
	public Instant getCreatedAt() { return createdAt; }
	public String getCurrencyCode() { return currencyCode; }
	public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
}
