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

@RestController
@RequestMapping("/api/category-budgets")
public class CategoryMonthlyBudgetController {

	private final CategoryMonthlyBudgetRepository budgetRepository;

	public CategoryMonthlyBudgetController(CategoryMonthlyBudgetRepository budgetRepository) {
		this.budgetRepository = budgetRepository;
	}

	@GetMapping("/{month}")
	public List<CategoryMonthlyBudget> getBudgets(@PathVariable YearMonth month) {
		return budgetRepository.findAllByBudgetMonthOrderByCategoryAsc(month.toString());
	}

	@PutMapping("/{month}")
	public CategoryMonthlyBudget saveBudget(
			@PathVariable YearMonth month,
			@Valid @RequestBody SetCategoryMonthlyBudgetRequest request) {
		CategoryMonthlyBudget budget = budgetRepository
				.findByBudgetMonthAndCategory(month.toString(), request.category())
				.orElseGet(() -> new CategoryMonthlyBudget(month, request.category(), request.amount()));
		budget.setAmount(request.amount());
		return budgetRepository.save(budget);
	}
}
