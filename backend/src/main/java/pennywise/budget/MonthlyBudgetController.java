package pennywise.budget;

import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import pennywise.auth.AppUser;
import pennywise.auth.AppUserRepository;

@RestController
@RequestMapping("/api/budgets")
public class MonthlyBudgetController {

	private final MonthlyBudgetRepository budgetRepository;
	private final AppUserRepository userRepository;

	public MonthlyBudgetController(MonthlyBudgetRepository budgetRepository, AppUserRepository userRepository) {
		this.budgetRepository = budgetRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/{month}")
	public ResponseEntity<MonthlyBudget> getBudget(@PathVariable YearMonth month, Authentication authentication) {
		AppUser user = currentUser(authentication);
		return budgetRepository.findByOwner_IdAndBudgetMonth(user.getId(), month.toString())
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PutMapping("/{month}")
	public MonthlyBudget saveBudget(
			@PathVariable YearMonth month,
			@Valid @RequestBody SetMonthlyBudgetRequest request,
			Authentication authentication) {
		AppUser user = currentUser(authentication);
		MonthlyBudget budget = budgetRepository.findByOwner_IdAndBudgetMonth(user.getId(), month.toString())
				.orElseGet(() -> new MonthlyBudget(month, request.amount(), user));
		budget.setAmount(request.amount());
		return budgetRepository.save(budget);
	}

	private AppUser currentUser(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName()).orElseThrow();
	}
}
