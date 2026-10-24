package pennywise.transaction;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	List<Expense> findAllByOwner_IdOrderBySpentOnDescIdDesc(Long ownerId);
	java.util.Optional<Expense> findByIdAndOwner_Id(Long id, Long ownerId);
}
