package de.caransgar.chorehub.repository;

import de.caransgar.chorehub.entity.ChoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChoreHistoryRepository extends JpaRepository<ChoreHistory, Long> {

    List<ChoreHistory> findByChoreId(Long choreId);
}
