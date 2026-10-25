package pennywise.recurring;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pennywise.auth.AppUser;
import pennywise.auth.AppUserRepository;

@RestController
@RequestMapping("/api/recurring-expenses")
public class RecurringExpenseController {
	private final RecurringExpenseRepository recurringExpenses;
	private final AppUserRepository users;

	public RecurringExpenseController(RecurringExpenseRepository recurringExpenses, AppUserRepository users) {
		this.recurringExpenses = recurringExpenses;
		this.users = users;
	}

	@GetMapping
	public List<RecurringExpense> list(Authentication authentication) {
		return recurringExpenses.findAllByOwner_IdOrderByActiveDescNextDateAscIdAsc(currentUser(authentication).getId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RecurringExpense create(@Valid @RequestBody RecurringExpenseRequest request, Authentication authentication) {
		AppUser user = currentUser(authentication);
		return recurringExpenses.save(new RecurringExpense(
				request.description().trim(), request.amount(), request.category().trim(), request.frequency(), request.startDate(), user));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<RecurringExpense> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody RecurringExpenseStatusRequest request,
			Authentication authentication) {
		return recurringExpenses.findByIdAndOwner_Id(id, currentUser(authentication).getId())
				.map(item -> { item.setActive(request.active()); return ResponseEntity.ok(recurringExpenses.save(item)); })
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
		return recurringExpenses.findByIdAndOwner_Id(id, currentUser(authentication).getId())
				.map(item -> { recurringExpenses.delete(item); return ResponseEntity.noContent().<Void>build(); })
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private AppUser currentUser(Authentication authentication) {
		return users.findByEmail(authentication.getName()).orElseThrow();
	}
}
