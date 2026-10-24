package pennywise.budget;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMonthlyBudgetRepository extends JpaRepository<CategoryMonthlyBudget, Long> {

	List<CategoryMonthlyBudget> findAllByOwner_IdAndBudgetMonthOrderByCategoryAsc(Long ownerId, String budgetMonth);

	Optional<CategoryMonthlyBudget> findByOwner_IdAndBudgetMonthAndCategory(Long ownerId, String budgetMonth, String category);
}
