package pennywise.recurring;

import jakarta.validation.constraints.NotNull;

public record RecurringExpenseStatusRequest(@NotNull Boolean active) {}
