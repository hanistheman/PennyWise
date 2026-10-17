package pennywise.budget;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMonthlyBudgetRepository extends JpaRepository<CategoryMonthlyBudget, Long> {

	List<CategoryMonthlyBudget> findAllByBudgetMonthOrderByCategoryAsc(String budgetMonth);

	Optional<CategoryMonthlyBudget> findByBudgetMonthAndCategory(String budgetMonth, String category);
}
