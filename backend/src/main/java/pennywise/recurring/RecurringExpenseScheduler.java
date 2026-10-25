package pennywise.recurring;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pennywise.transaction.ExpenseRepository;

@Service
public class RecurringExpenseScheduler {
	private final RecurringExpenseRepository recurringExpenses;
	private final ExpenseRepository expenses;

	public RecurringExpenseScheduler(RecurringExpenseRepository recurringExpenses, ExpenseRepository expenses) {
		this.recurringExpenses = recurringExpenses;
		this.expenses = expenses;
	}

	@Scheduled(cron = "0 0 * * * *", zone = "UTC")
	@Transactional
	public void createDueExpenses() {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);
		for (RecurringExpense recurring : recurringExpenses.findDue(today)) {
			while (!recurring.getNextDate().isAfter(today)) {
				expenses.save(recurring.createOccurrence());
				recurring.advance();
			}
		}
	}
}
