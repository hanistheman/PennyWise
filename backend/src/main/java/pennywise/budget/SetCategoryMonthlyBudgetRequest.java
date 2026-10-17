package pennywise.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record SetCategoryMonthlyBudgetRequest(
		@NotBlank @Pattern(regexp = "Food & drink|Transport|Shopping|Home|Health|Other") String category,
		@NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {
}
