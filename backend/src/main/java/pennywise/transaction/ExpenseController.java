package pennywise.transaction;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import pennywise.auth.AppUser;
import pennywise.auth.AppUserRepository;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

	private final ExpenseRepository expenseRepository;
	private final AppUserRepository userRepository;

	public ExpenseController(ExpenseRepository expenseRepository, AppUserRepository userRepository) {
		this.expenseRepository = expenseRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<Expense> listExpenses(Authentication authentication) {
		return expenseRepository.findAllByOwner_IdOrderBySpentOnDescIdDesc(currentUser(authentication).getId());
	}

	@GetMapping("/monthly-totals")
	public List<MonthlyExpenseTotal> monthlyTotals(Authentication authentication) {
		return expenseRepository.findAllByOwner_IdOrderBySpentOnDescIdDesc(currentUser(authentication).getId())
				.stream()
				.collect(Collectors.groupingBy(
						expense -> YearMonth.from(expense.getSpentOn()),
						TreeMap::new,
						Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)))
				.entrySet()
				.stream()
				.map(entry -> new MonthlyExpenseTotal(entry.getKey(), entry.getValue()))
				.toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Expense createExpense(@Valid @RequestBody ExpenseRequest request, Authentication authentication) {
		Expense expense = new Expense(
				request.description().trim(),
				request.amount(),
				request.category().trim(),
				request.spentOn(), currentUser(authentication));
		return expenseRepository.save(expense);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Expense> updateExpense(
			@PathVariable Long id,
			@Valid @RequestBody ExpenseRequest request,
			Authentication authentication) {
		return expenseRepository.findByIdAndOwner_Id(id, currentUser(authentication).getId())
				.map(expense -> {
					expense.update(
							request.description().trim(),
							request.amount(),
							request.category().trim(),
							request.spentOn());
					return ResponseEntity.ok(expenseRepository.save(expense));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication authentication) {
		return expenseRepository.findByIdAndOwner_Id(id, currentUser(authentication).getId())
				.map(expense -> { expenseRepository.delete(expense); return ResponseEntity.noContent().<Void>build(); })
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	private AppUser currentUser(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName()).orElseThrow();
	}
}
