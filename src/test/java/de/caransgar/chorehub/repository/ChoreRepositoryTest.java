package de.caransgar.chorehub.repository;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.RecurrenceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChoreRepositoryTest {

    @Autowired
    private ChoreRepository choreRepository;

    @Test
    void testSaveAndFindChore() {
        // Given
        Chore chore = new Chore("Clean Kitchen", "Weekly kitchen cleaning", RecurrenceType.FLEXIBLE, "P1W", 1L);

        // When
        Chore savedChore = choreRepository.save(chore);

        // Then
        assertThat(savedChore.getId()).isNotNull();
        assertThat(savedChore.getName()).isEqualTo("Clean Kitchen");
        assertThat(savedChore.getRecurrenceType()).isEqualTo(RecurrenceType.FLEXIBLE);
    }

    @Test
    void testFindById() {
        // Given
        Chore chore = new Chore("Wash Dishes", "Daily dish washing", RecurrenceType.ONETIME, null, 2L);
        Chore savedChore = choreRepository.save(chore);

        // When
        Optional<Chore> foundChore = choreRepository.findById(savedChore.getId());

        // Then
        assertThat(foundChore).isPresent();
        assertThat(foundChore.get().getName()).isEqualTo("Wash Dishes");
    }

    @Test
    void testFindByAssignedUserId() {
        // Given
        Chore chore1 = new Chore("Mow Lawn", "Monthly lawn mowing", RecurrenceType.FIXED, "0 0 1 * *", 1L);
        Chore chore2 = new Chore("Vacuum", "Weekly vacuuming", RecurrenceType.FLEXIBLE, "P1W", 1L);
        Chore chore3 = new Chore("Grocery Shopping", "Weekly shopping", RecurrenceType.FLEXIBLE, "P1W", 2L);
        choreRepository.save(chore1);
        choreRepository.save(chore2);
        choreRepository.save(chore3);

        // When
        List<Chore> choresForUser1 = choreRepository.findByAssignedUserId(1L);

        // Then
        assertThat(choresForUser1).hasSize(2);
        assertThat(choresForUser1).extracting(Chore::getName).contains("Mow Lawn", "Vacuum");
    }

    @Test
    void testFindByRecurrenceType() {
        // Given
        Chore chore1 = new Chore("Clean Bathroom", "Monthly cleaning", RecurrenceType.FIXED, "0 0 1 * *", 1L);
        Chore chore2 = new Chore("Take out trash", "Weekly task", RecurrenceType.FLEXIBLE, "P1W", 1L);
        Chore chore3 = new Chore("Fix sink", "One-time repair", RecurrenceType.ONETIME, null, 2L);
        choreRepository.save(chore1);
        choreRepository.save(chore2);
        choreRepository.save(chore3);

        // When
        List<Chore> fixedChores = choreRepository.findByRecurrenceType(RecurrenceType.FIXED);
        List<Chore> flexibleChores = choreRepository.findByRecurrenceType(RecurrenceType.FLEXIBLE);
        List<Chore> onetimeChores = choreRepository.findByRecurrenceType(RecurrenceType.ONETIME);

        // Then
        assertThat(fixedChores).hasSize(1);
        assertThat(flexibleChores).hasSize(1);
        assertThat(onetimeChores).hasSize(1);
    }

    @Test
    void testDeleteChore() {
        // Given
        Chore chore = new Chore("Test Chore", "For deletion test", RecurrenceType.ONETIME, null, 1L);
        Chore savedChore = choreRepository.save(chore);

        // When
        choreRepository.deleteById(savedChore.getId());

        // Then
        Optional<Chore> deletedChore = choreRepository.findById(savedChore.getId());
        assertThat(deletedChore).isNotPresent();
    }
}