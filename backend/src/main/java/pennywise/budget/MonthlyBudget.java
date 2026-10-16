package pennywise.budget;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "monthly_budgets", uniqueConstraints = @UniqueConstraint(columnNames = "budget_month"))
public class MonthlyBudget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "budget_month", nullable = false, length = 7)
	private String budgetMonth;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	protected MonthlyBudget() {
	}

	public MonthlyBudget(YearMonth budgetMonth, BigDecimal amount) {
		this.budgetMonth = budgetMonth.toString();
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public YearMonth getBudgetMonth() {
		return YearMonth.parse(budgetMonth);
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}
