package pennywise.transaction;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyExpenseTotal(YearMonth month, BigDecimal amount) {
}
