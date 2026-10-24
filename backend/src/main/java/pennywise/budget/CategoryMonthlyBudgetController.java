package pennywise.budget;

import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
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
@RequestMapping("/api/category-budgets")
public class CategoryMonthlyBudgetController {

	private final CategoryMonthlyBudgetRepository budgetRepository;
	private final AppUserRepository userRepository;

	public CategoryMonthlyBudgetController(CategoryMonthlyBudgetRepository budgetRepository, AppUserRepository userRepository) {
		this.budgetRepository = budgetRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/{month}")
	public List<CategoryMonthlyBudget> getBudgets(@PathVariable YearMonth month, Authentication authentication) {
		return budgetRepository.findAllByOwner_IdAndBudgetMonthOrderByCategoryAsc(currentUser(authentication).getId(), month.toString());
	}

	@PutMapping("/{month}")
	public CategoryMonthlyBudget saveBudget(
			@PathVariable YearMonth month,
			@Valid @RequestBody SetCategoryMonthlyBudgetRequest request,
			Authentication authentication) {
		AppUser user = currentUser(authentication);
		CategoryMonthlyBudget budget = budgetRepository
				.findByOwner_IdAndBudgetMonthAndCategory(user.getId(), month.toString(), request.category())
				.orElseGet(() -> new CategoryMonthlyBudget(month, request.category(), request.amount(), user));
		budget.setAmount(request.amount());
		return budgetRepository.save(budget);
	}

	private AppUser currentUser(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName()).orElseThrow();
	}
}
