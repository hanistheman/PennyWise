package pennywise.transaction;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	public Expense createExpense(@Valid @RequestBody CreateExpenseRequest request) {
		Expense expense = new Expense(
				request.description().trim(),
				request.amount(),
				request.category().trim(),
				request.spentOn());
		return expenseRepository.save(expense);
	}
}
