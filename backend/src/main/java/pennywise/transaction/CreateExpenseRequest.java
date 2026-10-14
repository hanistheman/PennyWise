package pennywise.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
		@NotBlank @Size(max = 120) String description,
		@NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
		@NotBlank @Size(max = 40) String category,
		@NotNull LocalDate spentOn) {
}
