package pennywise.recurring;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
	List<RecurringExpense> findAllByOwner_IdOrderByActiveDescNextDateAscIdAsc(Long ownerId);
	Optional<RecurringExpense> findByIdAndOwner_Id(Long id, Long ownerId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select recurring from RecurringExpense recurring where recurring.active = true and recurring.nextDate <= :today order by recurring.id")
	List<RecurringExpense> findDue(@Param("today") LocalDate today);
}
