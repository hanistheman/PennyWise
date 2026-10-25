package pennywise.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {
	List<UserCategory> findAllByOwner_IdOrderByNameAsc(Long ownerId);
	boolean existsByOwner_IdAndNameIgnoreCase(Long ownerId, String name);
	Optional<UserCategory> findByIdAndOwner_Id(Long id, Long ownerId);
}
