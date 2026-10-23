package pennywise.transaction;

import jakarta.validation.Valid;
import java.util.List;
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

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

	private final ExpenseRepository expenseRepository;

	public ExpenseController(ExpenseRepository expenseRepository) {
		this.expenseRepository = expenseRepository;
	}

	@GetMapping
	public List<Expense> listExpenses() {
		return expenseRepository.findAllByOrderBySpentOnDescIdDesc();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Expense createExpense(@Valid @RequestBody ExpenseRequest request) {
		Expense expense = new Expense(
				request.description().trim(),
				request.amount(),
				request.category().trim(),
				request.spentOn());
		return expenseRepository.save(expense);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Expense> updateExpense(
			@PathVariable Long id,
			@Valid @RequestBody ExpenseRequest request) {
		return expenseRepository.findById(id)
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
	public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
		if (!expenseRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		expenseRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
