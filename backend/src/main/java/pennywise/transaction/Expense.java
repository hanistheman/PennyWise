package pennywise.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 40)
	private String category;

	@Column(nullable = false)
	private LocalDate spentOn;

	protected Expense() {
	}

	public Expense(String description, BigDecimal amount, String category, LocalDate spentOn) {
		this.description = description;
		this.amount = amount;
		this.category = category;
		this.spentOn = spentOn;
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCategory() {
		return category;
	}

	public LocalDate getSpentOn() {
		return spentOn;
	}

	public void update(String description, BigDecimal amount, String category, LocalDate spentOn) {
		this.description = description;
		this.amount = amount;
		this.category = category;
		this.spentOn = spentOn;
	}
}
