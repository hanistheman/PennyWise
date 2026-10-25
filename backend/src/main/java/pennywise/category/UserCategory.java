package pennywise.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import pennywise.auth.AppUser;

@Entity
@Table(name = "user_categories", uniqueConstraints = @UniqueConstraint(name = "uk_user_category_name", columnNames = { "user_id", "name" }))
public class UserCategory {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 40)
	private String name;

	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser owner;

	protected UserCategory() {}

	public UserCategory(String name, AppUser owner) { this.name = name; this.owner = owner; }
	public Long getId() { return id; }
	public String getName() { return name; }
}
