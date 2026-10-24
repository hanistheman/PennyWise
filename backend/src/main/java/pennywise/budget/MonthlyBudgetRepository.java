package pennywise.budget;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {

	Optional<MonthlyBudget> findByOwner_IdAndBudgetMonth(Long ownerId, String budgetMonth);
}
