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

@RestController
@RequestMapping("/api/budgets")
public class MonthlyBudgetController {

	private final MonthlyBudgetRepository budgetRepository;

	public MonthlyBudgetController(MonthlyBudgetRepository budgetRepository) {
		this.budgetRepository = budgetRepository;
	}

	@GetMapping("/{month}")
	public ResponseEntity<MonthlyBudget> getBudget(@PathVariable YearMonth month) {
		return budgetRepository.findByBudgetMonth(month.toString())
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.noContent().build());
	}

	@PutMapping("/{month}")
	public MonthlyBudget saveBudget(
			@PathVariable YearMonth month,
			@Valid @RequestBody SetMonthlyBudgetRequest request) {
		MonthlyBudget budget = budgetRepository.findByBudgetMonth(month.toString())
				.orElseGet(() -> new MonthlyBudget(month, request.amount()));
		budget.setAmount(request.amount());
		return budgetRepository.save(budget);
	}
}
