package pennywise.category;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pennywise.auth.AppUser;
import pennywise.auth.AppUserRepository;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
	private static final List<String> DEFAULTS = List.of("Food & drink", "Transport", "Shopping", "Home", "Health", "Other");
	private final UserCategoryRepository categories;
	private final AppUserRepository users;

	public CategoryController(UserCategoryRepository categories, AppUserRepository users) {
		this.categories = categories;
		this.users = users;
	}

	@GetMapping
	public List<UserCategory> list(Authentication authentication) {
		AppUser user = currentUser(authentication);
		if (categories.findAllByOwner_IdOrderByNameAsc(user.getId()).isEmpty()) {
			categories.saveAll(DEFAULTS.stream().map(name -> new UserCategory(name, user)).toList());
		}
		return categories.findAllByOwner_IdOrderByNameAsc(user.getId());
	}

	@PostMapping
	public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest request, Authentication authentication) {
		AppUser user = currentUser(authentication);
		String name = request.name().trim();
		if (categories.existsByOwner_IdAndNameIgnoreCase(user.getId(), name)) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new CategoryError("That category already exists."));
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(categories.save(new UserCategory(name, user)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
		Long ownerId = currentUser(authentication).getId();
		var category = categories.findByIdAndOwner_Id(id, ownerId);
		if (category.isEmpty()) return ResponseEntity.notFound().build();
		if (categories.findAllByOwner_IdOrderByNameAsc(ownerId).size() <= 1) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		categories.delete(category.get());
		return ResponseEntity.noContent().build();
	}

	private AppUser currentUser(Authentication authentication) {
		return users.findByEmail(authentication.getName()).orElseThrow();
	}

	public record CategoryError(String message) {}
}
