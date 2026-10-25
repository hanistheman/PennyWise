package pennywise.recurring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import pennywise.auth.AppUser;
import pennywise.transaction.Expense;

@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 40)
	private String category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private RecurrenceFrequency frequency;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "next_date", nullable = false)
	private LocalDate nextDate;

	@Column(name = "anchor_day", nullable = false)
	private int anchorDay;

	@Column(name = "anchor_month", nullable = false)
	private int anchorMonth;

	@Column(nullable = false)
	private boolean active = true;

	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser owner;

	protected RecurringExpense() {}

	public RecurringExpense(String description, BigDecimal amount, String category, RecurrenceFrequency frequency, LocalDate startDate, AppUser owner) {
		this.description = description;
		this.amount = amount;
		this.category = category;
		this.frequency = frequency;
		this.startDate = startDate;
		this.nextDate = startDate;
		this.anchorDay = startDate.getDayOfMonth();
		this.anchorMonth = startDate.getMonthValue();
		this.owner = owner;
	}

	public Long getId() { return id; }
	public String getDescription() { return description; }
	public BigDecimal getAmount() { return amount; }
	public String getCategory() { return category; }
	public RecurrenceFrequency getFrequency() { return frequency; }
	public LocalDate getStartDate() { return startDate; }
	public LocalDate getNextDate() { return nextDate; }
	public boolean isActive() { return active; }
	@JsonIgnore public AppUser getOwner() { return owner; }
	public void setActive(boolean active) { this.active = active; }

	public Expense createOccurrence() {
		return new Expense(description, amount, category, nextDate, owner);
	}

	public void advance() {
		if (frequency == RecurrenceFrequency.WEEKLY) {
			nextDate = nextDate.plusWeeks(1);
		} else if (frequency == RecurrenceFrequency.MONTHLY) {
			YearMonth nextMonth = YearMonth.from(nextDate).plusMonths(1);
			nextDate = nextMonth.atDay(Math.min(anchorDay, nextMonth.lengthOfMonth()));
		} else {
			YearMonth nextYear = YearMonth.of(nextDate.getYear() + 1, anchorMonth);
			nextDate = nextYear.atDay(Math.min(anchorDay, nextYear.lengthOfMonth()));
		}
	}
}
